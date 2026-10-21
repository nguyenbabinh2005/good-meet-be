package com.practice.goodmeet.modules.chat.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

  List<ChatMessageEntity> findByMeetingIdOrderBySentAtAsc(UUID meetingId);
}
