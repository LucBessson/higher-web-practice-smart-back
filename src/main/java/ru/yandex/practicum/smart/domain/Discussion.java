package ru.yandex.practicum.smart.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discussions")
public class Discussion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "system_prompt", nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feature> features = new ArrayList<>();

    public Discussion() {}

    public Discussion(String name, String systemPrompt, String model) {
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.model = model;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setDiscussion(this);
    }

    public void addFeature(Feature feature) {
        features.add(feature);
        feature.setDiscussion(this);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSystemPrompt() { return systemPrompt; }
    public String getModel() { return model; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<ChatMessage> getMessages() { return messages; }
    public List<Feature> getFeatures() { return features; }
    public void setName(String name) { this.name = name; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public void setModel(String model) { this.model = model; }
}
