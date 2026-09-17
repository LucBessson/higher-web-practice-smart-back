package ru.yandex.practicum.smart.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record FeatureRequest(
        @NotBlank String name,
        @NotBlank String type,
        String description,
        String code,
        String method,
        String path,
        Map<String, String> settings
) {}
