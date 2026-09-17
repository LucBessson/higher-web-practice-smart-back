package ru.yandex.practicum.smart.ollama;

import java.util.List;

public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream,
        boolean format
) {}
