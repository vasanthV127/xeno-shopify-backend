package com.xeno.controller;

import com.xeno.dto.DashboardStats;
import com.xeno.dto.OrderStatsDTO;
import com.xeno.dto.TopCustomerDTO;
import com.xeno.model.Tenant;
import com.xeno.service.AuthService;
import com.xeno.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(dashboardService.getDashboardStats(tenant));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(dashboardService.getTopCustomers(tenant, limit));
    }

    @GetMapping("/orders-by-date")
    public ResponseEntity<List<OrderStatsDTO>> getOrdersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Tenant tenant = authService.getCurrentTenant();
        return ResponseEntity.ok(dashboardService.getOrderStatsByDateRange(tenant, startDate, endDate));
    }
}
