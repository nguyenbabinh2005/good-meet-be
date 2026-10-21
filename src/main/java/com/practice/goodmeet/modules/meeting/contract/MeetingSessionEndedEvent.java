package com.practice.goodmeet.modules.meeting.contract;

import java.util.UUID;

public record MeetingSessionEndedEvent(UUID meetingId, UUID sessionId) {}
