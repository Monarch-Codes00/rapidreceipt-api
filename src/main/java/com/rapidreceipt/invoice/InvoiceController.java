package com.rapidreceipt.invoice;

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
 * REST controller for managing invoices.
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id, user));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) throws Exception {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id, user);
        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice, user);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam InvoiceStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        invoiceService.deleteInvoice(id, user);
        return ResponseEntity.noContent().build();
    }
}
