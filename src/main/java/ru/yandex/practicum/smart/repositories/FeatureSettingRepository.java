package ru.yandex.practicum.smart.repositories;

import ru.yandex.practicum.smart.domain.FeatureSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureSettingRepository extends JpaRepository<FeatureSetting, Long> {}
