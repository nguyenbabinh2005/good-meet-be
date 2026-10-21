package com.practice.goodmeet.modules.participant.domain;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {

  Participant save(Participant participant);

  Optional<Participant> findById(UUID id);

  Optional<Participant> findByMeetingAndUser(UUID meetingId, UUID userId);
}
