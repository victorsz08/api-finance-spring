package com.appfinace.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.repositories.InstallmentRepository;
import com.appfinace.api.repositories.TransactionRepository;

@Service
public class InstallmentService {

        private final InstallmentRepository installmentRepository;
        private final TransactionRepository transactionRepository;

        public InstallmentService(InstallmentRepository installmentRepository,
                        TransactionRepository transactionRepository) {
                this.installmentRepository = installmentRepository;
                this.transactionRepository = transactionRepository;
        }

        public void payInstallment(UUID installmentId, UUID userId) {
                Installment installment = this.installmentRepository.findByIdAndPurchaseUserId(installmentId, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Parcela não localizada"));

                if (installment.getStatus() == InstallmentStatus.PAID) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Parcela já está paga");
                }

                installment.setStatus(InstallmentStatus.PAID);
                this.installmentRepository.save(installment);

                Transaction transaction = new Transaction();

                transaction.setDescription(
                                installment.getPurchase().getDescription() + " - parcela " + installment.getNumber()
                                                + "/"
                                                + installment.getPurchase().getTotalInstallments());
                transaction.setAmount(installment.getAmount());
                transaction.setDate(LocalDate.now());
                transaction.setType(TransactionType.EXPENSE);
                transaction.setUser(installment.getPurchase().getUser());
                transaction.setCategory(installment.getPurchase().getCategory());
                transaction.setInstallment(installment);

                this.transactionRepository.save(transaction);
        }

        public List<InstallmentResponseDto> listPending(UUID userId, Integer month, Integer year) {
                List<Installment> installments = (month != null && year != null)
                                ? installmentRepository.findPendingByMonth(userId, month, year)
                                : installmentRepository.findAllPending(userId);

                return installments.stream()
                                .map(i -> new InstallmentResponseDto(i.getId(), i.getNumber(), i.getAmount(),
                                                i.getDueDate(),
                                                i.getStatus()))
                                .toList();
        }
}
