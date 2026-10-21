package com.practice.goodmeet.modules.meeting.domain;

import java.time.Instant;
import java.util.UUID;

public record ParticipantSession(
    UUID id, UUID participantId, UUID sessionId, Instant joinedAt, Instant leftAt) {

  public static ParticipantSession open(UUID participantId, UUID sessionId, Instant now) {
    return new ParticipantSession(UUID.randomUUID(), participantId, sessionId, now, null);
  }

  public ParticipantSession close(Instant now) {
    return new ParticipantSession(id, participantId, sessionId, joinedAt, now);
  }
}
