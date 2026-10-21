package com.practice.goodmeet.modules.participant.contract;

import java.util.UUID;

public record ParticipantInfo(
    UUID id,
    UUID meetingId,
    UUID userId,
    String displayName,
    ParticipantRole role,
    String mediaIdentity) {}
