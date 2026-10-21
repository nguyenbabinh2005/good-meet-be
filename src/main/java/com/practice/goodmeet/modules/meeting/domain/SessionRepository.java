package com.practice.goodmeet.modules.meeting.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {

  MeetingSession save(MeetingSession session);

  Optional<MeetingSession> findActive(UUID meetingId);

  ParticipantSession save(ParticipantSession participantSession);

  Optional<ParticipantSession> findOpenParticipantSession(UUID participantId, UUID sessionId);

  List<ParticipantSession> findOpenParticipantSessions(UUID sessionId);
}
