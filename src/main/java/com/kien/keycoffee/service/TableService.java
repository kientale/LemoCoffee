package com.kien.keycoffee.service;

import com.kien.keycoffee.dto.TableInfoDTO;
import com.kien.keycoffee.dto.TableTableDTO;
import com.kien.keycoffee.constant.TableManagementResult;
import com.kien.keycoffee.constant.TableStatusEnum;
import org.springframework.data.domain.Page;

public interface TableService {

    Page<TableTableDTO> getTable(int page, int size, String keyword);

    Page<TableTableDTO> getAvailableTables(int page, int size, String keyword);

    TableManagementResult createTable(TableInfoDTO formData);

    TableInfoDTO getTableInfoById(Integer id);

    TableManagementResult updateTable(TableInfoDTO formData);

    TableManagementResult deleteTable(Integer id, TableStatusEnum status);
}
