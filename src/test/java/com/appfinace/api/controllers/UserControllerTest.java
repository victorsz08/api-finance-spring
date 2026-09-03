package com.appfinace.api.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.dto.user.FindUserResponseDto;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsImplService userDetailsService;

        @Test
        public void shouldCreateUserSuccessfully() throws Exception {
                mockMvc.perform(multipart("/api/users")
                                .param("email", "joao@email.com")
                                .param("name", "João")
                                .param("password", "password"))
                                .andExpect(status().isCreated());
        }

        @Test
        public void shouldReturnConflictWhenCreatingUserWithExistingEmail() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"))
                                .when(userService).createUser(any());

                mockMvc.perform(multipart("/api/users")
                                .param("email", "joao@email.com")
                                .param("name", "João")
                                .param("password", "password"))
                                .andExpect(status().isConflict());
        }

        @Test
        public void shouldReturnUserWhenFound() throws Exception {
                UUID id = UUID.randomUUID();
                FindUserResponseDto response = new FindUserResponseDto(id, "joao@email.com", "Joao", "//image");

                when(userService.findUser(id)).thenReturn(response);

                mockMvc.perform(get("/api/users/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("joao@email.com"))
                                .andExpect(jsonPath("$.name").value("Joao"));
        }

        @Test
        public void shouldThrowNotFoundUserWithId() throws Exception {
                UUID id = UUID.randomUUID();

                when(userService.findUser(id)).thenThrow(
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

                mockMvc.perform(get("/api/users/{id}", id))
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
                UUID id = UUID.randomUUID();

                mockMvc.perform(multipart("/api/users/{id}", id)
                                .param("name", "João Updated")
                                .param("email", "joao.updated@email.com")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isOk());

        }

        @Test
        public void shouldThrowConflictWhenUpdateUserWithExistingEmail() throws Exception {
                UUID id = UUID.randomUUID();

                doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado"))
                                .when(userService).updateUser(eq(id), any(), any(), any());

                mockMvc.perform(multipart("/api/users/{id}", id)
                                .param("email", "joao@email.com")
                                .param("name", "Joao")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                }))
                                .andExpect(status().isConflict());

        }

        @Test
        void shouldReturnProfileImagesByUser() throws Exception {
                UUID id = UUID.randomUUID();
                when(userService.getProfileImagesByUser(id))
                                .thenReturn(List.of(new ProfileImagesResponseDto(UUID.randomUUID(), "//image-url")));

                mockMvc.perform(get("/api/users/profile-images-user/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].profileImageUrl").value("//image-url"));
        }
}
