package com.practice.goodmeet.modules.breakout.domain;

import java.time.Instant;
import java.util.UUID;

public record BreakoutRoom(
    UUID id,
    UUID meetingId,
    UUID sessionId,
    String name,
    String mediaRoomId,
    Instant createdAt,
    Instant closedAt) {

  public static BreakoutRoom create(UUID meetingId, UUID sessionId, String name, Instant now) {
    UUID id = UUID.randomUUID();
    return new BreakoutRoom(id, meetingId, sessionId, name.strip(), "breakout-" + id, now, null);
  }

  public boolean isOpen() {
    return closedAt == null;
  }

  public BreakoutRoom closed(Instant now) {
    return new BreakoutRoom(id, meetingId, sessionId, name, mediaRoomId, createdAt, now);
  }
}
