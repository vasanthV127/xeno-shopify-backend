package com.xeno.service;

import com.xeno.model.Customer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating CSV exports from customer data.
 */
@Service
public class CSVService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generate CSV byte array from customer list.
     * 
     * @param customers List of customers to export
     * @return CSV file as byte array
     * @throws IOException if CSV generation fails
     */
    public byte[] generateCustomersCSV(List<Customer> customers) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        
        // Define CSV format with headers
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(
                        "Customer ID",
                        "Shopify ID",
                        "First Name",
                        "Last Name",
                        "Email",
                        "Phone",
                        "Orders Count",
                        "Total Spent",
                        "Currency",
                        "Customer Since",
                        "Last Order Date",
                        "Accepts Marketing",
                        "Status",
                        "Tags"
                )
                .build();
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            // Write data rows
            for (Customer customer : customers) {
                csvPrinter.printRecord(
                        customer.getId(),
                        customer.getShopifyCustomerId(),
                        customer.getFirstName() != null ? customer.getFirstName() : "",
                        customer.getLastName() != null ? customer.getLastName() : "",
                        customer.getEmail(),
                        customer.getPhone() != null ? customer.getPhone() : "",
                        customer.getOrdersCount(),
                        String.format("%.2f", customer.getTotalSpent()),
                        customer.getCurrency() != null ? customer.getCurrency() : "USD",
                        customer.getCreatedAt() != null ? customer.getCreatedAt().format(DATE_FORMATTER) : "",
                        customer.getUpdatedAt() != null ? customer.getUpdatedAt().format(DATE_FORMATTER) : "",
                        customer.isAcceptsMarketing(),
                        customer.getState() != null ? customer.getState() : "",
                        customer.getTags() != null ? customer.getTags() : ""
                );
            }
            
            csvPrinter.flush();
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * Generate filename for CSV export with timestamp.
     * 
     * @param storeName Store name for filename
     * @return Generated filename
     */
    public String generateFilename(String storeName) {
        String sanitizedStoreName = storeName.replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(java.time.LocalDateTime.now());
        return String.format("customers_%s_%s.csv", sanitizedStoreName, timestamp);
    }
}
