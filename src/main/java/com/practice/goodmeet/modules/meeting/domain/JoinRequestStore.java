package com.practice.goodmeet.modules.meeting.domain;

import java.util.Optional;
import java.util.UUID;

public interface JoinRequestStore {

  JoinRequest save(JoinRequest request);

  Optional<JoinRequest> findById(UUID id);

  void deleteById(UUID id);
}
