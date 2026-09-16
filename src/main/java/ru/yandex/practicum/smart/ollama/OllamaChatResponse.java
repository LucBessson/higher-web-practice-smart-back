package ru.yandex.practicum.smart.ollama;

public record OllamaChatResponse(String model, OllamaMessage message, boolean done) {}
