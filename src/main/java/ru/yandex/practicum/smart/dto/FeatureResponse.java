package ru.yandex.practicum.smart.dto;

import java.util.List;

public record FeatureResponse(
        Long id, String name, String type, String description,
        String method, String path, boolean enabled, List<String> settings
) {}
