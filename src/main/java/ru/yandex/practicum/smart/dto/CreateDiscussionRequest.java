package ru.yandex.practicum.smart.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDiscussionRequest(
        @NotBlank String name,
        String systemPrompt,
        String model
) {}
