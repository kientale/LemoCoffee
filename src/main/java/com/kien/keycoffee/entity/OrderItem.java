package com.kien.keycoffee.entity;

import com.kien.keycoffee.constant.OrderItemPricingTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "drink_id", nullable = false)
    private Integer drinkId;

    @Column(name = "drink_name", nullable = false, length = 255)
    private String drinkName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", length = 50)
    private OrderItemPricingTypeEnum pricingType;

    @Column(name = "points_redeemed")
    private Integer pointsRedeemed;
}
