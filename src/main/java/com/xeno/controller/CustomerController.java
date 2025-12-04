package com.xeno.controller;

import com.xeno.dto.CustomerDTO;
import com.xeno.model.Customer;
import com.xeno.model.Tenant;
import com.xeno.repository.CustomerRepository;
import com.xeno.repository.TenantRepository;
import com.xeno.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://xeno-shopify-frontend-five.vercel.app"})
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Get all customers with filtering, search, and pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCustomers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "totalSpent") String sortBy,
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
}
