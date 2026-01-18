package com.groseloa.service;

import com.groseloa.model.User;
import com.groseloa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Simple login check with SHA-256 hashing
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(hashPassword(password))) {
                return user;
            }
        }
        return null; // Login failed
    }

    public User saveUser(User user) {
        // Hash password before saving if it's not already hashed (simple check length,
        // though flawed, sufficient for this demo)
        // Better: Assumes input user always has raw password
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

}
