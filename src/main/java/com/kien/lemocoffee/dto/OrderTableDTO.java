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
public class OrderTableDTO {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer id;

    private Integer tableId;
    private String tableName;

    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private Integer customerPoints;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    private String status;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime checkedOutAt;

    private Integer itemCount;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    public String getCreatedAtFormatted() {
        return formatDateTime(createdAt);
    }

    public String getUpdatedAtFormatted() {
        return formatDateTime(updatedAt);
    }

    public String getCheckedOutAtFormatted() {
        return formatDateTime(checkedOutAt);
    }

    public Integer getItemCount() {
        if (itemCount != null) {
            return itemCount;
        }

        return items == null ? 0 : items.size();
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }
}
