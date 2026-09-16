package ru.yandex.practicum.smart.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.smart.domain.Discussion;
import ru.yandex.practicum.smart.dto.*;
import ru.yandex.practicum.smart.services.DiscussionService;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {
    private final DiscussionService service;

    public DiscussionController(DiscussionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Discussion create(@Valid @RequestBody CreateDiscussionRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public Discussion get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/messages")
    public ChatResponse chat(
            @PathVariable long id,
            @Valid @RequestBody ChatRequest request
    ) {
        return service.chat(id, request);
    }

    @PostMapping("/{id}/parse-last")
    public Object parseLast(@PathVariable long id) {
        return service.parseLastAnswer(id);
    }

    @PostMapping("/{id}/fix")
    public ChatResponse fix(
            @PathVariable long id,
            @RequestParam String error
    ) {
        return service.askToFix(id, error);
    }
}
