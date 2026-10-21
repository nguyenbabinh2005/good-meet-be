package com.practice.goodmeet.modules.participant.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

  Optional<ParticipantEntity> findByMeetingIdAndUserId(UUID meetingId, UUID userId);
}
