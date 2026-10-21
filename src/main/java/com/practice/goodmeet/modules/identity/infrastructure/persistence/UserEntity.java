package com.practice.goodmeet.modules.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
class UserEntity {

  @Id private UUID id;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "password", nullable = false, length = 100)
  private String password;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected UserEntity() {}

  UserEntity(UUID id, String email, String password, String displayName, Instant createdAt) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.displayName = displayName;
    this.createdAt = createdAt;
  }

  UUID getId() {
    return id;
  }

  String getEmail() {
    return email;
  }

  String getPassword() {
    return password;
  }

  String getDisplayName() {
    return displayName;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
