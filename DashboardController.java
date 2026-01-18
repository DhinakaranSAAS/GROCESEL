package com.groseloa.controller;

import com.groseloa.model.Transaction;
import com.groseloa.model.User;
import com.groseloa.repository.CustomerRepository;
import com.groseloa.repository.TransactionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        long totalCustomers = customerRepository.countByUserId(user.getId());
        // Get recent 5 transactions
        List<Transaction> allTransactions = transactionRepository
                .findByCustomerUserIdOrderByTimestampDesc(user.getId());

        BigDecimal totalOutstanding = allTransactions.stream()
                .map(t -> {
                    if (t.getType() == com.groseloa.model.Transaction.TransactionType.CREDIT_ADD)
                        return t.getAmount();
                    if (t.getType() == com.groseloa.model.Transaction.TransactionType.PAYMENT)
                        return t.getAmount().negate();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdvance = allTransactions.stream()
                .map(t -> {
                    if (t.getType() == com.groseloa.model.Transaction.TransactionType.ADVANCE_ADD)
                        return t.getAmount();
                    if (t.getType() == com.groseloa.model.Transaction.TransactionType.PURCHASE)
                        return t.getAmount().negate();
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> recentTransactions = allTransactions.stream().limit(5).collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", totalCustomers);
        summary.put("totalOutstanding", totalOutstanding);
        summary.put("totalAdvance", totalAdvance);
        summary.put("recentTransactions", recentTransactions);

        return ResponseEntity.ok(summary);
    }
}
