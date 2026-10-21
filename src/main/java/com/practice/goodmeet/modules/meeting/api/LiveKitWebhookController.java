package com.practice.goodmeet.modules.meeting.api;

import com.practice.goodmeet.modules.meeting.application.MediaRoom;
import com.practice.goodmeet.modules.meeting.application.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LiveKitWebhookController {

  private final MediaRoom mediaRoom;
  private final SessionService sessionService;

  LiveKitWebhookController(MediaRoom mediaRoom, SessionService sessionService) {
    this.mediaRoom = mediaRoom;
    this.sessionService = sessionService;
  }

  @PostMapping("/livekit/webhooks")
  void receive(
      HttpServletRequest request,
      @RequestHeader(name = "Authorization", required = false) String authorization)
      throws IOException {
    String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    sessionService.handleWebhook(mediaRoom.decodeWebhook(body, authorization));
  }
}
