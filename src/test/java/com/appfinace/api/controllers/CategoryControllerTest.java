package com.appfinace.api.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryRequestDto;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.CategoryService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsImplService userDetailsImplService;

    private UUID userId;
    private UUID categoryId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldCreateCategorySuccessfully() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("Transporte", "EXPENSE");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldThrowNotFoundWhenUserNotFoundOnCreate() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("Transporte", "EXPENSE");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado com esse id"))
                .when(categoryService).createCategory(any(), eq(userId));

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
    }

    @Test
    public void shouldListCategoriesSuccessfully() throws Exception {
        CategoryResponseDto response = new CategoryResponseDto(categoryId, "Transporte", "EXPENSE");

        when(categoryService.listCategories(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Transporte"));
    }

    @Test
    public void shouldReturnEmptyListWhenUserHasNoCategories() throws Exception {
        when(categoryService.listCategories(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldFindCategoryByIdSuccessfully() throws Exception {
        CategoryResponseDto response = new CategoryResponseDto(categoryId, "Transporte", "EXPENSE");

        when(categoryService.findCategory(categoryId)).thenReturn(response);

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Transporte"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    public void shouldThrowNotFoundCatshouldThrowNotFoundWhenCategoryNotFoundOnFindegoryById() throws Exception {
        when(categoryService.findCategory(categoryId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id"));

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateCategorySuccessfully() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("Transporte", "EXPENSE");

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenUpdateCategoryNotFound() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("Alimentação", "EXPENSE");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id"))
                .when(categoryService).update(categoryId, "Alimentação", "EXPENSE");

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteCategorySuccessfully() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteCategoryNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id"))
                .when(categoryService).delete(categoryId);

        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }
}
