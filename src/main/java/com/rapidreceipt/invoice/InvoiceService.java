package com.rapidreceipt.invoice;

import com.rapidreceipt.common.ResourceNotFoundException;
import com.rapidreceipt.customer.Customer;
import com.rapidreceipt.customer.CustomerRepository;
import com.rapidreceipt.customer.CustomerResponse;
import com.rapidreceipt.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling business logic for Invoices.
 * 
 * Key decisions:
 * - We enforce data isolation: a user can only create an invoice for their own customer.
 * - Invoice numbering is generated sequentially per user.
 * - Subtotals and totals are computed server-side to prevent client tampering.
 * - We map the embedded Customer to a CustomerResponse to reduce frontend API calls.
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request, User user) {
        Customer customer = customerRepository.findByIdAndUserId(request.getCustomerId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found or doesn't belong to this user"));

        long nextInvoiceNumberSeq = invoiceRepository.countByUserId(user.getId()) + 1;
        String generatedNumber = generateInvoiceNumber(nextInvoiceNumberSeq);

        Invoice invoice = Invoice.builder()
                .user(user)
                .customer(customer)
                .invoiceNumber(generatedNumber)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .documentType(request.getDocumentType())
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .build();

        List<InvoiceItem> items = request.getItems().stream().map(itemReq -> 
            InvoiceItem.builder()
                    .invoice(invoice)
                    .serviceName(itemReq.getServiceName())
                    .quantity(itemReq.getQuantity())
                    .price(itemReq.getPrice())
                    .build()
        ).collect(Collectors.toList());
        
        invoice.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(i -> i.getPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setSubtotal(subtotal);
        
        BigDecimal total = subtotal.subtract(invoice.getDiscount());
        invoice.setTotal(total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total); 

        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        return mapToResponse(savedInvoice);
    }

    public List<InvoiceResponse> getAllInvoices(User user) {
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(Long id, User user) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found or doesn't belong to this user"));
        return mapToResponse(invoice);
    }
    
    @Transactional
    public void deleteInvoice(Long id, User user) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found or doesn't belong to this user"));
        invoiceRepository.delete(invoice);
    }

    private String generateInvoiceNumber(long sequence) {
        int currentYear = LocalDate.now().getYear();
        return String.format("RR-%d-%05d", currentYear, sequence);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        CustomerResponse customerResponse = CustomerResponse.builder()
                .id(invoice.getCustomer().getId())
                .name(invoice.getCustomer().getName())
                .phone(invoice.getCustomer().getPhone())
                .email(invoice.getCustomer().getEmail())
                .address(invoice.getCustomer().getAddress())
                .createdAt(invoice.getCustomer().getCreatedAt())
                .build();

        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(item -> InvoiceItemResponse.builder()
                        .id(item.getId())
                        .serviceName(item.getServiceName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .documentType(invoice.getDocumentType())
                .customer(customerResponse)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .items(itemResponses)
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .total(invoice.getTotal())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
