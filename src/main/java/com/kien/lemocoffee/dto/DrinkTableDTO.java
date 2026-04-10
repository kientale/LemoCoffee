package com.kien.lemocoffee.dto;

import com.kien.lemocoffee.constant.DrinkStatusEnum;
import lombok.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrinkTableDTO {

    private static final List<String> DEFAULT_PUBLIC_IMAGES = List.of(
            "/img/cafe1.png",
            "/img/cafe2.png",
            "/img/cafe5.png",
            "/img/cafe6.png",
            "/img/cafe8.png",
            "/img/cafe10.png"
    );

    private Integer id;

    private String name;
    private BigDecimal price;
    private String description;
    private String image;

    private DrinkStatusEnum status;

    public String getPublicImageUrl() {
        if (image == null || image.isBlank()) {
            int imageIndex = Math.floorMod(id == null ? 0 : id, DEFAULT_PUBLIC_IMAGES.size());
            return DEFAULT_PUBLIC_IMAGES.get(imageIndex);
        }

        return image.startsWith("/") ? image : "/" + image;
    }

    public String getPriceFormatted() {
        if (price == null) {
            return "0";
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price);
    }
}
