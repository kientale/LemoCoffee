package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.CustomerStatusEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfoDTO {
    private Integer id;

    private String fullName;
    private String phone;
    private Integer points;

    private CustomerStatusEnum status;
}
