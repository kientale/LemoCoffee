package com.kien.keycoffee.dto;

import com.kien.keycoffee.constant.TableStatusEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableInfoDTO {
    private Integer id;

    private String tableName;
    private Integer capacity;

    private TableStatusEnum status;
}
