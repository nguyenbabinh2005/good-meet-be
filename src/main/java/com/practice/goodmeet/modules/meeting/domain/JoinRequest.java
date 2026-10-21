package com.practice.goodmeet.modules.meeting.domain;

import java.time.Instant;
import java.util.UUID;

public record JoinRequest(
    UUID id, UUID meetingId, UUID userId, String displayName, Instant requestedAt) {

  public static JoinRequest of(UUID meetingId, UUID userId, String displayName, Instant now) {
    return new JoinRequest(UUID.randomUUID(), meetingId, userId, displayName, now);
  }
}
