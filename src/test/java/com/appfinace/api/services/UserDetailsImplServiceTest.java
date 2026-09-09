package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsImplServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsImplService userDetailsImplService;

    private User existUser;

    @BeforeEach
    public void setUp() {
        existUser = new User();
        existUser.setEmail("joao@email.com");
        existUser.setPassword("pass-hashed");
    }

    @Test
    public void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(existUser));

        UserDetails userDetails = userDetailsImplService.loadUserByUsername("joao@email.com");

        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        assertThat(userDetails.getUsername()).isEqualTo("joao@email.com");
        assertThat(userDetails.getPassword()).isEqualTo("pass-hashed");
    }

    @Test
    public void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("notexists@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsImplService.loadUserByUsername("notexists@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }
}
