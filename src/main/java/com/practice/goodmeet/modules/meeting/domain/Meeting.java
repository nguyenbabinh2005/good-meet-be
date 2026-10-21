package com.practice.goodmeet.modules.meeting.domain;

import com.practice.goodmeet.modules.meeting.contract.MeetingStatus;
import java.time.Instant;
import java.util.UUID;

public record Meeting(
    UUID id,
    String publicCode,
    String title,
    MeetingStatus status,
    Instant scheduledStartAt,
    Instant scheduledEndAt,
    String mediaRoomId,
    boolean captionsEnabled,
    boolean notesEnabled,
    boolean assistantEnabled,
    Instant createdAt) {

  public static Meeting instant(String publicCode, String title, Instant now) {
    return create(publicCode, title, MeetingStatus.ACTIVE, null, null, now);
  }

  public static Meeting scheduled(
      String publicCode, String title, Instant start, Instant end, Instant now) {
    return create(publicCode, title, MeetingStatus.SCHEDULED, start, end, now);
  }

  public boolean isClosed() {
    return status == MeetingStatus.ENDED || status == MeetingStatus.CANCELLED;
  }

  public Meeting started() {
    return copy(MeetingStatus.ACTIVE, scheduledStartAt, scheduledEndAt);
  }

  public Meeting ended() {
    return copy(MeetingStatus.ENDED, scheduledStartAt, scheduledEndAt);
  }

  public Meeting cancelled() {
    return copy(MeetingStatus.CANCELLED, scheduledStartAt, scheduledEndAt);
  }

  public Meeting rescheduled(Instant start, Instant end) {
    return copy(status, start, end);
  }

  public Meeting withSettings(Boolean captions, Boolean notes, Boolean assistant) {
    return new Meeting(
        id,
        publicCode,
        title,
        status,
        scheduledStartAt,
        scheduledEndAt,
        mediaRoomId,
        captions != null ? captions : captionsEnabled,
        notes != null ? notes : notesEnabled,
        assistant != null ? assistant : assistantEnabled,
        createdAt);
  }

  private static Meeting create(
      String publicCode,
      String title,
      MeetingStatus status,
      Instant start,
      Instant end,
      Instant now) {
    UUID id = UUID.randomUUID();
    return new Meeting(
        id, publicCode, title.strip(), status, start, end, "meeting-" + id, false, false, false, now);
  }

  private Meeting copy(MeetingStatus newStatus, Instant start, Instant end) {
    return new Meeting(
        id,
        publicCode,
        title,
        newStatus,
        start,
        end,
        mediaRoomId,
        captionsEnabled,
        notesEnabled,
        assistantEnabled,
        createdAt);
  }
}
