package com.practice.goodmeet.modules.chat.domain;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository {

  ChatMessage save(ChatMessage message);

  List<ChatMessage> findByMeeting(UUID meetingId);
}
