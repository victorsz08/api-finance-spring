package com.appfinace.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.repositories.InstallmentRepository;

@Service
public class InstallmentService {

    private final InstallmentRepository installmentRepository;

    public InstallmentService(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    public void payInstallment(UUID installmentId) {
        Installment installment = this.installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcela não localizada"));

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Parcela já está paga");
        }

        installment.setStatus(InstallmentStatus.PAID);
        this.installmentRepository.save(installment);
    }

    public List<InstallmentResponseDto> listPending(UUID userId, Integer month, Integer year) {
        List<Installment> installments = (month != null && year != null)
                ? installmentRepository.findPendingByMonth(userId, month, year)
                : installmentRepository.findAllPending(userId);

        return installments.stream()
                .map(i -> new InstallmentResponseDto(i.getId(), i.getNumber(), i.getAmount(), i.getDueDate(),
                        i.getStatus()))
                .toList();
    }
}
