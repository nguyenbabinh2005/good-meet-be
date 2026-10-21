package com.practice.goodmeet.modules.meeting.api;

import com.practice.goodmeet.modules.meeting.application.JoinService;
import com.practice.goodmeet.shared.ApiHeaders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class JoinController {

  record JoinBody(UUID userId, @NotBlank @Size(max = 100) String displayName) {}

  record WaitingResponse(UUID requestId) {}

  record DecisionRequest(@NotNull Boolean approved) {}

  private final JoinService joinService;

  JoinController(JoinService joinService) {
    this.joinService = joinService;
  }

  @PostMapping("/meetings/{code}/join")
  ResponseEntity<?> join(@PathVariable String code, @Valid @RequestBody JoinBody body) {
    JoinService.JoinOutcome outcome = joinService.join(code, body.userId(), body.displayName());
    if (outcome.joined() != null) {
      return ResponseEntity.ok(outcome.joined());
    }
    return ResponseEntity.accepted().body(new WaitingResponse(outcome.requestId()));
  }

  @PostMapping("/join-requests/{requestId}/decision")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void decide(
      @PathVariable UUID requestId,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId,
      @Valid @RequestBody DecisionRequest request) {
    joinService.decide(requestId, participantId, request.approved());
  }
}
