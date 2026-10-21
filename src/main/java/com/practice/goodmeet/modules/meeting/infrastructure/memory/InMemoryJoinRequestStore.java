package com.practice.goodmeet.modules.meeting.infrastructure.memory;

import com.practice.goodmeet.modules.meeting.domain.JoinRequest;
import com.practice.goodmeet.modules.meeting.domain.JoinRequestStore;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
class InMemoryJoinRequestStore implements JoinRequestStore {

  private final Map<UUID, JoinRequest> requests = new ConcurrentHashMap<>();

  @Override
  public JoinRequest save(JoinRequest request) {
    requests.put(request.id(), request);
    return request;
  }

  @Override
  public Optional<JoinRequest> findById(UUID id) {
    return Optional.ofNullable(requests.get(id));
  }

  @Override
  public void deleteById(UUID id) {
    requests.remove(id);
  }
}
