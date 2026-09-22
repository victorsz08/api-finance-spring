package com.appfinace.api.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.dto.user.UserResponseDto;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UpdatePasswordRequestDto;
import com.appfinace.api.dto.user.UpdateUserRequestDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.UserService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsImplService userDetailsService;

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
        public void shouldCreateUserSuccessfully() throws Exception {
                mockMvc.perform(multipart("/api/users")
                                .param("email", "joao@email.com")
                                .param("name", "João")
                                .param("password", "Password123@"))
                                .andExpect(status().isCreated());

                ArgumentCaptor<UserRequestDto> captor = ArgumentCaptor.forClass(UserRequestDto.class);
                verify(userService).createUser(captor.capture());

                UserRequestDto saved = captor.getValue();
                assertThat(saved.name()).isEqualTo("João");
                assertThat(saved.email()).isEqualTo("joao@email.com");
                assertThat(saved.password()).isEqualTo("Password123@");
        }

        @Test
        public void shouldThrowBadRequestWhenCreateUserNotBlankNameAndEmail() throws Exception {
                mockMvc.perform(multipart("/api/users")
                                .param("email", "")
                                .param("name", "")
                                .param("password", "Password123@"))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any());
        }

        @Test
        public void shouldThrowBadRequestWhenCreateUserInvalidPasswordRequired() throws Exception {
                mockMvc.perform(multipart("/api/users")
                                .param("email", "Joao")
                                .param("name", "joao@email.com")
                                .param("password", "invalid"))
                                .andExpect(status().isBadRequest());

                verify(userService, never()).createUser(any());
        }

        @Test
        public void shouldReturnConflictWhenCreatingUserWithExistingEmail() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"))
                                .when(userService).createUser(any());

                mockMvc.perform(multipart("/api/users")
                                .param("email", "joao@email.com")
                                .param("name", "João")
                                .param("password", "Password123@"))
                                .andExpect(status().isConflict());

                ArgumentCaptor<UserRequestDto> captor = ArgumentCaptor.forClass(UserRequestDto.class);
                verify(userService).createUser(captor.capture());

                UserRequestDto saved = captor.getValue();
                assertThat(saved.name()).isEqualTo("João");
                assertThat(saved.email()).isEqualTo("joao@email.com");
                assertThat(saved.password()).isEqualTo("Password123@");
        }

        @Test
        public void shouldReturnUserWhenFound() throws Exception {
                UserResponseDto response = new UserResponseDto(userId, "joao@email.com", "Joao", "//image");

                when(userService.findUser(userId)).thenReturn(response);

                mockMvc.perform(get("/api/users/{id}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("joao@email.com"))
                                .andExpect(jsonPath("$.name").value("Joao"));
        }

        @Test
        public void shouldThrowNotFoundUserWithId() throws Exception {
                when(userService.findUser(userId)).thenThrow(
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

                mockMvc.perform(get("/api/users/{id}", userId))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void shouldReturnListFiltredUsersSuccessfully() throws Exception {
                when(userService.listUsers(0, 10, "joao", "joao")).thenReturn(List.of());

                mockMvc.perform(get("/api/users/filter")
                                .param("page", "0")
                                .param("size", "10")
                                .param("name", "joao")
                                .param("email", "joao"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        public void shouldUpdateUserSuccessfully() throws Exception {
                mockMvc.perform(multipart("/api/users/{id}", userId)
                                .param("name", "João Updated")
                                .param("email", "joao.updated@email.com")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isOk());

                ArgumentCaptor<UpdateUserRequestDto> captor = ArgumentCaptor.forClass(UpdateUserRequestDto.class);
                verify(userService).updateUser(eq(userId), captor.capture());

                UpdateUserRequestDto saved = captor.getValue();
                assertThat(saved.email()).isEqualTo("joao.updated@email.com");
                assertThat(saved.name()).isEqualTo("João Updated");
        }

        @Test
        public void shouldThrowConflictWhenUpdateUserWithExistingEmail() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"))
                                .when(userService).updateUser(eq(userId), any());

                mockMvc.perform(multipart("/api/users/{id}", userId)
                                .param("email", "joao@email.com")
                                .param("name", "Joao")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isConflict());
        }

        @Test
        public void shouldBadRequestWhenUpdateUserNotBlankNameAndEmail() throws Exception {
                mockMvc.perform(multipart("/api/users/{id}", userId)
                                .param("email", "")
                                .param("name", "")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                })).andExpect(status().isBadRequest());

                verify(userService, never()).updateUser(any(), any());
        }

        @Test
        void shouldReturnProfileImagesByUser() throws Exception {
                when(userService.getProfileImagesByUser(userId))
                                .thenReturn(List.of(new ProfileImagesResponseDto(UUID.randomUUID(), "//image-url")));

                mockMvc.perform(get("/api/users/profile-images-user/{id}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].profileImageUrl").value("//image-url"));
        }

        @Test
        public void shouldUpdateUserPasswordSuccessfully() throws Exception {
                UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("CurrentPassword123@", "NewPassword123@");

                userService.updatePassword(userId, dto);

                mockMvc.perform(put("/api/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk());

        }

        @Test
        public void shouldThrowNotFoundWhenUpdateUserPasswordUserNotFound() throws Exception {
                UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("CurrentPassword123@", "NewPassword123@");

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado"))
                                .when(userService).updatePassword(eq(userId), any());

                mockMvc.perform(put("/api/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
        }

        @Test
        public void shouldThrowBadRequestWhenUpdatePasswordInvalidNewPassword() throws Exception {
                UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("CurrentPassword123@", "invalid");

                mockMvc.perform(put("/api/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());

                verify(userService, never()).updatePassword(any(), any());
        }

        @Test
        public void shouldBadRequestWhenUpdateUserPasswordIncorrectCurrentPassword() throws Exception {
                UpdatePasswordRequestDto dto = new UpdatePasswordRequestDto("IncorrectPass123@", "NewPassword123@");

                doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta"))
                                .when(userService).updatePassword(eq(userId), any());

                mockMvc.perform(put("/api/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
        }
}
