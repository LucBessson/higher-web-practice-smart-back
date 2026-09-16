package ru.yandex.practicum.smart.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownJsonParser {
    private static final Pattern FENCED_JSON =
            Pattern.compile("```(?:json)?\\s*(.*?)\\s*```", Pattern.DOTALL);

    private final ObjectMapper objectMapper;
    private final Parser parser;

    public MarkdownJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
        HtmlRenderer.builder(options).build();
    }

    public JsonNode parse(String content) {
        String markdown = sanitizeMarkdown(content);
        try {
            return objectMapper.readTree(markdown);
        } catch (Exception first) {
            throw new IllegalArgumentException(
                    "Cannot parse AI response as JSON: " + first.getMessage()
            );
        }
    }

    public String sanitizeMarkdown(String content) {
        Matcher matcher = FENCED_JSON.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new IllegalArgumentException("JSON object was not found in Markdown response");
    }
}
