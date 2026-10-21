package com.practice.goodmeet.modules.identity.infrastructure.persistence;

import com.practice.goodmeet.modules.identity.domain.User;
import com.practice.goodmeet.modules.identity.domain.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaUserRepository implements UserRepository {

  private final SpringDataUserRepository repository;

  JpaUserRepository(SpringDataUserRepository repository) {
    this.repository = repository;
  }

  @Override
  public User save(User user) {
    return toDomain(repository.save(toEntity(user)));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email).map(JpaUserRepository::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  private static User toDomain(UserEntity entity) {
    return new User(
        entity.getId(),
        entity.getEmail(),
        entity.getPassword(),
        entity.getDisplayName(),
        entity.getCreatedAt());
  }

  private static UserEntity toEntity(User user) {
    return new UserEntity(
        user.id(), user.email(), user.password(), user.displayName(), user.createdAt());
  }
}
