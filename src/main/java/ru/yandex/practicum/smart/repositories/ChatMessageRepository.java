package ru.yandex.practicum.smart.repositories;

import ru.yandex.practicum.smart.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByDiscussionIdOrderByCreatedAtAsc(Long discussionId);
}
