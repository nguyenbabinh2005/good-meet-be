package com.practice.goodmeet.modules.meeting.domain;

import java.time.Instant;
import java.util.UUID;

public record MeetingSession(
    UUID id, UUID meetingId, Status status, Instant startedAt, Instant endedAt) {

  public enum Status {
    ACTIVE,
    ENDED
  }

  public static MeetingSession open(UUID meetingId, Instant now) {
    return new MeetingSession(UUID.randomUUID(), meetingId, Status.ACTIVE, now, null);
  }

  public MeetingSession end(Instant now) {
    return new MeetingSession(id, meetingId, Status.ENDED, startedAt, now);
  }
}
