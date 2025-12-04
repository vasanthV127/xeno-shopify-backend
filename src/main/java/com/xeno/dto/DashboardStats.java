package com.xeno.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalCustomers;
    private Long totalOrders;
    private Long totalProducts;
    private Double totalRevenue;
    private Double averageOrderValue;
    private Integer ordersToday;
    private Double revenueToday;
}
