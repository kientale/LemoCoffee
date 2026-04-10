package com.kien.lemocoffee.mapper;

import com.kien.lemocoffee.dto.TableInfoDTO;
import com.kien.lemocoffee.dto.TableTableDTO;
import com.kien.lemocoffee.entity.CoffeeTable;
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
