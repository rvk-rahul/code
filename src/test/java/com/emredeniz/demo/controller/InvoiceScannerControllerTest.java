package com.emredeniz.demo.controller;

import com.emredeniz.demo.service.InvoiceScannerService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class InvoiceScannerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InvoiceScannerService invoiceScannerService;

    @InjectMocks
    private InvoiceScannerController invoiceScannerController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(invoiceScannerController).build();
    }

    @Test
    public void testScanInvoice_NoBlacklistedIBANs() throws Exception {
        when(invoiceScannerService.scanInvoiceForBlacklistedIBANs(anyString())).thenReturn(Set.of());

        mockMvc.perform(post("/api/invoices/scan")
                        .param("url", "classpath:example_invoice.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No blacklisted IBANs found."))
                .andExpect(jsonPath("$.blacklistedIbans").isEmpty())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    public void testScanInvoice_BlacklistedIBANsFound() throws Exception {
        when(invoiceScannerService.scanInvoiceForBlacklistedIBANs(anyString()))
                .thenReturn(Set.of("DE89370400440532013000", "GB29NWBK60161331926819"));

        mockMvc.perform(post("/api/invoices/scan")
                        .param("url", "classpath:example_invoice.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("There are 2 blacklisted IBANs found in the invoice."))
                .andExpect(jsonPath("$.blacklistedIbans", Matchers.containsInAnyOrder(
                        "DE89370400440532013000",
                        "GB29NWBK60161331926819"
                )))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    public void testScanInvoice_InvalidPDFUrl() throws Exception {
        when(invoiceScannerService.scanInvoiceForBlacklistedIBANs(anyString()))
                .thenThrow(new IOException("Failed to load PDF document from URL: classpath:invalid_invoice.pdf"));

        mockMvc.perform(post("/api/invoices/scan")
                        .param("url", "classpath:invalid_invoice.pdf"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to load PDF document from URL: classpath:invalid_invoice.pdf"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    public void testScanInvoice_EmptyPDF() throws Exception {
        when(invoiceScannerService.scanInvoiceForBlacklistedIBANs(anyString())).thenReturn(Set.of());

        mockMvc.perform(post("/api/invoices/scan")
                        .param("url", "classpath:empty_invoice.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No blacklisted IBANs found."))
                .andExpect(jsonPath("$.blacklistedIbans").isEmpty())
                .andExpect(jsonPath("$.status").value(200));
    }
}
