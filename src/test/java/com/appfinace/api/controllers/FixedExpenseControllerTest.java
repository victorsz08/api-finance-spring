package com.appfinace.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
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
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.FixedExpenseService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FixedExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class FixedExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FixedExpenseService fixedExpenseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsImplService userDetailsImplService;

    private UUID userId;
    private UUID fixedExpenseId;
    private UUID categoryId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        fixedExpenseId = UUID.randomUUID();
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
    public void shouldCreateFixedExpenseSuccessfully() throws Exception {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto(
                "Aluguel", new BigDecimal("425.00"), 10, categoryId);

        mockMvc.perform(post("/api/fixed-expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated());
    }

    @Test
    public void shouldThrowNotFoundWhenCreateFixedExpenseWithInvalidCategory() throws Exception {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto(
                "Aluguel", new BigDecimal("425.00"), 10, categoryId);

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada"))
                .when(fixedExpenseService).create(any(), eq(userId));

        mockMvc.perform(post("/api/fixed-expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
    }

    @Test
    public void shouldListFiltredFixedExpensesSuccessfully() throws Exception {
        CategoryResponseDto categoryDto = new CategoryResponseDto(categoryId, "Moradia", "EXPENSE");
        FixedExpenseResponseDto expenseDto = new FixedExpenseResponseDto(
                fixedExpenseId, "Aluguel", new BigDecimal("425.00"), 10, true, categoryDto);

        when(fixedExpenseService.listByFiltred(
                0, 10, null, null, null, null, null, null, userId)).thenReturn(List.of(expenseDto));

        mockMvc.perform(get("/api/fixed-expenses/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Aluguel"));
    }

    @Test
    public void shouldListFiltredFixedExpensesWithFiltersSuccessfully() throws Exception {
        when(fixedExpenseService.listByFiltred(
                0,
                10,
                new BigDecimal("1000"),
                new BigDecimal("2000"),
                5,
                20,
                true,
                categoryId,
                userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/fixed-expenses/filter")
                .param("startAmount", "1000")
                .param("endAmount", "2000")
                .param("startDueDay", "5")
                .param("endDueDay", "20")
                .param("active", "true")
                .param("categoryId", categoryId.toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldFindFixedExpenseByIdSuccessfully() throws Exception {
        CategoryResponseDto categoryDto = new CategoryResponseDto(categoryId, "Moradia", "EXPENSE");
        FixedExpenseResponseDto expenseDto = new FixedExpenseResponseDto(
                fixedExpenseId, "Aluguel", new BigDecimal("425.00"), 10, true, categoryDto);

        when(fixedExpenseService.findById(fixedExpenseId)).thenReturn(expenseDto);

        mockMvc.perform(get("/api/fixed-expenses/{id}", fixedExpenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Aluguel"))
                .andExpect(jsonPath("$.category.name").value("Moradia"));
    }

    @Test
    public void shouldThrowNotFoundWhenFixedExpenseNotFoundOnFind() throws Exception {
        when(fixedExpenseService.findById(fixedExpenseId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"));

        mockMvc.perform(get("/api/fixed-expenses/{id}", fixedExpenseId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateFixedExpenseSuccessfully() throws Exception {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("Transporte", new BigDecimal("400.00"), 15, categoryId);

        mockMvc.perform(put("/api/fixed-expenses/{id}", fixedExpenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenUpdateFixedExpenseNotFound() throws Exception {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("Transporte", new BigDecimal("400.00"), 15, categoryId);

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"))
                .when(fixedExpenseService).update(eq(fixedExpenseId), any());

        mockMvc.perform(put("/api/fixed-expenses/{id}", fixedExpenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateActiveStatusSuccessfully() throws Exception {
        mockMvc.perform(put("/api/fixed-expenses/active/{id}", fixedExpenseId)
                .param("active", "false")).andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenUpdateActiveFixedExpenseNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"))
                .when(fixedExpenseService).updateActive(eq(fixedExpenseId), any());

        mockMvc.perform(put("/api/fixed-expenses/active/{id}", fixedExpenseId)
                .param("active", "false")).andExpect(status().isNotFound());
    }

    @Test
    public void shouldMarkFixedExpenseAsPaidSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/fixed-expenses/{id}/pay", fixedExpenseId))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenMarkAsPaidFixedExpenseNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"))
                .when(fixedExpenseService).markAsPaid(fixedExpenseId, userId);

        mockMvc.perform(patch("/api/fixed-expenses/{id}/pay", fixedExpenseId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldThrowConflictWhenMarkAsPaidFixedExpenseAlreadyInactive() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Despesa já está inativa"))
                .when(fixedExpenseService).markAsPaid(fixedExpenseId, userId);

        mockMvc.perform(patch("/api/fixed-expenses/{id}/pay", fixedExpenseId))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldDeleteFixedExpenseSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/fixed-expenses/{id}", fixedExpenseId))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldThrowNotFoundWhenDeleteFixedExpenseNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"))
                .when(fixedExpenseService).delete(fixedExpenseId);

        mockMvc.perform(delete("/api/fixed-expenses/{id}", fixedExpenseId))
                .andExpect(status().isNotFound());
    }
}
