package com.kien.keycoffee.entity;

import com.kien.keycoffee.constant.TableStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coffee_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatusEnum status;
}