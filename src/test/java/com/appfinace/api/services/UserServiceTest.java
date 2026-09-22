package com.appfinace.api.services;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.user.UpdatePasswordRequestDto;
import com.appfinace.api.dto.user.UpdateUserRequestDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.infra.S3StoragePort;
import com.appfinace.api.repositories.ProfileImagesRepository;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3StoragePort s3StoragePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileImagesRepository profileImageRepository;

    private UUID userId;
    private User existingUser;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("joao@email.com");
        existingUser.setName("João");
        existingUser.setCurrentProfileImgUrl("//image-url");
        existingUser.setPassword("password-old-hash");
    }

    @Test
    public void shouldCreateUserSuccessfully() {
        UserRequestDto userDto = new UserRequestDto("maria@email.com", "Maria", null, "password");

        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(passwordEncoder.encode(userDto.password())).thenReturn("password-hash");

        userService.createUser(userDto);

        verify(userRepository, times(1)).save(any(User.class));
        verify(profileImageRepository, never()).save(any());
    }

    @Test
    public void shouldThrowConflictWhenEmailAlreadyExists() {
        UserRequestDto userDto = new UserRequestDto("maria@email.com", "Maria", null, "password");

        when(userRepository.existsByEmail(userDto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldFindUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        var result = userService.findUser(userId);

        assertThat(result.email()).isEqualTo("joao@email.com");
        assertThat(result.name()).isEqualTo("João");
    }

    @Test
    public void shouldThrowNotFoundUserWithId() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não encontrado");

    }

    @Test
    public void shouldUpdatePasswordWhenCurrentPasswordIsCorrect() {
        UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("password-hashed", "new-password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password-hashed", existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-password-hash");

        userService.updatePassword(userId, dto);

        verify(userRepository).save(existingUser);
        assertThat(existingUser.getPassword()).isEqualTo("new-password-hash");
    }

    @Test
    public void shouldThrowExceptionWhenCuurentPassowrdIsIncorrect() {
        UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("password-incorrect", "new-password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password-incorrect", existingUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(userId, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Senha atual incorreta");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldUpdateUserSuccessfully() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto("joao.updated@email.com", "João Updated", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("joao.updated@email.com")).thenReturn(false);

        userService.updateUser(userId, dto);

        verify(userRepository).save(existingUser);
        assertThat(existingUser.getName()).isEqualTo("João Updated");
        assertThat(existingUser.getEmail()).isEqualTo("joao.updated@email.com");

        verify(profileImageRepository, never()).save(any());
    }

    @Test
    public void shouldThrowConflictWhenUpdatingUserWithExistingEmail() {
        UpdateUserRequestDto dto = new UpdateUserRequestDto("joao.updated@email.com", "João Updated", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("joao.updated@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(userRepository, never()).save(existingUser);
        verify(profileImageRepository, never()).save(any());
    }

    @Test
    public void shouldDeleteUserSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        verify(userRepository).delete(existingUser);
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(userRepository, never()).delete(any());
    }
}
