package com.practice.goodmeet.modules.chat.domain;

import java.time.Instant;
import java.util.UUID;

public record ChatMessage(
    UUID id,
    UUID meetingId,
    UUID sessionId,
    UUID senderParticipantId,
    String content,
    Instant sentAt) {

  public static ChatMessage create(
      UUID meetingId, UUID sessionId, UUID senderParticipantId, String content, Instant now) {
    return new ChatMessage(
        UUID.randomUUID(), meetingId, sessionId, senderParticipantId, content.strip(), now);
  }
}
