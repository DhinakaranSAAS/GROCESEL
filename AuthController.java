package com.groseloa.controller;

import com.groseloa.model.User;
import com.groseloa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest,
            jakarta.servlet.http.HttpSession session) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("shopName", user.getShopName());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String type = registerRequest.get("type"); // admin or customer

        if ("admin".equalsIgnoreCase(type)) {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String shopName = registerRequest.get("shopName");

            if (username == null || password == null || shopName == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Check if user exists (simple check via login fail is basic, better to have
            // existsByUsername)
            // For MVP, we catch generic error or just try save
            try {
                User newUser = new User(username, password, shopName);
                userService.saveUser(newUser);
                return ResponseEntity.ok(Map.of("message", "Registration successful"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
            }
        } else if ("customer".equalsIgnoreCase(type)) {
            // As per current data model, Customers must belong to a Shopkeeper (User).
            // Standalone Customer registration is not fully supported by DB schema
            // (requires user_id).
            // Returning specific message as per User Request to "do it for login page only"
            // implementation
            return ResponseEntity.ok(
                    Map.of("message", "Customer registration request received. Please contact your local shopkeeper."));
        }

        return ResponseEntity.badRequest().body("Invalid account type");
    }
}
