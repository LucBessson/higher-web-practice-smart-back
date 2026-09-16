package ru.yandex.practicum.smart.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.smart.dto.FeatureRequest;
import ru.yandex.practicum.smart.dto.FeatureResponse;
import ru.yandex.practicum.smart.services.FeatureService;

import java.util.List;

@RestController
@RequestMapping("/api/features")
public class FeatureController {
    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    @GetMapping
    public List<FeatureResponse> list() {
        return service.list();
    }

    @PostMapping("/discussion/{discussionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureResponse create(
            @PathVariable long discussionId,
            @Valid @RequestBody FeatureRequest request
    ) {
        return service.create(discussionId, request);
    }

    @PostMapping("/{id}/enable")
    public void enable(@PathVariable long id, @RequestParam(defaultValue = "true") boolean value) {
        service.enable(id, value);
    }
}
