package com.practice.goodmeet.modules.identity.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public record User(UUID id, String email, String password, String displayName, Instant createdAt) {

  public static User register(String email, String password, String displayName, Instant now) {
    return new User(UUID.randomUUID(), normalizeEmail(email), password, displayName.strip(), now);
  }

  public static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }
}
