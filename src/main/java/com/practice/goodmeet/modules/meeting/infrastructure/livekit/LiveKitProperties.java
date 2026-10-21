package com.practice.goodmeet.modules.meeting.infrastructure.livekit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("application.livekit")
public record LiveKitProperties(
    @NotNull URI url, @NotBlank String apiKey, @NotBlank String apiSecret, @NotNull Duration tokenTtl) {}
