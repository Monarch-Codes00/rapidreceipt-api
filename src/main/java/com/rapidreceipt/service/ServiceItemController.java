package com.rapidreceipt.service;

import com.rapidreceipt.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/**
 * REST controller for managing services/products.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @PostMapping
    public ResponseEntity<ServiceItemResponse> createServiceItem(
            @Valid @RequestBody ServiceItemRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceItemService.createServiceItem(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ServiceItemResponse>> getAllServices(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(serviceItemService.getAllServiceItems(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItemResponse> getServiceItemById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(serviceItemService.getServiceItemById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemResponse> updateServiceItem(
            @PathVariable Long id,
            @Valid @RequestBody ServiceItemRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(serviceItemService.updateServiceItem(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        serviceItemService.deleteServiceItem(id, user);
        return ResponseEntity.noContent().build();
    }
}
