package com.practice.goodmeet.modules.participant.infrastructure.persistence;

import com.practice.goodmeet.modules.participant.contract.ParticipantRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
class ParticipantEntity {

  @Id private UUID id;

  @Column(name = "meeting_id", nullable = false)
  private UUID meetingId;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private ParticipantRole role;

  @Column(name = "media_identity", nullable = false, length = 100)
  private String mediaIdentity;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ParticipantEntity() {}

  ParticipantEntity(
      UUID id,
      UUID meetingId,
      UUID userId,
      String displayName,
      ParticipantRole role,
      String mediaIdentity,
      Instant createdAt) {
    this.id = id;
    this.meetingId = meetingId;
    this.userId = userId;
    this.displayName = displayName;
    this.role = role;
    this.mediaIdentity = mediaIdentity;
    this.createdAt = createdAt;
  }

  UUID getId() {
    return id;
  }

  UUID getMeetingId() {
    return meetingId;
  }

  UUID getUserId() {
    return userId;
  }

  String getDisplayName() {
    return displayName;
  }

  ParticipantRole getRole() {
    return role;
  }

  String getMediaIdentity() {
    return mediaIdentity;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
