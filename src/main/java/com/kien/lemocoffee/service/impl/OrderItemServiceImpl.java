package com.kien.lemocoffee.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.dto.OrderItemDTO;
import com.kien.lemocoffee.dto.SelectedDrinkDTO;
import com.kien.lemocoffee.entity.Drink;
import com.kien.lemocoffee.entity.Order;
import com.kien.lemocoffee.entity.OrderItem;
import com.kien.lemocoffee.mapper.OrderMapper;
import com.kien.lemocoffee.repository.DrinkRepository;
import com.kien.lemocoffee.repository.OrderItemRepository;
import com.kien.lemocoffee.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final DrinkRepository drinkRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return Collections.emptyList();
        }

        return orderItemRepository.findByOrderIdOrderByIdAsc(orderId);
    }

    @Override
    @Transactional
    public BigDecimal replaceOrderItems(Order order, String selectedDrinksJson) {
        if (order == null || isInvalidId(order.getId())) {
            throw new IllegalArgumentException("Invalid order");
        }

        List<SelectedDrinkDTO> selectedDrinks = parseSelectedDrinks(selectedDrinksJson);

        orderItemRepository.deleteByOrderId(order.getId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SelectedDrinkDTO selectedDrink : selectedDrinks) {
            Drink drink = drinkRepository.findById(selectedDrink.getDrinkId()).orElse(null);
            if (drink == null || drink.getStatus() != DrinkStatusEnum.AVAILABLE) {
                throw new IllegalArgumentException("Drink is not available");
            }

            BigDecimal unitPrice = drink.getPrice() == null ? BigDecimal.ZERO : drink.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(selectedDrink.getQuantity()));

            orderItems.add(OrderItem.builder()
                    .orderId(order.getId())
                    .drinkId(drink.getId())
                    .drinkName(drink.getName())
                    .quantity(selectedDrink.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .pricingType("NORMAL")
                    .pointsRedeemed(0)
                    .build());

            totalAmount = totalAmount.add(subtotal);
        }

        orderItemRepository.saveAll(orderItems);
        return totalAmount;
    }

    private List<SelectedDrinkDTO> parseSelectedDrinks(String selectedDrinksJson) {
        if (!StringUtils.hasText(selectedDrinksJson)) {
            throw new IllegalArgumentException("Invalid selected drinks");
        }

        try {
            List<SelectedDrinkDTO> selectedDrinks = objectMapper.readValue(
                    selectedDrinksJson,
                    new TypeReference<>() {
                    }
            );

            if (selectedDrinks == null || selectedDrinks.isEmpty()) {
                throw new IllegalArgumentException("Invalid selected drinks");
            }

            validateSelectedDrinks(selectedDrinks);
            return selectedDrinks;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid selected drinks", e);
        }
    }

    private boolean isInvalidId(Integer id) {
        return id == null || id <= 0;
    }

    private void validateSelectedDrinks(List<SelectedDrinkDTO> selectedDrinks) {
        Set<Integer> drinkIds = new HashSet<>();
        for (SelectedDrinkDTO selectedDrink : selectedDrinks) {
            Integer drinkId = selectedDrink.getDrinkId();
            Integer quantity = selectedDrink.getQuantity();

            if (drinkId == null || drinkId <= 0 || quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Invalid selected drink");
            }

            if (!drinkIds.add(drinkId)) {
                throw new IllegalArgumentException("Duplicate selected drink");
            }
        }
    }
}
