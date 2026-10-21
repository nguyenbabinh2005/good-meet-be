package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataParticipantSessionRepository
    extends JpaRepository<ParticipantSessionEntity, UUID> {

  Optional<ParticipantSessionEntity> findByParticipantIdAndSessionIdAndLeftAtIsNull(
      UUID participantId, UUID sessionId);

  List<ParticipantSessionEntity> findBySessionIdAndLeftAtIsNull(UUID sessionId);
}
