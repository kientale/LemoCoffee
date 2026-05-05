package com.kien.keycoffee.service;

import com.kien.keycoffee.constant.OrderManagementResult;
import com.kien.keycoffee.constant.OrderStatusEnum;
import com.kien.keycoffee.dto.OrderInfoDTO;
import com.kien.keycoffee.dto.OrderTableDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface OrderService {

    Page<OrderTableDTO> getOrder(int page, int size, String keyword);

    OrderManagementResult createOrder(OrderInfoDTO formData);

    OrderInfoDTO getOrderInfoById(Integer id);

    OrderManagementResult updateOrder(OrderInfoDTO formData);

    OrderManagementResult cancelOrder(Integer id);

    OrderManagementResult checkoutOrder(Integer id, String loyaltyAction, Integer freeDrinkId);

    ResponseEntity<byte[]> downloadInvoice(Integer id);
}
