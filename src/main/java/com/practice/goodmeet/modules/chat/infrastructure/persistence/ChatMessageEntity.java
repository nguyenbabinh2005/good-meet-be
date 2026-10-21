package com.practice.goodmeet.modules.chat.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
class ChatMessageEntity {

  @Id private UUID id;

  @Column(name = "meeting_id", nullable = false)
  private UUID meetingId;

  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Column(name = "sender_participant_id", nullable = false)
  private UUID senderParticipantId;

  @Column(name = "content", nullable = false, length = 2000)
  private String content;

  @Column(name = "sent_at", nullable = false)
  private Instant sentAt;

  protected ChatMessageEntity() {}

  ChatMessageEntity(
      UUID id,
      UUID meetingId,
      UUID sessionId,
      UUID senderParticipantId,
      String content,
      Instant sentAt) {
    this.id = id;
    this.meetingId = meetingId;
    this.sessionId = sessionId;
    this.senderParticipantId = senderParticipantId;
    this.content = content;
    this.sentAt = sentAt;
  }

  UUID getId() {
    return id;
  }

  UUID getMeetingId() {
    return meetingId;
  }

  UUID getSessionId() {
    return sessionId;
  }

  UUID getSenderParticipantId() {
    return senderParticipantId;
  }

  String getContent() {
    return content;
  }

  Instant getSentAt() {
    return sentAt;
  }
}
