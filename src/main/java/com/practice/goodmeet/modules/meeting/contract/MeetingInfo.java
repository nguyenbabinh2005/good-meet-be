package com.practice.goodmeet.modules.meeting.contract;

import java.time.Instant;
import java.util.UUID;

public record MeetingInfo(
    UUID id,
    String publicCode,
    String title,
    MeetingStatus status,
    Instant scheduledStartAt,
    Instant scheduledEndAt,
    String mediaRoomId,
    boolean captionsEnabled,
    boolean notesEnabled,
    boolean assistantEnabled) {}
