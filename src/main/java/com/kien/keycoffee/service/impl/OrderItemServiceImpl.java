package com.kien.keycoffee.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kien.keycoffee.constant.DrinkStatusEnum;
import com.kien.keycoffee.constant.IngredientStatusEnum;
import com.kien.keycoffee.constant.OrderItemPricingTypeEnum;
import com.kien.keycoffee.constant.OrderManagementResult;
import com.kien.keycoffee.dto.SelectedDrinkDTO;
import com.kien.keycoffee.entity.Drink;
import com.kien.keycoffee.entity.DrinkIngredient;
import com.kien.keycoffee.entity.Ingredient;
import com.kien.keycoffee.entity.Order;
import com.kien.keycoffee.entity.OrderItem;
import com.kien.keycoffee.exception.OrderProcessException;
import com.kien.keycoffee.repository.DrinkRepository;
import com.kien.keycoffee.repository.DrinkIngredientRepository;
import com.kien.keycoffee.repository.OrderItemRepository;
import com.kien.keycoffee.repository.WarehouseRepository;
import com.kien.keycoffee.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private static final String EMPTY_SELECTED_DRINKS_JSON = "[]";

    private final OrderItemRepository orderItemRepository;
    private final DrinkRepository drinkRepository;
    private final DrinkIngredientRepository drinkIngredientRepository;
    private final WarehouseRepository warehouseRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return Collections.emptyList();
        }
        return orderItemRepository.findByOrderIdOrderByIdAsc(orderId);
    }

    @Override
    public String getSelectedDrinksJsonByOrderId(Integer orderId) {
        List<SelectedDrinkDTO> selectedDrinks = findItemsByOrderId(orderId)
                .stream()
                .map(this::toSelectedDrinkDTO)
                .toList();

        try {
            return objectMapper.writeValueAsString(selectedDrinks);
        } catch (Exception e) {
            return EMPTY_SELECTED_DRINKS_JSON;
        }
    }

    @Override
    @Transactional
    public BigDecimal replaceOrderItems(Order order, String selectedDrinksJson) {

        if (order == null || order.getId() == null || order.getId() <= 0) {
            throw new IllegalArgumentException(OrderManagementResult.INVALID_ORDER.getMessage());
        }

        List<SelectedDrinkDTO> selectedDrinks = parseSelectedDrinks(selectedDrinksJson);
        OrderReplacementPlan replacementPlan = buildReplacementPlan(order, selectedDrinks);

        List<OrderItem> existingItems = findItemsByOrderId(order.getId());
        adjustIngredientStock(
                calculateRequiredIngredientsFromOrderItems(existingItems),
                replacementPlan.requiredIngredients()
        );

        orderItemRepository.deleteByOrderId(order.getId());
        orderItemRepository.saveAll(replacementPlan.orderItems());
        return replacementPlan.totalAmount();
    }

    @Override
    @Transactional
    public void restoreIngredientStockByOrderId(Integer orderId) {
        if (orderId == null || orderId <= 0) {
            return;
        }

        adjustIngredientStock(
                calculateRequiredIngredientsFromOrderItems(findItemsByOrderId(orderId)),
                Collections.emptyMap()
        );
    }

    private SelectedDrinkDTO toSelectedDrinkDTO(OrderItem orderItem) {
        return SelectedDrinkDTO.builder()
                .drinkId(orderItem.getDrinkId())
                .drinkName(orderItem.getDrinkName())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .build();
    }

    private OrderReplacementPlan buildReplacementPlan(Order order, List<SelectedDrinkDTO> selectedDrinks) {
        List<OrderItem> orderItems = new ArrayList<>();
        Map<Integer, BigDecimal> requiredIngredients = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SelectedDrinkDTO selectedDrink : selectedDrinks) {
            //Lấy dữ liệu t DB, vì selectedDrinksJson chỉ là dữ lệu từ client, không phải nguồn đáng tin
            Drink drink = drinkRepository.findById(selectedDrink.getDrinkId()).orElse(null);

            if (drink == null || drink.getStatus() != DrinkStatusEnum.AVAILABLE) {
                throw new OrderProcessException(OrderManagementResult.DRINK_UNAVAILABLE);
            }

            int quantity = selectedDrink.getQuantity();
            BigDecimal unitPrice = defaultAmount(drink.getPrice());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            orderItems.add(OrderItem.builder()
                    .orderId(order.getId())
                    .drinkId(drink.getId())
                    .drinkName(drink.getName())
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .pricingType(OrderItemPricingTypeEnum.NORMAL)
                    .pointsRedeemed(0)
                    .build());

            accumulateRequiredIngredients(
                    requiredIngredients,
                    drinkIngredientRepository.findByIdDrinkIdOrderByIngredientNameAsc(drink.getId()),
                    quantity
            );

            totalAmount = totalAmount.add(subtotal);
        }

        return new OrderReplacementPlan(orderItems, totalAmount, requiredIngredients);
    }

    private Map<Integer, BigDecimal> calculateRequiredIngredientsFromOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, BigDecimal> requiredIngredients = new HashMap<>();
        for (OrderItem orderItem : orderItems) {
            accumulateRequiredIngredients(
                    requiredIngredients,
                    drinkIngredientRepository.findByIdDrinkIdOrderByIngredientNameAsc(orderItem.getDrinkId()),
                    orderItem.getQuantity()
            );
        }
        return requiredIngredients;
    }

    private void accumulateRequiredIngredients(
            Map<Integer, BigDecimal> requiredIngredients,
            List<DrinkIngredient> drinkIngredients,
            int drinkQuantity
    ) {
        if (drinkQuantity <= 0) {
            throw new OrderProcessException(OrderManagementResult.INVALID_QUANTITY);
        }

        if (drinkIngredients == null || drinkIngredients.isEmpty()) {
            throw new OrderProcessException(OrderManagementResult.DRINK_RECIPE_NOT_FOUND);
        }

        BigDecimal multiplier = BigDecimal.valueOf(drinkQuantity);

        for (DrinkIngredient drinkIngredient : drinkIngredients) {

            Integer ingredientId = drinkIngredient.getId() == null ? null : drinkIngredient.getId().getIngredientId();
            BigDecimal recipeQuantity = defaultAmount(drinkIngredient.getQuantity());
            BigDecimal requiredQuantity = recipeQuantity.multiply(multiplier);
            requiredIngredients.merge(ingredientId, requiredQuantity, BigDecimal::add);
        }
    }

    private void adjustIngredientStock(
            Map<Integer, BigDecimal> currentRequirements,
            Map<Integer, BigDecimal> newRequirements
    ) {
        Set<Integer> ingredientIds = new HashSet<>();
        ingredientIds.addAll(currentRequirements.keySet());
        ingredientIds.addAll(newRequirements.keySet());

        Map<Integer, Ingredient> ingredientMap = toIngredientMap(
                warehouseRepository.findAllByIdInForUpdate(ingredientIds)
        );

        for (Integer ingredientId : ingredientIds) {
            Ingredient ingredient = ingredientMap.get(ingredientId);
            BigDecimal currentRequired = currentRequirements.getOrDefault(ingredientId, BigDecimal.ZERO);
            BigDecimal newRequired = newRequirements.getOrDefault(ingredientId, BigDecimal.ZERO);
            BigDecimal delta = newRequired.subtract(currentRequired);

            if (delta.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal availableQuantity = defaultAmount(ingredient.getQuantity());
            if (delta.compareTo(BigDecimal.ZERO) > 0 && availableQuantity.compareTo(delta) < 0) {
                throw new OrderProcessException(OrderManagementResult.INSUFFICIENT_INGREDIENT_STOCK);
            }

            ingredient.setQuantity(availableQuantity.subtract(delta));
        }

        warehouseRepository.saveAll(ingredientMap.values());
    }

    private Map<Integer, Ingredient> toIngredientMap(Collection<Ingredient> ingredients) {
        Map<Integer, Ingredient> ingredientMap = new HashMap<>();
        if (ingredients == null) {
            return ingredientMap;
        }

        for (Ingredient ingredient : ingredients) {
            if (ingredient != null && ingredient.getId() != null) {
                ingredientMap.put(ingredient.getId(), ingredient);
            }
        }

        return ingredientMap;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<SelectedDrinkDTO> parseSelectedDrinks(String selectedDrinksJson) {
        if (!StringUtils.hasText(selectedDrinksJson)) {
            throw new IllegalArgumentException(OrderManagementResult.INVALID_SELECTED_DRINK.getMessage());
        }

        try {
            List<SelectedDrinkDTO> selectedDrinks = objectMapper.readValue(
                    selectedDrinksJson,
                    new TypeReference<>() {
                    }
            );

            if (selectedDrinks == null || selectedDrinks.isEmpty()) {
                throw new IllegalArgumentException(OrderManagementResult.INVALID_SELECTED_DRINK.getMessage());
            }

            validateSelectedDrinks(selectedDrinks);
            return selectedDrinks;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(OrderManagementResult.INVALID_SELECTED_DRINK.getMessage(), e);
        }
    }

    private void validateSelectedDrinks(List<SelectedDrinkDTO> selectedDrinks) {
        Set<Integer> drinkIds = new HashSet<>();

        for (SelectedDrinkDTO selectedDrink : selectedDrinks) {
            Integer drinkId = selectedDrink.getDrinkId();
            Integer quantity = selectedDrink.getQuantity();

            if (drinkId == null || drinkId <= 0 || quantity == null || quantity <= 0) {
                throw new IllegalArgumentException(OrderManagementResult.INVALID_SELECTED_DRINK.getMessage());
            }

            if (!drinkIds.add(drinkId)) {
                throw new IllegalArgumentException(OrderManagementResult.DUPLICATE_SELECTED_DRINK.getMessage());
            }
        }
    }
}
