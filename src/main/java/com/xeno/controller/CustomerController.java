package com.xeno.controller;

import com.xeno.dto.CustomerDTO;
import com.xeno.model.Customer;
import com.xeno.model.Tenant;
import com.xeno.repository.CustomerRepository;
import com.xeno.repository.TenantRepository;
import com.xeno.security.UserPrincipal;
import com.xeno.service.CSVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://xeno-shopify-frontend-five.vercel.app"})
@Tag(name = "Customers", description = "Customer management and segmentation endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CSVService csvService;

    /**
     * Get all customers with filtering, search, and pagination
     */
    @GetMapping
    @Operation(
            summary = "List customers with filters",
            description = "Get paginated list of customers with optional filtering by segment (high/medium/low value) and search by name/email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customers"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> getCustomers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filter by customer segment: high (>$5000), medium ($1000-$5000), low (<$1000)")
            @RequestParam(required = false) String segment,
            @Parameter(description = "Search term for customer name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "totalSpent") String sortBy,
            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Tenant tenant = tenantRepository.findByTenantId(userPrincipal.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customerPage;

        // Apply filters based on segment and search
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.toLowerCase() + "%";
            customerPage = customerRepository.searchCustomers(searchTerm, tenant, pageable);
        } else if (segment != null && !segment.isEmpty()) {
            customerPage = getCustomersBySegment(segment, tenant, pageable);
        } else {
            customerPage = customerRepository.findByTenant(tenant, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("customers", customerPage.getContent());
        response.put("currentPage", customerPage.getNumber());
        response.put("totalItems", customerPage.getTotalElements());
        response.put("totalPages", customerPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Get customer segments statistics
     */
    @GetMapping("/segments")
    @Operation(
            summary = "Get customer segment statistics",
            description = "Returns count of customers in each value segment (high/medium/low) and total"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved segment stats")
    public ResponseEntity<Map<String, Object>> getSegmentStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Tenant tenant = tenantRepository.findByTenantId(userPrincipal.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // High value: > $5000
        long highValueCount = customerRepository.countByTenantAndTotalSpentGreaterThan(
                tenant, new BigDecimal("5000"));

        // Medium value: $1000 - $5000
        long mediumValueCount = customerRepository.countByTenantAndTotalSpentBetween(
                tenant, new BigDecimal("1000"), new BigDecimal("5000"));

        // Low value: < $1000
        long lowValueCount = customerRepository.countByTenantAndTotalSpentLessThan(
                tenant, new BigDecimal("1000"));

        // Total customers
        long totalCount = customerRepository.countByTenant(tenant);

        Map<String, Object> response = new HashMap<>();
        response.put("highValue", highValueCount);
        response.put("mediumValue", mediumValueCount);
        response.put("lowValue", lowValueCount);
        response.put("total", totalCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Get single customer details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        Tenant tenant = tenantRepository.findByTenantId(userPrincipal.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Customer customer = customerRepository.findByIdAndTenant(id, tenant)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return ResponseEntity.ok(customer);
    }

    /**
     * Export customers to CSV file
     */
    @GetMapping("/export")
    @Operation(
            summary = "Export customers to CSV",
            description = "Download all customers (or filtered subset) as CSV file with full customer details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<byte[]> exportCustomersCSV(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filter by customer segment: high (>$5000), medium ($1000-$5000), low (<$1000)")
            @RequestParam(required = false) String segment,
            @Parameter(description = "Search term for customer name or email")
            @RequestParam(required = false) String search) {

        try {
            Tenant tenant = tenantRepository.findByTenantId(userPrincipal.getTenantId())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            // Get customers based on filters (no pagination for export)
            List<Customer> customers;

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                customers = customerRepository.searchCustomersAll(searchTerm, tenant);
            } else if (segment != null && !segment.isEmpty()) {
                customers = getCustomersBySegmentAll(segment, tenant);
            } else {
                customers = customerRepository.findByTenant(tenant);
            }

            // Generate CSV
            byte[] csvBytes = csvService.generateCustomersCSV(customers);
            String filename = csvService.generateFilename(tenant.getStoreName());

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to get customers by segment
     */
    private Page<Customer> getCustomersBySegment(String segment, Tenant tenant, Pageable pageable) {
        return switch (segment.toLowerCase()) {
            case "high" -> customerRepository.findByTenantAndTotalSpentGreaterThan(
                    tenant, new BigDecimal("5000"), pageable);
            case "medium" -> customerRepository.findByTenantAndTotalSpentBetween(
                    tenant, new BigDecimal("1000"), new BigDecimal("5000"), pageable);
            case "low" -> customerRepository.findByTenantAndTotalSpentLessThan(
                    tenant, new BigDecimal("1000"), pageable);
            default -> customerRepository.findByTenant(tenant, pageable);
        };
    }

    /**
     * Helper method to get all customers by segment (no pagination for export)
     */
    private List<Customer> getCustomersBySegmentAll(String segment, Tenant tenant) {
        return switch (segment.toLowerCase()) {
            case "high" -> customerRepository.findByTenantAndTotalSpentGreaterThan(
                    tenant, new BigDecimal("5000"));
            case "medium" -> customerRepository.findByTenantAndTotalSpentBetween(
                    tenant, new BigDecimal("1000"), new BigDecimal("5000"));
            case "low" -> customerRepository.findByTenantAndTotalSpentLessThan(
                    tenant, new BigDecimal("1000"));
            default -> customerRepository.findByTenant(tenant);
        };
    }
}
