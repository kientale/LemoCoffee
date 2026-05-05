package com.kien.keycoffee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopCustomerStatisticDTO {

    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private Integer currentPoints;
    private Long totalOrders;
    private BigDecimal totalSpent;
}
