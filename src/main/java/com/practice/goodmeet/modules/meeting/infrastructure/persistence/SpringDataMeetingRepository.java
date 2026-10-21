package com.practice.goodmeet.modules.meeting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMeetingRepository extends JpaRepository<MeetingEntity, UUID> {

  Optional<MeetingEntity> findByPublicCode(String publicCode);

  Optional<MeetingEntity> findByMediaRoomId(String mediaRoomId);
}
