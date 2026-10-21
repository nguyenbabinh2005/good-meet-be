package com.practice.goodmeet.modules.meeting.contract;

import java.time.Instant;

public record MediaToken(String url, String token, Instant expiresAt) {}
