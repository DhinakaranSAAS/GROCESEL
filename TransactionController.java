package com.groseloa.controller;

import com.groseloa.model.Transaction;
import com.groseloa.service.TransactionService;
import com.groseloa.service.CustomerService; // Check ownership
import com.groseloa.model.Customer;
import com.groseloa.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CustomerService customerService;

    private User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    // Add Transaction (Credit/Payment)
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<Transaction> addTransaction(
            @PathVariable Long customerId,
            @RequestBody Transaction transaction,
            HttpSession session) {
        
        User user = getUserFromSession(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Verify customer belongs to user
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null || !customer.getUser().getId().equals(user.getId())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(transactionService.createTransaction(customerId, transaction));
    }

    // Get History
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable Long customerId, HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // Verify customer belongs to user
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null || !customer.getUser().getId().equals(user.getId())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(transactionService.getTransactionsByCustomer(customerId));
    }
}
