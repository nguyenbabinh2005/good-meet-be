package com.practice.goodmeet.modules.participant.domain;

import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import java.time.Instant;
import java.util.UUID;

public record Participant(
    UUID id,
    UUID meetingId,
    UUID userId,
    String displayName,
    ParticipantRole role,
    String mediaIdentity,
    Instant createdAt) {

  public static Participant create(
      UUID meetingId, UUID userId, String displayName, ParticipantRole role, Instant now) {
    UUID id = UUID.randomUUID();
    String mediaIdentity = userId != null ? "user_" + userId : "guest_" + id;
    return new Participant(id, meetingId, userId, displayName.strip(), role, mediaIdentity, now);
  }
}
