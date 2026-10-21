package com.practice.goodmeet.modules.breakout.api;

import com.practice.goodmeet.modules.breakout.application.BreakoutService;
import com.practice.goodmeet.shared.ApiHeaders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings/{code}/breakout-rooms")
class BreakoutController {

  record CreateRequest(@NotBlank @Size(max = 100) String name) {}

  private final BreakoutService breakoutService;

  BreakoutController(BreakoutService breakoutService) {
    this.breakoutService = breakoutService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  BreakoutService.BreakoutRoomView create(
      @PathVariable String code,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId,
      @Valid @RequestBody CreateRequest request) {
    return breakoutService.create(code, participantId, request.name());
  }

  @PutMapping("/{roomId}/participants/{targetParticipantId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void assign(
      @PathVariable String code,
      @PathVariable UUID roomId,
      @PathVariable UUID targetParticipantId,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    breakoutService.assign(code, participantId, roomId, targetParticipantId);
  }

  @DeleteMapping("/{roomId}/participants/{targetParticipantId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void returnToMain(
      @PathVariable String code,
      @PathVariable UUID roomId,
      @PathVariable UUID targetParticipantId,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    breakoutService.returnToMain(code, participantId, roomId, targetParticipantId);
  }

  @PostMapping("/{roomId}/close")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void close(
      @PathVariable String code,
      @PathVariable UUID roomId,
      @RequestHeader(ApiHeaders.PARTICIPANT_ID) UUID participantId) {
    breakoutService.close(code, participantId, roomId);
  }
}
