package com.emredeniz.demo.service;

import com.emredeniz.demo.util.IBANValidator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    private PDFDownloaderService pdfDownloaderService;

    @Mock
    private IBANValidator ibanValidator;

    @InjectMocks
    private InvoiceScannerService invoiceScannerService;

    @Test
    public void testScanInvoiceForBlacklistedIBANs_NoBlacklistedIBANs() throws IOException {
        // Mock the PDF document
        PDDocument mockDocument = new PDDocument();
        when(pdfDownloaderService.downloadPDF(anyString())).thenReturn(mockDocument);

        // Mock the IBAN validator to return an empty list (no blacklisted IBANs)
        when(ibanValidator.findBlacklistedIBANs(anyString())).thenReturn(Set.of());

        // Test the method
        String url = "classpath:example_invoice.pdf";
        Set<String> blacklistedIBANs = invoiceScannerService.scanInvoiceForBlacklistedIBANs(url);

        // Verify the result
        assertTrue(blacklistedIBANs.isEmpty());

        // Verify interactions
        verify(pdfDownloaderService, times(1)).downloadPDF(url);
        verify(ibanValidator, times(1)).findBlacklistedIBANs(anyString());

        // Close the mock document
        mockDocument.close();
    }

    @Test
    public void testScanInvoiceForBlacklistedIBANs_BlacklistedIBANsFound() throws IOException {
        // Mock the PDF document
        PDDocument mockDocument = new PDDocument();
        when(pdfDownloaderService.downloadPDF(anyString())).thenReturn(mockDocument);

        // Mock the IBAN validator to return a list of blacklisted IBANs
        when(ibanValidator.findBlacklistedIBANs(anyString())).thenReturn(Set.of("DE89370400440532013000", "GB29NWBK60161331926819"));

        // Test the method
        String url = "classpath:example_invoice.pdf";
        Set<String> blacklistedIBANs = invoiceScannerService.scanInvoiceForBlacklistedIBANs(url);

        // Verify the result
        assertEquals(2, blacklistedIBANs.size());
        assertTrue(blacklistedIBANs.contains("DE89370400440532013000"));
        assertTrue(blacklistedIBANs.contains("GB29NWBK60161331926819"));

        // Verify interactions
        verify(pdfDownloaderService, times(1)).downloadPDF(url);
        verify(ibanValidator, times(1)).findBlacklistedIBANs(anyString());

        // Close the mock document
        mockDocument.close();
    }

    @Test
    public void testScanInvoiceForBlacklistedIBANs_InvalidPDFUrl() throws IOException {
        // Mock the PDF downloader to throw an exception for an invalid URL
        when(pdfDownloaderService.downloadPDF(anyString())).thenThrow(new IOException("File not found"));

        // Test the method
        String url = "classpath:invalid_invoice.pdf";
        Exception exception = assertThrows(IOException.class, () -> invoiceScannerService.scanInvoiceForBlacklistedIBANs(url));

        // Verify the exception message
        assertTrue(exception.getMessage().contains("File not found"));

        // Verify interactions
        verify(pdfDownloaderService, times(1)).downloadPDF(url);
        verify(ibanValidator, never()).findBlacklistedIBANs(anyString());
    }

    @Test
    public void testScanInvoiceForBlacklistedIBANs_EmptyPDF() throws IOException {
        // Mock an empty PDF document
        PDDocument mockDocument = new PDDocument();
        when(pdfDownloaderService.downloadPDF(anyString())).thenReturn(mockDocument);

        // Mock the IBAN validator to return an empty list (no text to scan)
        when(ibanValidator.findBlacklistedIBANs(anyString())).thenReturn(Set.of());

        // Test the method
        String url = "classpath:empty_invoice.pdf";
        Set<String> blacklistedIBANs = invoiceScannerService.scanInvoiceForBlacklistedIBANs(url);

        // Verify the result
        assertTrue(blacklistedIBANs.isEmpty());

        // Verify interactions
        verify(pdfDownloaderService, times(1)).downloadPDF(url);
        verify(ibanValidator, times(1)).findBlacklistedIBANs(anyString());

        // Close the mock document
        mockDocument.close();
    }
}
