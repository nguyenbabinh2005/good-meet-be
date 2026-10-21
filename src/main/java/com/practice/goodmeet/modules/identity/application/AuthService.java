package com.practice.goodmeet.modules.identity.application;

import com.practice.goodmeet.modules.identity.domain.User;
import com.practice.goodmeet.modules.identity.domain.UserRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final Clock clock;

  AuthService(UserRepository userRepository, Clock clock) {
    this.userRepository = userRepository;
    this.clock = clock;
  }

  @Transactional
  public void register(String email, String password, String displayName) {
    String normalized = User.normalizeEmail(email);
    if (userRepository.existsByEmail(normalized)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }
    userRepository.save(User.register(normalized, password, displayName, clock.instant()));
  }

  @Transactional(readOnly = true)
  public UUID login(String email, String password) {
    return userRepository
        .findByEmail(User.normalizeEmail(email))
        .filter(user -> user.password().equals(password))
        .map(User::id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
  }
}
