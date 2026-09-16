package ru.yandex.practicum.smart.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "features")
public class Feature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(length = 10)
    private String method;

    @Column(length = 255)
    private String path;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeatureSetting> settings = new ArrayList<>();

    public Feature() {}

    public Feature(String name, String type, String description, String code, String method, String path) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.code = code;
        this.method = method;
        this.path = path;
    }

    public void addSetting(FeatureSetting setting) {
        settings.add(setting);
        setting.setFeature(this);
    }

    public Long getId() { return id; }
    public Discussion getDiscussion() { return discussion; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getCode() { return code; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<FeatureSetting> getSettings() { return settings; }
    public void setDiscussion(Discussion discussion) { this.discussion = discussion; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
