package com.appfinace.api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseRequestDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseResponseDto;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.infra.config.SecurityConfig;
import com.appfinace.api.infra.security.JwtService;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.infra.security.UserDetailsImplService;
import com.appfinace.api.service.InstallmentPurchaseService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(InstallmentPurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class InstallmentPurchaseControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private InstallmentPurchaseService purchaseService;

        @MockitoBean
        private UserDetailsImplService userDetailsImplService;

        private UUID userId;
        private UUID categoryId;
        private UUID purchaseId;
        private UUID installmentId;

        @BeforeEach
        public void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                purchaseId = UUID.randomUUID();
                installmentId = UUID.randomUUID();

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
        public void shouldCreateInstallmentPurchaseSuccessfully() throws Exception {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("3000.00"), 10, LocalDate.of(2026, 9, 6), categoryId);

                mockMvc.perform(post("/api/installment-purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated());
        }

        @Test
        public void shouldThrowBadRequestWhenCreateInstallmentPurchaseNotBlank() throws Exception {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "", new BigDecimal("3000.00"), 10, LocalDate.of(2026, 9, 6), categoryId);

                mockMvc.perform(post("/api/installment-purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());

                verify(purchaseService, never()).create(any(), any());
        }

        @Test
        public void shouldThrowBadRequestWhenCreateInstallmentPurchaseNegativeAndNull() throws Exception {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("-3000.00"), 0, null, categoryId);

                mockMvc.perform(post("/api/installment-purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());

                verify(purchaseService, never()).create(any(), any());
        }

        @Test
        public void shouldThrowNotFoundWhenCreateInstallmentPurchaseInvalidCategory() throws Exception {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("3000.00"), 10, LocalDate.of(2026, 9, 6), categoryId);

                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizado"))
                                .when(purchaseService).create(any(), eq(userId));

                mockMvc.perform(post("/api/installment-purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))).andExpect(status().isNotFound());
        }

        @Test
        public void shouldListFiltredInstallmentPurchasesSuccessfully() throws Exception {
                CategoryResponseDto categoryDto = new CategoryResponseDto(categoryId, "Eletronicos", "EXPENSE");
                InstallmentResponseDto installmentDto = new InstallmentResponseDto(
                                installmentId, 1, new BigDecimal("300.00"), LocalDate.of(2026, 10, 1),
                                InstallmentStatus.PENDING);
                InstallmentPurchaseResponseDto purchaseDto = new InstallmentPurchaseResponseDto(
                                purchaseId, "Fone de Ouvido", new BigDecimal("300.00"), 1,
                                LocalDate.of(2026, 9, 1), categoryDto, List.of(installmentDto));

                when(purchaseService.listByUser(0, 10, null, null, null, userId, null))
                                .thenReturn(List.of(purchaseDto));

                mockMvc.perform(get("/api/installment-purchase")
                                .param("page", "0")
                                .param("size", "10")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].description").value("Fone de Ouvido"))
                                .andExpect(jsonPath("$[0].category.name").value("Eletronicos"))
                                .andExpect(jsonPath("$[0].installments.length()").value(1));
        }

        @Test
        public void shouldListFiltredInstallmentPurchasesWithFiltersSuccessfully() throws Exception {
                when(purchaseService.listByUser(0, 10, categoryId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 1),
                                userId,
                                true))
                                .thenReturn(List.of());

                mockMvc.perform(get("/api/installment-purchase")
                                .param("page", "0")
                                .param("size", "10")
                                .param("categoryId", categoryId.toString())
                                .param("startDate", LocalDate.of(2026, 1, 1).toString())
                                .param("endDate", LocalDate.of(2026, 9, 1).toString())
                                .param("onlyOpen", "true")).andExpect(status().isOk());
        }

        @Test
        public void shouldFindInstallmentPurchaseByIdSuccessfully() throws Exception {
                CategoryResponseDto categoryDto = new CategoryResponseDto(categoryId, "Eletronicos", "EXPENSE");
                InstallmentPurchaseResponseDto purchaseDto = new InstallmentPurchaseResponseDto(
                                purchaseId, "Fone de Ouvido", new BigDecimal("300.00"), 1,
                                LocalDate.of(2026, 9, 1), categoryDto, List.of());

                when(purchaseService.findOne(purchaseId)).thenReturn(purchaseDto);

                mockMvc.perform(get("/api/installment-purchase/{id}", purchaseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.description").value("Fone de Ouvido"))
                                .andExpect(jsonPath("$.category.name").value("Eletronicos"));
        }

        @Test
        public void shouldThrowNotFoundWhenInstallmentPurchaseNotFindOnFind() throws Exception {
                when(purchaseService.findOne(purchaseId))
                                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"));

                mockMvc.perform(get("/api/installment-purchase/{id}", purchaseId))
                                .andExpect(status().isNotFound());
        }

        @Test
        public void shouldDeleteInstallmentPurchaseSuccessfully() throws Exception {
                mockMvc.perform(delete("/api/installment-purchase/{id}", purchaseId))
                                .andExpect(status().isOk());
        }

        @Test
        public void shouldThrowNotFoundWhenInstallmentPurchaseNotFoundOnDelete() throws Exception {
                doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"))
                                .when(purchaseService).delete(purchaseId);

                mockMvc.perform(delete("/api/installment-purchase/{id}", purchaseId))
                                .andExpect(status().isNotFound());
        }
}
