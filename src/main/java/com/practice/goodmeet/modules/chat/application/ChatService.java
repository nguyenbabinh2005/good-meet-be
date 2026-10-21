package com.practice.goodmeet.modules.chat.application;

import com.practice.goodmeet.modules.chat.domain.ChatMessage;
import com.practice.goodmeet.modules.chat.domain.ChatMessageRepository;
import com.practice.goodmeet.modules.meeting.contract.MeetingInfo;
import com.practice.goodmeet.modules.meeting.contract.MeetingLookup;
import com.practice.goodmeet.modules.participant.contract.ParticipantDirectory;
import com.practice.goodmeet.modules.participant.contract.ParticipantInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {

  private static final int MAX_CONTENT_LENGTH = 2000;

  public record MessageView(
      UUID id,
      UUID meetingId,
      UUID senderParticipantId,
      String senderName,
      String content,
      Instant sentAt) {}

  private final ChatMessageRepository chatMessageRepository;
  private final MeetingLookup meetingLookup;
  private final ParticipantDirectory participantDirectory;
  private final Clock clock;

  ChatService(
      ChatMessageRepository chatMessageRepository,
      MeetingLookup meetingLookup,
      ParticipantDirectory participantDirectory,
      Clock clock) {
    this.chatMessageRepository = chatMessageRepository;
    this.meetingLookup = meetingLookup;
    this.participantDirectory = participantDirectory;
    this.clock = clock;
  }

  @Transactional
  public MessageView send(String code, UUID participantId, String content) {
    MeetingInfo meeting = findMeeting(code);
    ParticipantInfo sender = participantDirectory.requireMember(meeting.id(), participantId);
    String text = requireContent(content);
    UUID sessionId =
        meetingLookup
            .findActiveSessionId(meeting.id())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT, "The meeting has no active session"));
    ChatMessage saved =
        chatMessageRepository.save(
            ChatMessage.create(meeting.id(), sessionId, sender.id(), text, clock.instant()));
    return toView(saved, sender.displayName());
  }

  @Transactional(readOnly = true)
  public List<MessageView> history(String code, UUID participantId) {
    MeetingInfo meeting = findMeeting(code);
    participantDirectory.requireMember(meeting.id(), participantId);
    Map<UUID, String> senderNames = new HashMap<>();
    return chatMessageRepository.findByMeeting(meeting.id()).stream()
        .map(
            message ->
                toView(
                    message,
                    senderNames.computeIfAbsent(
                        message.senderParticipantId(),
                        id ->
                            participantDirectory
                                .findById(id)
                                .map(ParticipantInfo::displayName)
                                .orElse("Unknown"))))
        .toList();
  }

  private MeetingInfo findMeeting(String code) {
    return meetingLookup
        .findByPublicCode(code)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
  }

  private static String requireContent(String content) {
    if (content == null || content.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is required");
    }
    if (content.strip().length() > MAX_CONTENT_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is too long");
    }
    return content;
  }

  private static MessageView toView(ChatMessage message, String senderName) {
    return new MessageView(
        message.id(),
        message.meetingId(),
        message.senderParticipantId(),
        senderName,
        message.content(),
        message.sentAt());
  }
}
