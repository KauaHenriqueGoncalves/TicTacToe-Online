package application.service;

import com.tic.tac.toe.application.dto.request.CreateUserRequestDto;
import com.tic.tac.toe.application.dto.request.LoginRequestDto;
import com.tic.tac.toe.application.dto.response.AccessTokenResponseDto;
import com.tic.tac.toe.application.service.AuthServiceImpl;
import com.tic.tac.toe.domain.entity.User;
import com.tic.tac.toe.domain.exception.InputInvalidException;
import com.tic.tac.toe.domain.exception.UnauthorizedException;
import com.tic.tac.toe.domain.repositoy.UserRepository;
import com.tic.tac.toe.infrastructure.security.JwtService;
import com.tic.tac.toe.infrastructure.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userRepository, jwtService);
    }

    @Test
    void registerSavesValidUser() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        User user = service.register(new CreateUserRequestDto("joao", "joao@mail.com", "senha1234"));
        assertEquals("joao", user.getUsername());
        assertTrue(PasswordHasher.matches("senha1234", user.getPasswordHash()));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsNullUsernameOrPassword() {
        CreateUserRequestDto noUsername = new CreateUserRequestDto();
        assertThrows(InputInvalidException.class, () -> service.register(noUsername));
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerRejectsShortPassword() {
        InputInvalidException ex = assertThrows(InputInvalidException.class,
                () -> service.register(new CreateUserRequestDto("joao", "joao@mail.com", "1234567")));
        assertEquals("Password must be more than 8.", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerRejectsInvalidEmail() {
        InputInvalidException ex = assertThrows(InputInvalidException.class,
                () -> service.register(new CreateUserRequestDto("joao", "email-invalido", "senha1234")));
        assertEquals("Invalid email.", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void loginReturnsToken() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "joao", "joao@mail.com", PasswordHasher.hash("senha1234"), LocalDateTime.now());
        when(userRepository.findByEmail("joao@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(eq(id.toString()), anyLong())).thenReturn("token-jwt");

        AccessTokenResponseDto response = service.login(new LoginRequestDto("joao@mail.com", "senha1234"));
        assertEquals("token-jwt", response.getAccessToken());
    }

    @Test
    void loginFailsWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequestDto("x@x.com", "senha1234")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginFailsWithWrongPassword() {
        User user = new User(UUID.randomUUID(), "joao", "joao@mail.com", PasswordHasher.hash("senha1234"), LocalDateTime.now());
        when(userRepository.findByEmail("joao@mail.com")).thenReturn(Optional.of(user));
        assertThrows(UnauthorizedException.class,
                () -> service.login(new LoginRequestDto("joao@mail.com", "errada123")));
        verifyNoInteractions(jwtService);
    }
}
