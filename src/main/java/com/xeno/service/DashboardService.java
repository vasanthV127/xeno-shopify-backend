package com.xeno.service;

import com.xeno.dto.*;
import com.xeno.model.Customer;
import com.xeno.model.Tenant;
import com.xeno.repository.CustomerRepository;
import com.xeno.repository.OrderRepository;
import com.xeno.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public DashboardStats getDashboardStats(Tenant tenant) {
        String tenantId = tenant.getTenantId();
        
        Long totalCustomers = customerRepository.countByTenantTenantId(tenantId);
        Long totalOrders = orderRepository.countByTenantTenantId(tenantId);
        Long totalProducts = productRepository.countByTenantTenantId(tenantId);
        Double totalRevenue = orderRepository.getTotalRevenueByTenant(tenantId);
        
        if (totalRevenue == null) totalRevenue = 0.0;
        
        Double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
        
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        List<Object[]> todayStats = orderRepository.getOrderStatsByDateRange(tenantId, startOfDay, endOfDay);
        Integer ordersToday = 0;
        Double revenueToday = 0.0;
        
        if (!todayStats.isEmpty()) {
            Object[] stats = todayStats.get(0);
            revenueToday = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
            ordersToday = stats[2] != null ? ((Number) stats[2]).intValue() : 0;
        }

        return DashboardStats.builder()
                .totalCustomers(totalCustomers)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .ordersToday(ordersToday)
                .revenueToday(revenueToday)
                .build();
    }

    public List<TopCustomerDTO> getTopCustomers(Tenant tenant, int limit) {
        String tenantId = tenant.getTenantId();
        List<Customer> customers = customerRepository.findTopCustomersBySpend(tenantId);
        
        return customers.stream()
                .limit(limit)
                .map(customer -> TopCustomerDTO.builder()
                        .customerId(customer.getShopifyCustomerId())
                        .name((customer.getFirstName() != null ? customer.getFirstName() : "") + " " + 
                              (customer.getLastName() != null ? customer.getLastName() : ""))
                        .email(customer.getEmail())
                        .totalSpent(customer.getTotalSpent())
                        .ordersCount(customer.getOrdersCount())
                        .build())
                .collect(Collectors.toList());
    }

    public List<OrderStatsDTO> getOrderStatsByDateRange(Tenant tenant, LocalDate startDate, LocalDate endDate) {
        String tenantId = tenant.getTenantId();
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
        
        List<Object[]> stats = orderRepository.getOrderStatsByDateRange(tenantId, startDateTime, endDateTime);
        
        return stats.stream()
                .map(stat -> {
                    LocalDate date;
                    if (stat[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) stat[0]).toLocalDate();
                    } else {
                        date = (LocalDate) stat[0];
                    }
                    return OrderStatsDTO.builder()
                        .date(date)
                        .revenue(stat[1] != null ? ((Number) stat[1]).doubleValue() : 0.0)
                        .orderCount(stat[2] != null ? ((Number) stat[2]).longValue() : 0L)
                        .build();
                })
                .collect(Collectors.toList());
    }
}
