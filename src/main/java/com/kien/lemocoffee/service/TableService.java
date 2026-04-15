package com.kien.lemocoffee.service;

import com.kien.lemocoffee.dto.TableInfoDTO;
import com.kien.lemocoffee.dto.TableTableDTO;
import com.kien.lemocoffee.constant.TableManagementResult;
import com.kien.lemocoffee.constant.TableStatusEnum;
import org.springframework.data.domain.Page;

public interface TableService {

    Page<TableTableDTO> getTable(int page, int size, String keyword);

    Page<TableTableDTO> getAvailableTables(int page, int size, String keyword);

    TableManagementResult createTable(TableInfoDTO formData);

    TableInfoDTO getTableInfoById(Integer id);

    TableManagementResult updateTable(TableInfoDTO formData);

    TableManagementResult deleteTable(Integer id, TableStatusEnum status);
}
