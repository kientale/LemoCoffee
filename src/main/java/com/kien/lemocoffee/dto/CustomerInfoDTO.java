package com.kien.lemocoffee.dto;

import com.kien.lemocoffee.constant.CustomerStatusEnum;
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
