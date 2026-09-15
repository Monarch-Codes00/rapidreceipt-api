package com.rapidreceipt.sync;

import com.rapidreceipt.customer.CustomerService;
import com.rapidreceipt.invoice.InvoiceService;
import com.rapidreceipt.service.ServiceItemService;
import com.rapidreceipt.user.ProfileService;
import com.rapidreceipt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final ProfileService profileService;
    private final CustomerService customerService;
    private final ServiceItemService serviceItemService;
    private final InvoiceService invoiceService;

    @GetMapping("/pull")
    public ResponseEntity<SyncResponse> pullAllData(@AuthenticationPrincipal User user) {
        SyncResponse response = SyncResponse.builder()
                .profile(profileService.getProfile(user))
                .customers(customerService.getAllCustomers(user))
                .services(serviceItemService.getAllServiceItems(user))
                .invoices(invoiceService.getAllInvoices(user))
                .build();
                
        return ResponseEntity.ok(response);
    }
}
