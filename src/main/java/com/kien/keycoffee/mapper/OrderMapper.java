package com.kien.keycoffee.mapper;

import com.kien.keycoffee.constant.OrderStatusEnum;
import com.kien.keycoffee.dto.OrderInfoDTO;
import com.kien.keycoffee.dto.OrderItemDTO;
import com.kien.keycoffee.dto.OrderTableDTO;
import com.kien.keycoffee.entity.CoffeeTable;
import com.kien.keycoffee.entity.Customer;
import com.kien.keycoffee.entity.Order;
import com.kien.keycoffee.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(toStatusName(order.getStatus()))")
    OrderInfoDTO toOrderInfoDTO(Order order);

    @Mapping(target = "status", expression = "java(toStatusName(order.getStatus()))")
    OrderTableDTO toOrderTableDTO(Order order);

    @Mapping(target = "status", expression = "java(toOrderStatus(dto.getStatus()))")
    Order toOrder(OrderInfoDTO dto);

    OrderItemDTO toOrderItemDTO(OrderItem orderItem);

    OrderItem toOrderItem(OrderItemDTO dto);

    List<OrderItemDTO> toOrderItemDTOs(List<OrderItem> orderItems);

    default OrderInfoDTO toOrderInfoDTO(
            Order order,
            CoffeeTable table,
            Customer customer,
            List<OrderItem> orderItems
    ) {
        OrderInfoDTO dto = toOrderInfoDTO(order);
        if (dto == null) {
            return null;
        }

        enrichOrderInfo(dto, table, customer, orderItems);
        return dto;
    }

    default OrderTableDTO toOrderTableDTO(
            Order order,
            CoffeeTable table,
            Customer customer,
            List<OrderItem> orderItems
    ) {
        OrderTableDTO dto = toOrderTableDTO(order);
        if (dto == null) {
            return null;
        }

        enrichOrderTable(dto, table, customer, orderItems);
        return dto;
    }

    default void enrichOrderInfo(
            OrderInfoDTO dto,
            CoffeeTable table,
            Customer customer,
            List<OrderItem> orderItems
    ) {
        if (dto == null) {
            return;
        }

        if (table != null) {
            dto.setTableId(table.getId());
            dto.setTableName(table.getTableName());
        }

        if (customer != null) {
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getFullName());
            dto.setCustomerPhone(customer.getPhone());
            dto.setCustomerPoints(customer.getPoints());
        }

        dto.setItems(safeOrderItemDTOs(orderItems));
    }

    default void enrichOrderTable(
            OrderTableDTO dto,
            CoffeeTable table,
            Customer customer,
            List<OrderItem> orderItems
    ) {
        if (dto == null) {
            return;
        }

        if (table != null) {
            dto.setTableId(table.getId());
            dto.setTableName(table.getTableName());
        }

        if (customer != null) {
            dto.setCustomerId(customer.getId());
            dto.setCustomerName(customer.getFullName());
            dto.setCustomerPhone(customer.getPhone());
            dto.setCustomerPoints(customer.getPoints());
        }

        List<OrderItemDTO> items = safeOrderItemDTOs(orderItems);
        dto.setItems(items);
        dto.setItemCount(items.size());
    }

    default List<OrderItemDTO> safeOrderItemDTOs(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        return toOrderItemDTOs(orderItems);
    }

    default String toStatusName(OrderStatusEnum status) {
        return status == null ? null : status.name();
    }

    default OrderStatusEnum toOrderStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return OrderStatusEnum.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
