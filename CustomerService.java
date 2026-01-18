package com.groseloa.service;

import com.groseloa.model.Customer;
import com.groseloa.model.User;
import com.groseloa.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers(User user) {
        return customerRepository.findByUserId(user.getId());
    }

    public Customer getCustomer(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer createCustomer(Customer customer, User user) {
        customer.setUser(user);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Optional<Customer> existing = customerRepository.findById(id);
        if (existing.isPresent()) {
            Customer customer = existing.get();
            customer.setName(updatedCustomer.getName());
            customer.setPhone(updatedCustomer.getPhone());
            return customerRepository.save(customer);
        }
        return null;
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
