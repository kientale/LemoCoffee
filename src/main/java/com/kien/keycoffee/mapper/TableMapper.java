package com.kien.keycoffee.mapper;

import com.kien.keycoffee.dto.TableInfoDTO;
import com.kien.keycoffee.dto.TableTableDTO;
import com.kien.keycoffee.entity.CoffeeTable;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TableMapper {

    TableInfoDTO toTableInfoDTO(CoffeeTable table);

    TableTableDTO toTableTableDTO(CoffeeTable table);
}
