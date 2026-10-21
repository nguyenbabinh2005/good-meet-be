package com.practice.goodmeet.modules.meeting.contract;

import java.util.Optional;
import java.util.UUID;

public interface MeetingLookup {

  Optional<MeetingInfo> findById(UUID meetingId);

  Optional<MeetingInfo> findByPublicCode(String publicCode);

  Optional<UUID> findActiveSessionId(UUID meetingId);
}
