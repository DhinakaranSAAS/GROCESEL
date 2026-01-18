package com.groseloa.controller;

import com.groseloa.model.Customer;
import com.groseloa.model.User;
import com.groseloa.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private com.groseloa.service.TransactionService transactionService;

    private User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    @GetMapping
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllCustomers(HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Customer> customers = customerService.getAllCustomers(user);

        List<java.util.Map<String, Object>> dtos = customers.stream().map(c -> {
            java.util.Map<String, java.math.BigDecimal> balances = transactionService.getCustomerBalances(c.getId());
            java.util.Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", c.getId());
            dto.put("name", c.getName());
            dto.put("phone", c.getPhone());
            dto.put("advanceBalance", balances.get("advance"));
            dto.put("creditBalance", balances.get("credit"));
            return dto;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer, HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(customerService.createCustomer(customer, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id, HttpSession session) {
        User user = getUserFromSession(session);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Customer customer = customerService.getCustomer(id);
        if (customer != null && customer.getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }
}
