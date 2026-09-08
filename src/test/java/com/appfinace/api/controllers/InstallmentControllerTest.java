package com.appfinace.api.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.InstallmentService;

@WebMvcTest(InstallmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class InstallmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private InstallmentService installmentService;

    @MockitoBean
    private UserDetailsImplService userDetailsImplService;

    private UUID userId;
    private UUID installmentId;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        installmentId = UUID.randomUUID();

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
    public void shouldPayInstallmentSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/installments/{id}/pay", installmentId))
                .andExpect(status().isOk());

        verify(installmentService).payInstallment(installmentId, userId);
    }

    @Test
    public void shouldThrowNotFoundWhenPayInstallmentNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela não localizada"))
                .when(installmentService).payInstallment(installmentId, userId);

        mockMvc.perform(patch("/api/installments/{id}/pay", installmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldThrowConflictPayInstallmentAlreadyPaid() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Parcela já está paga"))
                .when(installmentService).payInstallment(installmentId, userId);

        mockMvc.perform(patch("/api/installments/{id}/pay", installmentId))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldListInstallmentsPendingWithMothAndYearSuccessfully() throws Exception {
        InstallmentResponseDto dto = new InstallmentResponseDto(
                installmentId, 1, new BigDecimal("100.00"), LocalDate.of(2015, 10, 10), InstallmentStatus.PENDING);

        when(installmentService.listPending(userId, 10, 2026)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/installments/pending")
                .param("month", "10")
                .param("year", "2026")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(1));
    }

    @Test
    public void shouldListAllPendingInstallmentsWithUserId() throws Exception {
        InstallmentResponseDto dto = new InstallmentResponseDto(
                installmentId, 1, new BigDecimal("100.00"), LocalDate.of(2015, 10, 10), InstallmentStatus.PENDING);

        when(installmentService.listPending(userId, null, null))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/installments/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void shouldReturnEmptyListWhenNoPendingInstallments() throws Exception {
        when(installmentService.listPending(userId, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/installments/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void shouldThrowBadRequestListAllPendingWhenOnlyMonthProvided() throws Exception {
        when(installmentService.listPending(userId, 3, null))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Informe mês e ano juntos, ou nenhum dos dois"));

        mockMvc.perform(get("/api/installments/pending")
                .param("month", "3")).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldThrowBadRequestListAllPendingWhenOnlyYearProvided() throws Exception {
        when(installmentService.listPending(userId, null, 2026))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Informe mês e ano juntos, ou nenhum dos dois"));

        mockMvc.perform(get("/api/installments/pending")
                .param("year", "2026"))
                .andExpect(status().isBadRequest());
    }
}
