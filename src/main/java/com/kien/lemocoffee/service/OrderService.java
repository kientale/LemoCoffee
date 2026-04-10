package com.kien.lemocoffee.service;

import com.kien.lemocoffee.constant.OrderManagementResult;
import com.kien.lemocoffee.constant.OrderStatusEnum;
import com.kien.lemocoffee.dto.OrderInfoDTO;
import com.kien.lemocoffee.dto.OrderTableDTO;
import org.springframework.data.domain.Page;

public interface OrderService {

    Page<OrderTableDTO> getOrder(int page, int size, String keyword);

    OrderManagementResult createOrder(OrderInfoDTO formData);

    OrderInfoDTO getOrderInfoById(Integer id);

    OrderTableDTO getOrderTableById(Integer id);

    OrderManagementResult updateOrder(OrderInfoDTO formData);

    OrderManagementResult cancelOrder(Integer id);

    OrderManagementResult checkoutOrder(Integer id, String loyaltyAction, Integer freeDrinkId);

    OrderManagementResult changeOrderStatus(Integer id, OrderStatusEnum status);

    String buildInvoiceContent(Integer id);
}
