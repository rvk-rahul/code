package com.emredeniz.demo.controller;

import com.emredeniz.demo.model.HttpResponse;
import com.emredeniz.demo.service.InvoiceScannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceScannerController {

    private final InvoiceScannerService invoiceScannerService;

    public InvoiceScannerController(InvoiceScannerService invoiceScannerService) {
        this.invoiceScannerService = invoiceScannerService;
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scanInvoice(@RequestParam String url) {
        try {
            Set<String> blacklistedIBANs = invoiceScannerService.scanInvoiceForBlacklistedIBANs(url);
            if (blacklistedIBANs.isEmpty()) {
                return response(HttpStatus.OK,"No blacklisted IBANs found.", blacklistedIBANs);
            }

            String message = String.format("There %s %d blacklisted IBAN%s found in the invoice.",
                    blacklistedIBANs.size() == 1 ? "is" : "are",
                    blacklistedIBANs.size(),
                    blacklistedIBANs.size() == 1 ? "" : "s");

            return response(HttpStatus.OK, message, blacklistedIBANs);
        } catch (IOException e) {
            return response(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message, Set<String> blacklistedIBANs) {
        return new ResponseEntity<>(new HttpResponse(message, blacklistedIBANs, httpStatus.value()), httpStatus);
    }
}

