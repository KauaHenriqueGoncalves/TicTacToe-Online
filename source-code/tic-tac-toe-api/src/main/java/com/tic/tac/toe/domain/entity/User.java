package com.tic.tac.toe.domain.entity;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.validation.EmailValidation;
import com.tic.tac.toe.infrastructure.security.PasswordHasher;
import javax.persistence.*;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;
import org.eclipse.persistence.config.CacheIsolationType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import static org.eclipse.persistence.annotations.CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_username_email",
                        columnList = "username, email"
                )
        }
)
@Cache(
        type = CacheType.WEAK,
        isolation = CacheIsolationType.ISOLATED,
        expiry = 600000,
        alwaysRefresh = true,
        disableHits = true,
        coordinationType = INVALIDATE_CHANGED_OBJECTS

)
public class User {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 150)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(UUID id, String username, String email, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public static User create(CreateUserRequestDto dto) {
        if (!EmailValidation.isValid(dto.getEmail())) {
            throw new InputInvalidException("Formato do email invalido");
        }
        return new User(
                UUID.randomUUID(),
                dto.getUsername(),
                dto.getEmail(),
                PasswordHasher.hash(dto.getPassword()),
                LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
