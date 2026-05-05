package com.kien.keycoffee.service.impl;

import com.kien.keycoffee.constant.OrderStatusEnum;
import com.kien.keycoffee.dto.TopCustomerStatisticDTO;
import com.kien.keycoffee.dto.TopCustomerStatisticProjection;
import com.kien.keycoffee.dto.TopDrinkStatisticDTO;
import com.kien.keycoffee.dto.TopDrinkStatisticProjection;
import com.kien.keycoffee.repository.CustomerRepository;
import com.kien.keycoffee.repository.OrderItemRepository;
import com.kien.keycoffee.repository.OrderRepository;
import com.kien.keycoffee.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticServiceImpl implements StatisticService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public BigDecimal getRevenueToday() {
        LocalDate today = LocalDate.now();
        return getRevenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    @Override
    public BigDecimal getRevenueThisMonth() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        return getRevenueBetween(firstDayOfMonth.atStartOfDay(), firstDayOfMonth.plusMonths(1).atStartOfDay());
    }

    @Override
    public BigDecimal getRevenueThisYear() {
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        return getRevenueBetween(firstDayOfYear.atStartOfDay(), firstDayOfYear.plusYears(1).atStartOfDay());
    }

    @Override
    public List<TopCustomerStatisticDTO> getTopCustomers(int limit) {
        Pageable pageable = PageRequest.of(0, normalizeLimit(limit));

        return customerRepository.findTopLoyalCustomers(pageable)
                .stream()
                .map(this::toTopCustomerStatisticDTO)
                .toList();
    }

    @Override
    public List<TopDrinkStatisticDTO> getTopDrinks(int limit) {
        Pageable pageable = PageRequest.of(0, normalizeLimit(limit));

        return orderItemRepository.findTopBestSellingDrinks(pageable)
                .stream()
                .map(this::toTopDrinkStatisticDTO)
                .toList();
    }

    private BigDecimal getRevenueBetween(LocalDateTime fromDate, LocalDateTime toDate) {
        BigDecimal revenue = orderRepository.sumRevenueByStatusAndCreatedAtRange(
                OrderStatusEnum.COMPLETED.name(),
                fromDate,
                toDate
        );

        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    private TopCustomerStatisticDTO toTopCustomerStatisticDTO(TopCustomerStatisticProjection projection) {
        return TopCustomerStatisticDTO.builder()
                .customerId(projection.getCustomerId())
                .customerName(defaultText(projection.getCustomerName()))
                .customerPhone(defaultText(projection.getCustomerPhone()))
                .currentPoints(projection.getCurrentPoints() == null ? 0 : projection.getCurrentPoints())
                .totalOrders(projection.getTotalOrders() == null ? 0L : projection.getTotalOrders())
                .totalSpent(projection.getTotalSpent() == null ? BigDecimal.ZERO : projection.getTotalSpent())
                .build();
    }

    private TopDrinkStatisticDTO toTopDrinkStatisticDTO(TopDrinkStatisticProjection projection) {
        return TopDrinkStatisticDTO.builder()
                .drinkId(projection.getDrinkId())
                .drinkName(defaultText(projection.getDrinkName()))
                .totalQuantity(projection.getTotalQuantity() == null ? 0L : projection.getTotalQuantity())
                .totalRevenue(projection.getTotalRevenue() == null ? BigDecimal.ZERO : projection.getTotalRevenue())
                .build();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }

        return Math.min(limit, 100);
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
