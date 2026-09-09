package com.appfinace.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.transaction.MonthlySummaryResponseDto;
import com.appfinace.api.dto.transaction.TransactionRequestDto;
import com.appfinace.api.dto.transaction.TransactionResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.TransactionService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class TransactionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsImplService userDetailsImplService;

        @MockitoBean
        private TransactionService transactionService;

        private UUID userId;
        private UUID categoryId;
        private UUID transactionId;

        @BeforeEach
        public void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                transactionId = UUID.randomUUID();

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
        public void shouldCreateTransactionSuccessfully() throws Exception {
                TransactionRequestDto dto = new TransactionRequestDto(
                                "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 15),
                                categoryId);

                mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated());
        }

        @Test
        public void shouldThrowNotFoundWhenCategoryNotFoundOnCreate() throws Exception {
                TransactionRequestDto dto = new TransactionRequestDto(
                                "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 15),
                                categoryId);

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada"))
                                .when(transactionService).create(any(), any());

                mockMvc.perform(post("/api/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
        }

        @Test
        public void shouldListTransactionsByMonthSuccessfully() throws Exception {
                CategoryResponseDto categoryDto = new CategoryResponseDto(categoryId, "Alimentação", "EXPENSE");
                TransactionResponseDto transactionDto = new TransactionResponseDto(
                                transactionId, "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE,
                                LocalDate.of(2026, 3, 15),
                                categoryDto);

                when(transactionService.listByMonth(3, 2026, userId))
                                .thenReturn(List.of(transactionDto));

                mockMvc.perform(get("/api/transactions")
                                .param("month", "3")
                                .param("year", "2026"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].description").value("Mercado"));
        }

        @Test
        public void shouldReturnEmptyListWhenNoTransactionsInMonth() throws Exception {
                when(transactionService.listByMonth(3, 2026, userId))
                                .thenReturn(List.of());

                mockMvc.perform(get("/api/transactions")
                                .param("month", "3")
                                .param("year", "2026")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(0));

        }

        @Test
        public void shouldGetMonthlySummarySuccessfully() throws Exception {
                MonthlySummaryResponseDto summary = new MonthlySummaryResponseDto(
                                3, 2026, new BigDecimal("1000.00"), new BigDecimal("500.00"), new BigDecimal("500.00"));

                when(transactionService.getMonthlySummary(3, 2026, userId))
                                .thenReturn(summary);

                mockMvc.perform(get("/api/transactions/summary")
                                .param("month", "3")
                                .param("year", "2026")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance").value(500.00))
                                .andExpect(jsonPath("$.totalIncome").value(1000.00))
                                .andExpect(jsonPath("$.totalExpense").value(500.00));
        }

        @Test
        public void shouldReturnBadRequestWhenMonthMissingOnSummary() throws Exception {
                mockMvc.perform(get("/api/transactions/summary")
                                .param("month", "3")).andExpect(status().isBadRequest());
        }
}
