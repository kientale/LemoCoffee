package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.constant.CustomerManagementResult;
import com.kien.lemocoffee.dto.TableInfoDTO;
import com.kien.lemocoffee.dto.TableTableDTO;
import com.kien.lemocoffee.entity.CoffeeTable;
import com.kien.lemocoffee.constant.TableManagementResult;
import com.kien.lemocoffee.constant.TableStatusEnum;
import com.kien.lemocoffee.mapper.TableMapper;
import com.kien.lemocoffee.repository.TableRepository;
import com.kien.lemocoffee.service.TableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableServiceImpl implements TableService {

    private static final String TABLE_NAME_PREFIX = "Table";

    private final TableRepository tableRepository;
    private final TableMapper tableMapper;

    @Override
    public Page<TableTableDTO> getTable(int page, int size, String keyword) {

        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<CoffeeTable> tablePage = kw.isEmpty() ?
                tableRepository.findByStatusNot(TableStatusEnum.DELETED, pageable) :
                tableRepository.findByTableNameContainingIgnoreCaseAndStatusNot(kw, TableStatusEnum.DELETED, pageable);

        return tablePage.map(tableMapper::toTableTableDTO);
    }

    @Override
    public Page<TableTableDTO> getAvailableTables(int page, int size, String keyword) {

        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<CoffeeTable> tablePage = kw.isEmpty() ?
                tableRepository.findByStatus(TableStatusEnum.AVAILABLE, pageable) :
                tableRepository.findByTableNameContainingIgnoreCaseAndStatus(kw, TableStatusEnum.AVAILABLE, pageable);

        return tablePage.map(tableMapper::toTableTableDTO);
    }

    @Override
    @Transactional
    public TableManagementResult createTable(TableInfoDTO formData) {
        try {
            int nextNumber = tableRepository.findTopByOrderByIdDesc()
                    .map(CoffeeTable::getId)
                    .orElse(0) + 1;

            CoffeeTable table = CoffeeTable.builder()
                    .tableName(generateTableName(nextNumber))
                    .capacity(formData.getCapacity())
                    .status(TableStatusEnum.AVAILABLE)
                    .build();

            tableRepository.save(table);
            return TableManagementResult.CREATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to create table with capacity={}", formData.getCapacity(), e);
            return TableManagementResult.CREATE_FAILED;
        }
    }

    @Override
    public TableInfoDTO getTableInfoById(Integer id) {
        return tableRepository.findById(id)
                .map(tableMapper::toTableInfoDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public TableManagementResult updateTable(TableInfoDTO formData) {
        try {
            CoffeeTable table = findTableById(formData.getId());

            if (table == null || table.getStatus() == TableStatusEnum.DELETED) {
                return TableManagementResult.TABLE_NOT_FOUND;
            }

            table.setCapacity(formData.getCapacity());
            tableRepository.save(table);
            return TableManagementResult.UPDATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to update table id={}", formData.getId(), e);
            return TableManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public TableManagementResult deleteTable(Integer id, TableStatusEnum status) {
        try {
            if (status == null) {
                return  TableManagementResult.DELETE_FAILED;
            }

            CoffeeTable table = findTableById(id);
            if (table == null) {
                return TableManagementResult.TABLE_NOT_FOUND;
            }

            table.setStatus(status);
            tableRepository.save(table);
            return TableManagementResult.DELETE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to delete table id={}", id, e);
            return TableManagementResult.DELETE_FAILED;
        }
    }

    private CoffeeTable findTableById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return tableRepository.findById(id).orElse(null);
    }

    private String generateTableName(int number) {
        return TABLE_NAME_PREFIX + number;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
