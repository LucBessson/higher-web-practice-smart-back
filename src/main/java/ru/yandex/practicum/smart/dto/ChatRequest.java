package ru.yandex.practicum.smart.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String message) {}
