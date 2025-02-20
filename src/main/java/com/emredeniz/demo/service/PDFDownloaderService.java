package com.emredeniz.demo.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@Service
public class PDFDownloaderService {

    public PDDocument downloadPDF(String url) throws IOException {
        if (url.startsWith("classpath:")) {
            // Load from classpath (resources folder)
            String resourcePath = url.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IOException("File not found in classpath: " + resourcePath);
            }
            try (InputStream inputStream = resource.getInputStream()) {
                return PDDocument.load(inputStream);
            }
        } else {
            // Load from external URL
            URI uri = URI.create(url);
            URL urlObj = uri.toURL();
            try (InputStream inputStream = urlObj.openStream()) {
                return PDDocument.load(inputStream);
            }
        }
    }
}
