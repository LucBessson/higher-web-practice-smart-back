package ru.yandex.practicum.smart.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.smart.domain.ChatMessage;
import ru.yandex.practicum.smart.domain.Discussion;
import ru.yandex.practicum.smart.dto.*;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.ollama.OllamaMessage;
import ru.yandex.practicum.smart.ollama.OllamaChatResponse;
import ru.yandex.practicum.smart.repositories.ChatMessageRepository;
import ru.yandex.practicum.smart.repositories.DiscussionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscussionService {
    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are a smart backend constructor agent.
            Return implementation proposals as Markdown JSON when the user asks for a feature.
            SQL feature schema: {"name":"...","type":"SQL","description":"...","code":"SELECT ...","parameters":{}}
            API feature schema: {"name":"...","type":"API","description":"...","method":"GET","path":"/api/...","code":"SQL query or description","parameters":{}}
            Do not invent Java source code. Prefer safe parameterized SQL.
            """;

    private final DiscussionRepository discussionRepository;
    private final ChatMessageRepository messageRepository;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper;
    private final MarkdownJsonParser parser;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            ChatMessageRepository messageRepository,
            OllamaService ollamaService,
            ObjectMapper objectMapper,
            MarkdownJsonParser parser
    ) {
        this.discussionRepository = discussionRepository;
        this.messageRepository = messageRepository;
        this.ollamaService = ollamaService;
        this.objectMapper = objectMapper;
        this.parser = parser;
    }

    @Transactional
    public Discussion create(CreateDiscussionRequest request) {
        String prompt = request.systemPrompt() == null || request.systemPrompt().isBlank()
                ? DEFAULT_SYSTEM_PROMPT : request.systemPrompt();
        Discussion discussion = new Discussion(
                request.name(),
                prompt,
                request.model() == null || request.model().isBlank() ? "gemma3:4b" : request.model()
        );
        discussion.addMessage(new ChatMessage("system", prompt));
        return discussionRepository.save(discussion);
    }

    @Transactional(readOnly = true)
    public Discussion get(long id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Discussion " + id + " not found"));
    }

    @Transactional
    public ChatResponse chat(long discussionId, ChatRequest request) {
        Discussion discussion = get(discussionId);
        ChatMessage userMessage = new ChatMessage("user", request.message());
        discussion.addMessage(userMessage);
        discussionRepository.save(discussion);

        List<ChatMessage> history = messageRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId);
        List<OllamaMessage> messages = history.stream()
                .map(m -> new OllamaMessage(m.getRole(), m.getContent()))
                .toList();

        OllamaChatResponse response = ollamaService.chat(discussion.getModel(), messages, false);
        String answer = response.message().content();

        discussion.addMessage(new ChatMessage("assistant", answer));
        discussionRepository.save(discussion);

        return new ChatResponse(discussionId, "assistant", answer);
    }

    @Transactional(readOnly = true)
    public JsonNode parseLastAnswer(long discussionId) {
        List<ChatMessage> messages = messageRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId);
        ChatMessage last = messages.stream()
                .filter(m -> "assistant".equals(m.getRole()))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new IllegalArgumentException("No assistant answer in discussion"));
        return parser.parse(last.getContent());
    }

    @Transactional
    public ChatResponse askToFix(long discussionId, String error) {
        return chat(discussionId, new ChatRequest(
                "The previous response could not be processed. Fix it. Parser error: " + error
                        + ". Return only valid JSON wrapped in Markdown if JSON is requested."
        ));
    }
}
