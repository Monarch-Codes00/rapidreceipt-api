package com.rapidreceipt.service;

import com.rapidreceipt.common.ResourceNotFoundException;
import com.rapidreceipt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling business logic for ServiceItem entities.
 * 
 * Key decisions:
 * - Same user scoping logic as CustomerService.
 * - Enforces horizontal data isolation so owners only see/update their own services.
 */
@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemResponse createServiceItem(ServiceItemRequest request, User user) {
        ServiceItem item = ServiceItem.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        ServiceItem saved = serviceItemRepository.save(item);
        return mapToResponse(saved);
    }

    public List<ServiceItemResponse> getAllServiceItems(User user) {
        return serviceItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceItemResponse getServiceItemById(Long id, User user) {
        ServiceItem item = serviceItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));
        return mapToResponse(item);
    }

    public ServiceItemResponse updateServiceItem(Long id, ServiceItemRequest request, User user) {
        ServiceItem item = serviceItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());

        ServiceItem updated = serviceItemRepository.save(item);
        return mapToResponse(updated);
    }

    public void deleteServiceItem(Long id, User user) {
        ServiceItem item = serviceItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with id: " + id));
        serviceItemRepository.delete(item);
    }

    private ServiceItemResponse mapToResponse(ServiceItem item) {
        return ServiceItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
