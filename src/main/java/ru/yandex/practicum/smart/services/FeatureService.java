package ru.yandex.practicum.smart.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.smart.domain.*;
import ru.yandex.practicum.smart.dto.*;
import ru.yandex.practicum.smart.exception.BadFeatureException;
import ru.yandex.practicum.smart.exception.NotFoundException;
import ru.yandex.practicum.smart.repositories.*;

import java.util.List;
import java.util.Map;

@Service
public class FeatureService {
    private final FeatureRepository featureRepository;
    private final FeatureSettingRepository settingRepository;
    private final DiscussionRepository discussionRepository;
    private final DynamicApiService dynamicApiService;

    public FeatureService(
            FeatureRepository featureRepository,
            FeatureSettingRepository settingRepository,
            DiscussionRepository discussionRepository,
            DynamicApiService dynamicApiService
    ) {
        this.featureRepository = featureRepository;
        this.settingRepository = settingRepository;
        this.discussionRepository = discussionRepository;
        this.dynamicApiService = dynamicApiService;
    }

    @Transactional
    public FeatureResponse create(long discussionId, FeatureRequest request) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion " + discussionId + " not found"));

        validate(request);
        Feature feature = new Feature(
                request.name(), request.type().toUpperCase(),
                request.description(), request.code(), request.method(), request.path()
        );
        discussion.addFeature(feature);

        if (request.settings() != null) {
            request.settings().forEach((key, value) ->
                    feature.addSetting(new FeatureSetting(key, value)));
        }

        Feature saved = featureRepository.save(feature);
        if ("API".equals(saved.getType())) {
            dynamicApiService.register(saved);
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FeatureResponse> list() {
        return featureRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feature " + id + " not found"));
        feature.setEnabled(enabled);
        featureRepository.save(feature);
        dynamicApiService.refresh();
    }

    private void validate(FeatureRequest request) {
        String type = request.type().toUpperCase();
        if (!List.of("API", "SQL").contains(type)) {
            throw new BadFeatureException("Feature type must be API or SQL");
        }
        if ("API".equals(type)) {
            if (request.method() == null || request.path() == null) {
                throw new BadFeatureException("API feature requires method and path");
            }
            if (!request.path().startsWith("/")) {
                throw new BadFeatureException("API path must start with /");
            }
            if (!List.of("GET", "POST").contains(request.method().toUpperCase())) {
                throw new BadFeatureException("Only GET and POST are supported");
            }
        }
    }

    private FeatureResponse toResponse(Feature f) {
        return new FeatureResponse(
                f.getId(), f.getName(), f.getType(), f.getDescription(),
                f.getMethod(), f.getPath(), f.isEnabled(),
                f.getSettings().stream().map(s -> s.getSettingName() + "=" + s.getSettingValue()).toList()
        );
    }
}
