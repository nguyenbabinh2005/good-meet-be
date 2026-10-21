package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import com.practice.goodmeet.modules.meeting.domain.MeetingSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataSessionRepository extends JpaRepository<SessionEntity, UUID> {

  Optional<SessionEntity> findByMeetingIdAndStatus(UUID meetingId, MeetingSession.Status status);
}
