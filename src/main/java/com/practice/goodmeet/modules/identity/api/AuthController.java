package com.practice.goodmeet.modules.identity.api;

import com.practice.goodmeet.modules.identity.application.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthController {

  record RegisterRequest(
      @NotBlank @Email @Size(max = 255) String email,
      @NotBlank @Size(min = 6, max = 100) String password,
      @NotBlank @Size(max = 100) String displayName) {}

  record LoginRequest(@NotBlank String email, @NotBlank String password) {}

  record LoginResponse(UUID userId) {}

  private final AuthService authService;

  AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  void register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request.email(), request.password(), request.displayName());
  }

  @PostMapping("/login")
  LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return new LoginResponse(authService.login(request.email(), request.password()));
  }
}
