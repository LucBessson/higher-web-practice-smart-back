package ru.yandex.practicum.smart.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.smart.ollama.*;

import java.util.List;

@Service
public class OllamaService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int maxRetries;

    public OllamaService(
            RestTemplate restTemplate,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.max-retries}") int maxRetries
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
    }

    public OllamaChatResponse chat(String model, List<OllamaMessage> messages, boolean json) {
        OllamaChatRequest body = new OllamaChatRequest(model, messages, false, json);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OllamaChatRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<OllamaChatResponse> response = restTemplate.exchange(
                baseUrl + "/api/chat",
                HttpMethod.POST,
                entity,
                OllamaChatResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Ollama returned HTTP " + response.getStatusCode());
        }
        return response.getBody();
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}
