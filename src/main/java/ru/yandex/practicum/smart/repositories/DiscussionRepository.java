package ru.yandex.practicum.smart.repositories;

import ru.yandex.practicum.smart.domain.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {}
