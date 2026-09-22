package com.tic.tac.toe.domain.repositoy;

import com.tic.tac.toe.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    User save(User user);
}
