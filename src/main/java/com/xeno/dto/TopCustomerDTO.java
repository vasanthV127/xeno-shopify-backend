package com.xeno.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {
    private String customerId;
    private String name;
    private String email;
    private BigDecimal totalSpent;
    private Integer ordersCount;
}
