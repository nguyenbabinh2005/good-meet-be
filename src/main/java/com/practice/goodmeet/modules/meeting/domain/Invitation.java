package com.practice.goodmeet.modules.meeting.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record Invitation(UUID id, UUID meetingId, String email, Instant createdAt) {

  public static Invitation of(UUID meetingId, String email, Instant now) {
    return new Invitation(UUID.randomUUID(), meetingId, normalizeEmail(email), now);
  }

  public static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }
}
