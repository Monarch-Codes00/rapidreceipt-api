package com.rapidreceipt.customer;

import com.rapidreceipt.common.ResourceNotFoundException;
import com.rapidreceipt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service handling business logic for Customer entities.
 * 
 * Key decisions:
 * - We always pass the authenticated User object from the controller.
 * - All queries and modifications explicitly scope to user.getId() via the repository.
 * - This guarantees horizontal data isolation (multi-tenancy safety) — one business
 *   owner can never read or modify another owner's customers.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request, User user) {
        Customer customer = Customer.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public Page<CustomerResponse> getAllCustomers(User user, Pageable pageable) {
        return customerRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    public CustomerResponse getCustomerById(Long id, User user) {
        Customer customer = customerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request, User user) {
        Customer customer = customerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    public void deleteCustomer(Long id, User user) {
        Customer customer = customerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
