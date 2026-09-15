package com.rapidreceipt.sync;

import com.rapidreceipt.customer.CustomerResponse;
import com.rapidreceipt.invoice.InvoiceResponse;
import com.rapidreceipt.service.ServiceItemResponse;
import com.rapidreceipt.user.ProfileResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SyncResponse {
    private ProfileResponse profile;
    private List<CustomerResponse> customers;
    private List<ServiceItemResponse> services;
    private List<InvoiceResponse> invoices;
}
