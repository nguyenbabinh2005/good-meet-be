package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataInvitationRepository extends JpaRepository<InvitationEntity, UUID> {

  List<InvitationEntity> findByMeetingIdOrderByCreatedAtAsc(UUID meetingId);

  Optional<InvitationEntity> findByIdAndMeetingId(UUID id, UUID meetingId);

  Optional<InvitationEntity> findByMeetingIdAndEmail(UUID meetingId, String email);
}
