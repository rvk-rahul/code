package com.emredeniz.demo.service;

import com.emredeniz.demo.util.IBANValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
public class InvoiceScannerService {

    private final PDFDownloaderService pdfDownloaderService;
    private final IBANValidator ibanValidator;

    public InvoiceScannerService(PDFDownloaderService pdfDownloaderService, IBANValidator ibanValidator) {
        this.pdfDownloaderService = pdfDownloaderService;
        this.ibanValidator = ibanValidator;
    }

    public Set<String> scanInvoiceForBlacklistedIBANs(String url) throws IOException {
        try (PDDocument document = pdfDownloaderService.downloadPDF(url)) {
            if (document == null) {
                throw new IOException("Failed to load PDF document from URL: " + url);
            }

            // Extract text from the PDF
            String text = new PDFTextStripper().getText(document);

            // Find all IBANs in the text and filter for blacklisted ones
            return ibanValidator.findBlacklistedIBANs(text);
        }
    }
}
