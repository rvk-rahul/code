package com.emredeniz.demo.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class IBANValidator {

    private Set<String> blacklistedIBANs = new HashSet<>();

    @Value("${blacklisted.ibans.file}") // Inject the file path from application.properties
    private String blacklistedIBANsFile;

    @PostConstruct
    public void init() throws IOException {
        // Load the blacklisted IBANs file from the classpath
        ClassPathResource resource = new ClassPathResource(blacklistedIBANsFile);
        if (!resource.exists()) {
            throw new IOException("Blacklisted IBANs file not found: " + blacklistedIBANsFile);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            blacklistedIBANs = reader.lines()
                    .map(String::trim)              // Trim whitespace
                    .map(this::normalizeIBAN)       // Normalize IBANs
                    .filter(iban -> !iban.isEmpty()) // Remove empty IBANs
                    .collect(Collectors.toSet());   // Store in a HashSet
        }
    }

    /**
     * Normalizes an IBAN by removing all non-alphanumeric characters.
     *
     * @param iban The IBAN to normalize.
     * @return The normalized IBAN.
     */
    private String normalizeIBAN(String iban) {
        // Allow spaces and ensure a valid IBAN format
        Pattern ibanPattern = Pattern.compile("\\b([A-Z]{2}\\s?[0-9]{2}\\s?(?:[A-Z0-9]{1,4}\\s?){1,6}[A-Z0-9]{1,4})\\b");
        Matcher matcher = ibanPattern.matcher(iban);
        if (matcher.find()) {
            String extractedIBAN = matcher.group(1);
            return extractedIBAN.replaceAll("\\s+", ""); // Remove spaces to get the final IBAN
        }
        return ""; // Return an empty string if no valid IBAN is found
    }


    /**
     * Finds all blacklisted IBANs in the given text.
     *
     * @param text The text to search for IBANs.
     * @return A set of blacklisted IBANs found in the text.
     */
    public Set<String> findBlacklistedIBANs(String text) {
        Matcher matcher = IBAN_PATTERN.matcher(text);
        return matcher.results()
                .map(match -> match.group(1)) // Extract the IBAN (group 1)
                .map(this::normalizeIBAN) // Normalize the extracted IBAN
                .filter(iban -> !iban.isEmpty()) // Filter out invalid IBANs
                .filter(blacklistedIBANs::contains) // Check if the IBAN is in the blacklisted set
                .collect(Collectors.toSet());
    }

    // Updated IBAN pattern to capture only valid IBANs
    private static final Pattern IBAN_PATTERN = Pattern.compile("\\b([A-Z]{2}\\s?[0-9]{2}\\s?[A-Z0-9\\s]{1,30})\\b");
}
