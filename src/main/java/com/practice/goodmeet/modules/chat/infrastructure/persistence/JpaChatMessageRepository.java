package com.practice.goodmeet.modules.chat.infrastructure.persistence;

import com.practice.goodmeet.modules.chat.domain.ChatMessage;
import com.practice.goodmeet.modules.chat.domain.ChatMessageRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaChatMessageRepository implements ChatMessageRepository {

  private final SpringDataChatMessageRepository repository;

  JpaChatMessageRepository(SpringDataChatMessageRepository repository) {
    this.repository = repository;
  }

  @Override
  public ChatMessage save(ChatMessage message) {
    return toDomain(
        repository.save(
            new ChatMessageEntity(
                message.id(),
                message.meetingId(),
                message.sessionId(),
                message.senderParticipantId(),
                message.content(),
                message.sentAt())));
  }

  @Override
  public List<ChatMessage> findByMeeting(UUID meetingId) {
    return repository.findByMeetingIdOrderBySentAtAsc(meetingId).stream()
        .map(JpaChatMessageRepository::toDomain)
        .toList();
  }

  private static ChatMessage toDomain(ChatMessageEntity entity) {
    return new ChatMessage(
        entity.getId(),
        entity.getMeetingId(),
        entity.getSessionId(),
        entity.getSenderParticipantId(),
        entity.getContent(),
        entity.getSentAt());
  }
}
