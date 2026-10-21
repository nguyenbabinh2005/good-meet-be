package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import com.practice.goodmeet.modules.meeting.contract.MeetingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meetings")
class MeetingEntity {

  @Id private UUID id;

  @Column(name = "public_code", nullable = false, length = 20)
  private String publicCode;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private MeetingStatus status;

  @Column(name = "scheduled_start_at")
  private Instant scheduledStartAt;

  @Column(name = "scheduled_end_at")
  private Instant scheduledEndAt;

  @Column(name = "media_room_id", nullable = false, length = 100)
  private String mediaRoomId;

  @Column(name = "captions_enabled", nullable = false)
  private boolean captionsEnabled;

  @Column(name = "notes_enabled", nullable = false)
  private boolean notesEnabled;

  @Column(name = "assistant_enabled", nullable = false)
  private boolean assistantEnabled;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MeetingEntity() {}

  MeetingEntity(
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
    this.id = id;
    this.publicCode = publicCode;
    this.title = title;
    this.status = status;
    this.scheduledStartAt = scheduledStartAt;
    this.scheduledEndAt = scheduledEndAt;
    this.mediaRoomId = mediaRoomId;
    this.captionsEnabled = captionsEnabled;
    this.notesEnabled = notesEnabled;
    this.assistantEnabled = assistantEnabled;
    this.createdAt = createdAt;
  }

  UUID getId() {
    return id;
  }

  String getPublicCode() {
    return publicCode;
  }

  String getTitle() {
    return title;
  }

  MeetingStatus getStatus() {
    return status;
  }

  Instant getScheduledStartAt() {
    return scheduledStartAt;
  }

  Instant getScheduledEndAt() {
    return scheduledEndAt;
  }

  String getMediaRoomId() {
    return mediaRoomId;
  }

  boolean isCaptionsEnabled() {
    return captionsEnabled;
  }

  boolean isNotesEnabled() {
    return notesEnabled;
  }

  boolean isAssistantEnabled() {
    return assistantEnabled;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
