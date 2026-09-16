package ru.yandex.practicum.smart.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.smart.domain.Discussion;
import ru.yandex.practicum.smart.services.DiscussionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiscussionController.class)
class DiscussionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    DiscussionService service;

    @Test
    void shouldCreateDiscussion() throws Exception {
        when(service.create(any())).thenReturn(new Discussion(
                "test", "system", "gemma3:4b"
        ));

        mockMvc.perform(post("/api/discussions")
                        .contentType("application/json")
                        .content("""
                                {"name":"test","systemPrompt":"system","model":"gemma3:4b"}
                                """))
                .andExpect(status().isCreated());
    }
}
