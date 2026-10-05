package com.rapidreceipt.invoice;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.rapidreceipt.user.User;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] generateInvoicePdf(InvoiceResponse invoice, User user) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);

        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, invoice.getStatus() == InvoiceStatus.PAID ? new Color(0, 150, 0) : new Color(220, 100, 0));

        // Top Banner Table: Business Info (Left) & Invoice Header (Right)
        PdfPTable topTable = new PdfPTable(2);
        topTable.setWidthPercentage(100);
        topTable.setWidths(new float[]{60, 40});

        PdfPCell businessCell = new PdfPCell();
        businessCell.setBorder(Rectangle.NO_BORDER);
        String businessName = user.getBusinessName() != null ? user.getBusinessName() : "RapidReceipt Business";
        businessCell.addElement(new Paragraph(businessName, headerFont));
        if (user.getBusinessAddress() != null && !user.getBusinessAddress().isEmpty()) {
            businessCell.addElement(new Paragraph(user.getBusinessAddress(), font));
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            businessCell.addElement(new Paragraph("Phone: " + user.getPhone(), font));
        }
        businessCell.addElement(new Paragraph("Email: " + user.getEmail(), font));
        topTable.addCell(businessCell);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        String docTypeTitle = invoice.getDocumentType() != null ? invoice.getDocumentType().name() : "INVOICE";
        Paragraph docTitle = new Paragraph(docTypeTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(40, 40, 40)));
        docTitle.setAlignment(Element.ALIGN_RIGHT);
        headerCell.addElement(docTitle);

        Paragraph invNum = new Paragraph("#" + invoice.getInvoiceNumber(), boldFont);
        invNum.setAlignment(Element.ALIGN_RIGHT);
        headerCell.addElement(invNum);

        String statusStr = invoice.getStatus() != null ? invoice.getStatus().name() : "UNPAID";
        Paragraph statusPara = new Paragraph("Status: " + statusStr, statusFont);
        statusPara.setAlignment(Element.ALIGN_RIGHT);
        headerCell.addElement(statusPara);

        topTable.addCell(headerCell);
        document.add(topTable);

        document.add(new Paragraph(" ")); // Spacer

        // Client & Dates Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{50, 50});

        PdfPCell billToCell = new PdfPCell();
        billToCell.setBorder(Rectangle.NO_BORDER);
        billToCell.addElement(new Paragraph("Billed To:", boldFont));
        if (invoice.getCustomer() != null) {
            billToCell.addElement(new Paragraph(invoice.getCustomer().getName(), titleFont));
            if (invoice.getCustomer().getEmail() != null) billToCell.addElement(new Paragraph(invoice.getCustomer().getEmail(), font));
            if (invoice.getCustomer().getPhone() != null) billToCell.addElement(new Paragraph(invoice.getCustomer().getPhone(), font));
            if (invoice.getCustomer().getAddress() != null) billToCell.addElement(new Paragraph(invoice.getCustomer().getAddress(), font));
        }
        infoTable.addCell(billToCell);

        PdfPCell datesCell = new PdfPCell();
        datesCell.setBorder(Rectangle.NO_BORDER);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        if (invoice.getIssueDate() != null) {
            Paragraph issueP = new Paragraph("Issue Date: " + invoice.getIssueDate().format(dtf), font);
            issueP.setAlignment(Element.ALIGN_RIGHT);
            datesCell.addElement(issueP);
        }
        if (invoice.getDueDate() != null) {
            Paragraph dueP = new Paragraph("Due Date: " + invoice.getDueDate().format(dtf), font);
            dueP.setAlignment(Element.ALIGN_RIGHT);
            datesCell.addElement(dueP);
        }
        infoTable.addCell(datesCell);

        document.add(infoTable);
        document.add(new Paragraph(" "));

        // Itemized Line Items Table
        PdfPTable itemTable = new PdfPTable(4);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{50, 15, 17, 18});

        addTableHeader(itemTable, "Description");
        addTableHeader(itemTable, "Qty");
        addTableHeader(itemTable, "Unit Price");
        addTableHeader(itemTable, "Amount");

        NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.US);

        if (invoice.getItems() != null) {
            for (InvoiceItemResponse item : invoice.getItems()) {
                itemTable.addCell(createCell(item.getServiceName(), Element.ALIGN_LEFT, font));
                itemTable.addCell(createCell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER, font));
                itemTable.addCell(createCell(currencyFmt.format(item.getPrice()), Element.ALIGN_RIGHT, font));
                BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                itemTable.addCell(createCell(currencyFmt.format(itemTotal), Element.ALIGN_RIGHT, font));
            }
        }
        document.add(itemTable);

        document.add(new Paragraph(" "));

        // Summary Box Table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(40);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setWidths(new float[]{50, 50});

        addSummaryRow(summaryTable, "Subtotal:", currencyFmt.format(invoice.getSubtotal()), font);
        if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Discount:", "-" + currencyFmt.format(invoice.getDiscount()), font);
        }
        if (invoice.getTaxRate() != null && invoice.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(summaryTable, "Tax (" + invoice.getTaxRate() + "%):", currencyFmt.format(invoice.getTaxAmount()), font);
        }
        addSummaryRow(summaryTable, "Total Payable:", currencyFmt.format(invoice.getTotal()), boldFont);

        document.add(summaryTable);

        // Bank Details Footer
        if (user.getBankName() != null && !user.getBankName().isEmpty()) {
            document.add(new Paragraph(" "));
            Paragraph bankHeader = new Paragraph("Payment Information", boldFont);
            document.add(bankHeader);
            document.add(new Paragraph("Bank Name: " + user.getBankName(), font));
            if (user.getAccountNumber() != null) document.add(new Paragraph("Account Number: " + user.getAccountNumber(), font));
            if (user.getAccountName() != null) document.add(new Paragraph("Account Name: " + user.getAccountName(), font));
        }

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(Color.BLACK);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private PdfPCell createCell(String text, int align, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private void addSummaryRow(PdfPTable table, String label, String val, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(val, font));
        valCell.setBorder(Rectangle.NO_BORDER);
        valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valCell);
    }
}
