package ru.yandex.practicum.smart.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "feature_settings")
public class FeatureSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_id", nullable = false)
    private Feature feature;

    @Column(name = "setting_name", nullable = false, length = 100)
    private String settingName;

    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    private String settingValue;

    public FeatureSetting() {}

    public FeatureSetting(String settingName, String settingValue) {
        this.settingName = settingName;
        this.settingValue = settingValue;
    }

    public Long getId() { return id; }
    public Feature getFeature() { return feature; }
    public String getSettingName() { return settingName; }
    public String getSettingValue() { return settingValue; }
    public void setFeature(Feature feature) { this.feature = feature; }
}
