package com.appfinace.api.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.auth.AuthLoginRequestDto;
import com.appfinace.api.dto.auth.AuthLoginResponseDto;
import com.appfinace.api.dto.user.UserResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.AuthService;
import com.appfinace.api.service.UserService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsImplService userDetailsImplService;

    private UUID userId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetailsImpl, null, userDetailsImpl.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldLoginSuccessfullyAndSetCookie() throws Exception {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("joao@email.com", "pass123");
        AuthLoginResponseDto response = new AuthLoginResponseDto("fake-jwt-token");

        when(authService.login(dto)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().value("access_token", "fake-jwt-token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().maxAge("access_token", 60 * 60 * 24));
    }

    @Test
    public void shouldThrowBadRequestWhenLoginCredentialsAreInvalid() throws Exception {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("incorrect@email.com", "pass123");

        when(authService.login(dto))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ou senha inválidos"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldLogoutSuccessfullyAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().value("access_token", ""))
                .andExpect(cookie().maxAge("access_token", 0));
    }

    @Test
    public void shouldReturnCurrentUserSuccessfully() throws Exception {
        UserResponseDto response = new UserResponseDto(userId, "joao@email.com", "Joao", "//image.png");

        when(userService.findUser(userId)).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.name").value("Joao"))
                .andExpect(jsonPath("$.currentProfileImgUrl").value("//image.png"));
    }

    @Test
    public void shouldThrowNotFoundWhenCurrentUserNotFound() throws Exception {
        when(userService.findUser(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldThrowBadRequestWhenAuthLoginEmailAndPasswordIsBlank() throws Exception {
        AuthLoginRequestDto dto = new AuthLoginRequestDto("", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
    }
}
