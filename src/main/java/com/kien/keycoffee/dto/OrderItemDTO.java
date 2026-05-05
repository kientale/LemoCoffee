package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.OrderItemPricingTypeEnum;
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
public class OrderItemDTO {

    private Integer id;
    private Integer orderId;
    private Integer drinkId;
    private String drinkName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private OrderItemPricingTypeEnum pricingType;

    public BigDecimal getSubtotal() {
        if (subtotal != null) {
            return subtotal;
        }

        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isPointReward() {
        return pricingType == OrderItemPricingTypeEnum.POINT_REWARD;
    }
}
