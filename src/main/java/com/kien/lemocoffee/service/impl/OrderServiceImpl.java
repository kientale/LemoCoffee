package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.constant.CustomerStatusEnum;
import com.kien.lemocoffee.constant.CustomerPointTransactionTypeEnum;
import com.kien.lemocoffee.constant.OrderManagementResult;
import com.kien.lemocoffee.constant.OrderStatusEnum;
import com.kien.lemocoffee.constant.TableStatusEnum;
import com.kien.lemocoffee.dto.OrderInfoDTO;
import com.kien.lemocoffee.dto.OrderTableDTO;
import com.kien.lemocoffee.entity.CoffeeTable;
import com.kien.lemocoffee.entity.Customer;
import com.kien.lemocoffee.entity.CustomerPointTransaction;
import com.kien.lemocoffee.entity.Order;
import com.kien.lemocoffee.entity.OrderItem;
import com.kien.lemocoffee.mapper.OrderMapper;
import com.kien.lemocoffee.repository.CustomerPointTransactionRepository;
import com.kien.lemocoffee.repository.CustomerRepository;
import com.kien.lemocoffee.repository.OrderItemRepository;
import com.kien.lemocoffee.repository.OrderRepository;
import com.kien.lemocoffee.repository.TableRepository;
import com.kien.lemocoffee.service.OrderItemService;
import com.kien.lemocoffee.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final int POINTS_REQUIRED_FOR_REDEEM = 10;
    private static final BigDecimal POINT_AMOUNT_UNIT = new BigDecimal("10000");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPointTransactionRepository customerPointTransactionRepository;
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderTableDTO> getOrder(int page, int size, String keyword) {
        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Order> orderPage = kw.isEmpty()
                ? orderRepository.findAll(pageable)
                : orderRepository.searchByTableName(kw, pageable);

        return orderPage.map(this::toOrderTableDTO);
    }

    @Override
    @Transactional
    public OrderManagementResult createOrder(OrderInfoDTO formData) {
        try {
            CoffeeTable table = findTableById(formData.getTableId());

            if (table == null || table.getStatus() == TableStatusEnum.DELETED) {
                return OrderManagementResult.TABLE_NOT_FOUND;
            }

            Customer customer = findActiveCustomerById(formData.getCustomerId());

            if (formData.getCustomerId() != null && customer == null) {
                return OrderManagementResult.CUSTOMER_NOT_FOUND;
            }

            Order order = Order.builder()
                    .tableId(table.getId())
                    .customerId(customer == null ? null : customer.getId())
                    .totalAmount(BigDecimal.ZERO)
                    .earnedPoints(0)
                    .redeemedPoints(0)
                    .finalAmount(BigDecimal.ZERO)
                    .status(OrderStatusEnum.PENDING)
                    .note(normalizeNullable(formData.getNote()))
                    .createdAt(LocalDateTime.now())
                    .build();

            orderRepository.saveAndFlush(order);
            BigDecimal totalAmount = orderItemService.replaceOrderItems(
                    order,
                    formData.getEffectiveSelectedDrinksJson()
            );

            order.setTotalAmount(totalAmount);
            order.setFinalAmount(totalAmount);
            orderRepository.save(order);

            table.setStatus(TableStatusEnum.OCCUPIED);
            tableRepository.save(table);

            return OrderManagementResult.CREATE_SUCCESS;
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Failed to create order", e);
            return OrderManagementResult.CREATE_FAILED;
        }
    }

    @Override
    public OrderInfoDTO getOrderInfoById(Integer id) {
        Order order = findOrderById(id);
        if (order == null) {
            return null;
        }

        return toOrderInfoDTO(order);
    }

    @Override
    @Transactional
    public OrderManagementResult updateOrder(OrderInfoDTO formData) {
        try {
            Order order = findOrderById(formData == null ? null : formData.getId());
            if (order == null) {
                return OrderManagementResult.ORDER_NOT_FOUND;
            }

            if (isTerminal(order)) {
                return OrderManagementResult.ORDER_CANNOT_BE_EDITED;
            }

            assert formData != null;
            CoffeeTable newTable = findTableById(formData.getTableId());
            if (newTable == null || newTable.getStatus() == TableStatusEnum.DELETED) {
                return OrderManagementResult.TABLE_NOT_FOUND;
            }

            boolean tableChanged = !newTable.getId().equals(order.getTableId());
            if (tableChanged && newTable.getStatus() != TableStatusEnum.AVAILABLE) {
                return OrderManagementResult.TABLE_UNAVAILABLE;
            }

            Customer customer = findActiveCustomerById(formData.getCustomerId());
            if (formData.getCustomerId() != null && customer == null) {
                return OrderManagementResult.CUSTOMER_NOT_FOUND;
            }

            Integer oldTableId = order.getTableId();

            order.setTableId(newTable.getId());
            order.setCustomerId(customer == null ? null : customer.getId());
            order.setNote(normalizeNullable(formData.getNote()));

            BigDecimal totalAmount = orderItemService.replaceOrderItems(
                    order,
                    formData.getEffectiveSelectedDrinksJson()
            );

            order.setTotalAmount(totalAmount);
            order.setFinalAmount(totalAmount);
            orderRepository.save(order);

            if (tableChanged) {
                setTableStatus(oldTableId, TableStatusEnum.AVAILABLE);
            }
            setTableStatus(newTable.getId(), TableStatusEnum.OCCUPIED);

            return OrderManagementResult.UPDATE_SUCCESS;
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Failed to update order id={}", formData == null ? null : formData.getId(), e);
            return OrderManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public OrderManagementResult cancelOrder(Integer id) {
        try {
            Order order = findOrderById(id);
            if (order == null) {
                return OrderManagementResult.ORDER_NOT_FOUND;
            }

            if (order.getStatus() == OrderStatusEnum.COMPLETED) {
                return OrderManagementResult.ORDER_ALREADY_COMPLETED;
            }

            if (order.getStatus() == OrderStatusEnum.CANCELLED) {
                return OrderManagementResult.ORDER_ALREADY_CANCELLED;
            }

            order.setStatus(OrderStatusEnum.CANCELLED);
            orderRepository.save(order);
            releaseTableIfNoPendingOrder(order.getTableId());

            return OrderManagementResult.CANCEL_SUCCESS;
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Failed to cancel order id={}", id, e);
            return OrderManagementResult.CANCEL_FAILED;
        }
    }

    @Override
    @Transactional
    public OrderManagementResult checkoutOrder(Integer id, String loyaltyAction, Integer freeDrinkId) {
        try {
            Order order = findOrderById(id);
            if (order == null) {
                return OrderManagementResult.ORDER_NOT_FOUND;
            }

            if (order.getStatus() == OrderStatusEnum.COMPLETED) {
                return OrderManagementResult.ORDER_ALREADY_COMPLETED;
            }

            if (order.getStatus() == OrderStatusEnum.CANCELLED) {
                return OrderManagementResult.ORDER_ALREADY_CANCELLED;
            }

            List<OrderItem> items = orderItemService.findItemsByOrderId(order.getId());
            if (items.isEmpty()) {
                return OrderManagementResult.EMPTY_ORDER;
            }

            BigDecimal originalTotal = calculateTotal(items);
            BigDecimal finalAmount = originalTotal;
            int earnedPoints = 0;
            int redeemedPoints = 0;

            Customer customer = findActiveCustomerById(order.getCustomerId());
            String action = normalize(loyaltyAction);
            if (action.isEmpty()) {
                action = customer == null ? "none" : "earn";
            }

            if ("redeem".equals(action)) {
                if (customer == null) {
                    return OrderManagementResult.CUSTOMER_NOT_FOUND;
                }

                if (customer.getPoints() == null || customer.getPoints() < POINTS_REQUIRED_FOR_REDEEM) {
                    return OrderManagementResult.LOYALTY_POINT_UPDATE_FAILED;
                }

                OrderItem freeItem = findFreeDrinkItem(items, freeDrinkId);
                if (freeItem == null) {
                    return OrderManagementResult.INVALID_ORDER_ITEM;
                }

                BigDecimal discount = freeItem.getUnitPrice() == null ? BigDecimal.ZERO : freeItem.getUnitPrice();
                finalAmount = originalTotal.subtract(discount).max(BigDecimal.ZERO);

                freeItem.setSubtotal(freeItem.getSubtotal().subtract(discount).max(BigDecimal.ZERO));
                freeItem.setPricingType("POINT_REWARD");
                freeItem.setPointsRedeemed(POINTS_REQUIRED_FOR_REDEEM);
                orderItemRepository.save(freeItem);

                redeemedPoints = POINTS_REQUIRED_FOR_REDEEM;
                customer.setPoints(customer.getPoints() - POINTS_REQUIRED_FOR_REDEEM);
            } else if ("earn".equals(action) && customer != null) {
                earnedPoints = finalAmount.divideToIntegralValue(POINT_AMOUNT_UNIT).intValue();
                customer.setPoints((customer.getPoints() == null ? 0 : customer.getPoints()) + earnedPoints);
            }

            if (customer != null) {
                customerRepository.save(customer);
                saveCustomerPointTransaction(order, customer, earnedPoints, redeemedPoints);
            }

            order.setTotalAmount(originalTotal);
            order.setFinalAmount(finalAmount);
            order.setEarnedPoints(earnedPoints);
            order.setRedeemedPoints(redeemedPoints);
            order.setStatus(OrderStatusEnum.COMPLETED);
            orderRepository.save(order);

            releaseTableIfNoPendingOrder(order.getTableId());

            return OrderManagementResult.CHECKOUT_SUCCESS;
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.error("Failed to checkout order id={}", id, e);
            return OrderManagementResult.CHECKOUT_FAILED;
        }
    }

    @Override
    public String buildInvoiceContent(Integer id) {
        OrderInfoDTO order = getOrderInfoById(id);
        if (order == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("KEYCOFFEE INVOICE").append(System.lineSeparator());
        builder.append("Order #").append(order.getId()).append(System.lineSeparator());
        builder.append("Created At: ").append(order.getCreatedAtFormatted()).append(System.lineSeparator());
        builder.append("Table: ").append(defaultText(order.getTableName(), "-")).append(System.lineSeparator());
        builder.append("Customer: ").append(defaultText(order.getCustomerName(), "Guest")).append(System.lineSeparator());
        builder.append(System.lineSeparator());

        for (var item : order.getItems()) {
            builder.append(item.getQuantity())
                    .append(" x ")
                    .append(item.getDrinkName())
                    .append(" = ")
                    .append(item.getSubtotal())
                    .append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
        builder.append("Total: ").append(defaultAmount(order.getTotalAmount())).append(System.lineSeparator());
        builder.append("Final Amount: ").append(defaultAmount(order.getFinalAmount())).append(System.lineSeparator());
        return builder.toString();
    }

    private OrderInfoDTO toOrderInfoDTO(Order order) {
        return orderMapper.toOrderInfoDTO(
                order,
                findTableById(order.getTableId()),
                findCustomerById(order.getCustomerId()),
                orderItemService.findItemsByOrderId(order.getId())
        );
    }

    private OrderTableDTO toOrderTableDTO(Order order) {
        return orderMapper.toOrderTableDTO(
                order,
                findTableById(order.getTableId()),
                findCustomerById(order.getCustomerId()),
                orderItemService.findItemsByOrderId(order.getId())
        );
    }

    private Order findOrderById(Integer id) {
        if (isInvalidId(id)) {
            return null;
        }

        return orderRepository.findById(id).orElse(null);
    }

    private CoffeeTable findTableById(Integer id) {
        if (isInvalidId(id)) {
            return null;
        }

        return tableRepository.findById(id).orElse(null);
    }

    private Customer findCustomerById(Integer id) {
        if (isInvalidId(id)) {
            return null;
        }

        return customerRepository.findById(id).orElse(null);
    }

    private Customer findActiveCustomerById(Integer id) {
        Customer customer = findCustomerById(id);
        if (customer == null || customer.getStatus() != CustomerStatusEnum.ACTIVE) {
            return null;
        }

        return customer;
    }

    private void setTableStatus(Integer tableId, TableStatusEnum status) {
        CoffeeTable table = findTableById(tableId);
        if (table == null || table.getStatus() == TableStatusEnum.DELETED) {
            return;
        }

        table.setStatus(status);
        tableRepository.save(table);
    }

    private void releaseTableIfNoPendingOrder(Integer tableId) {
        if (isInvalidId(tableId)) {
            return;
        }

        if (!orderRepository.existsByTableIdAndStatus(tableId, OrderStatusEnum.PENDING)) {
            setTableStatus(tableId, TableStatusEnum.AVAILABLE);
        }
    }

    private boolean isTerminal(Order order) {
        return order.getStatus() == OrderStatusEnum.COMPLETED
                || order.getStatus() == OrderStatusEnum.CANCELLED;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem findFreeDrinkItem(List<OrderItem> items, Integer freeDrinkId) {
        if (isInvalidId(freeDrinkId)) {
            return null;
        }

        return items.stream()
                .filter(item -> freeDrinkId.equals(item.getDrinkId()))
                .findFirst()
                .orElse(null);
    }

    private void saveCustomerPointTransaction(
            Order order,
            Customer customer,
            int earnedPoints,
            int redeemedPoints
    ) {
        if (order == null || customer == null || isInvalidId(customer.getId())) {
            return;
        }

        int balanceAfter = customer.getPoints() == null ? 0 : customer.getPoints();

        if (redeemedPoints > 0) {
            savePointTransaction(
                    customer.getId(),
                    order.getId(),
                    CustomerPointTransactionTypeEnum.REDEEM,
                    -redeemedPoints,
                    balanceAfter,
                    "Redeemed " + redeemedPoints + " points for order #" + order.getId()
            );
            return;
        }

        if (earnedPoints > 0) {
            savePointTransaction(
                    customer.getId(),
                    order.getId(),
                    CustomerPointTransactionTypeEnum.EARN,
                    earnedPoints,
                    balanceAfter,
                    "Earned " + earnedPoints + " points from order #" + order.getId()
            );
        }
    }

    private void savePointTransaction(
            Integer customerId,
            Integer orderId,
            CustomerPointTransactionTypeEnum type,
            Integer points,
            Integer balanceAfter,
            String description
    ) {
        customerPointTransactionRepository.save(
                CustomerPointTransaction.builder()
                        .customerId(customerId)
                        .orderId(orderId)
                        .type(type)
                        .points(points)
                        .balanceAfter(balanceAfter)
                        .description(description)
                        .build()
        );
    }

    private boolean isInvalidId(Integer id) {
        return id == null || id <= 0;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void rollbackCurrentTransaction() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
        }
    }
}
