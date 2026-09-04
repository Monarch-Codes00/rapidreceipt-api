package com.rapidreceipt.customer;

import com.rapidreceipt.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing customers.
 * 
 * Key decisions:
 * - @AuthenticationPrincipal injects the User entity that was resolved by our JwtAuthenticationFilter.
 * - @Valid ensures the CustomerRequest is validated before it hits the service layer.
 * - Standard REST conventions: 201 Created for POST, 204 No Content for DELETE.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request, user));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(customerService.getAllCustomers(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(customerService.getCustomerById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        customerService.deleteCustomer(id, user);
        return ResponseEntity.noContent().build();
    }
}
