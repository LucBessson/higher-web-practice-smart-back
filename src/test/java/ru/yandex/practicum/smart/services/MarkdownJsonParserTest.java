package ru.yandex.practicum.smart.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownJsonParserTest {
    private final MarkdownJsonParser parser = new MarkdownJsonParser(new ObjectMapper());

    @Test
    void shouldParseJsonInsideMarkdownFence() {
        var result = parser.parse("""
                ```json
                {"code":"SELECT * FROM users","type":"SQL"}
                ```
                """);

        assertEquals("SQL", result.get("type").asText());
        assertEquals("SELECT * FROM users", result.get("code").asText());
    }

    @Test
    void shouldParsePlainJson() {
        var result = parser.parse("""
                Here is the result:
                {"name":"users","type":"API"}
                """);

        assertEquals("users", result.get("name").asText());
    }

    @Test
    void shouldRejectInvalidResponse() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("I cannot provide JSON"));
    }
}
