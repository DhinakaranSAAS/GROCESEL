package com.groseloa.service;

import com.groseloa.model.Customer;
import com.groseloa.model.Transaction;
import com.groseloa.model.Transaction.TransactionType;
import com.groseloa.repository.CustomerRepository;
import com.groseloa.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Transaction createTransaction(Long customerId, Transaction transaction) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        List<Transaction> history = transactionRepository.findByCustomerId(customerId);

        BigDecimal advanceBalance = BigDecimal.ZERO;
        BigDecimal creditBalance = BigDecimal.ZERO;

        for (Transaction t : history) {
            switch (t.getType()) {
                case ADVANCE_ADD:
                    advanceBalance = advanceBalance.add(t.getAmount());
                    break;
                case PURCHASE:
                    advanceBalance = advanceBalance.subtract(t.getAmount());
                    break;
                case CREDIT_ADD:
                    creditBalance = creditBalance.add(t.getAmount());
                    break;
                case PAYMENT:
                    creditBalance = creditBalance.subtract(t.getAmount());
                    break;
            }
        }

        // Validate Business Rules
        if (transaction.getType() == TransactionType.PURCHASE) {
            if (advanceBalance.subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Insufficient Advance Balance. Current: " + advanceBalance);
            }
        }

        if (transaction.getType() == TransactionType.PAYMENT) {
            if (transaction.getAmount().compareTo(creditBalance) > 0) {
                throw new RuntimeException("Payment exceeds Credit Due. Current Debt: " + creditBalance);
            }
        }

        transaction.setCustomer(customer);
        transaction.setTimestamp(java.time.LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    // Helper to get Balances for UI (DTO)
    public Map<String, BigDecimal> getCustomerBalances(Long customerId) {
        List<Transaction> history = transactionRepository.findByCustomerId(customerId);
        BigDecimal advance = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;

        for (Transaction t : history) {
            switch (t.getType()) {
                case ADVANCE_ADD:
                    advance = advance.add(t.getAmount());
                    break;
                case PURCHASE:
                    advance = advance.subtract(t.getAmount());
                    break;
                case CREDIT_ADD:
                    credit = credit.add(t.getAmount());
                    break;
                case PAYMENT:
                    credit = credit.subtract(t.getAmount());
                    break;
            }
        }
        return Map.of("advance", advance, "credit", credit);
    }

    public List<Transaction> getTransactionsByCustomer(Long customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }
}
