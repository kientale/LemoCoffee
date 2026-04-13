package com.kien.lemocoffee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfoDTO {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer id;

    private Integer tableId;
    private String tableName;

    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private Integer customerPoints;

    private String status;
    private String note;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private String loyaltyAction;
    private Integer freeDrinkId;

    private String selectedDrinksJson;
    private String selectedDrinksJsonEdit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime checkedOutAt;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    public String getCreatedAtFormatted() {
        return formatDateTime(createdAt);
    }

    public String getEffectiveSelectedDrinksJson() {
        if (selectedDrinksJson != null && !selectedDrinksJson.isBlank()) {
            return selectedDrinksJson;
        }

        return selectedDrinksJsonEdit;
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }
}
