package com.practice.goodmeet.modules.participant.contract;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantDirectory {

  ParticipantInfo join(UUID meetingId, UUID userId, String displayName, ParticipantRole role);

  Optional<ParticipantInfo> findById(UUID participantId);

  Optional<ParticipantInfo> findByUser(UUID meetingId, UUID userId);

  ParticipantInfo requireMember(UUID meetingId, UUID participantId);

  ParticipantInfo requireHost(UUID meetingId, UUID participantId);
}
