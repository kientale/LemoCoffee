package com.kien.keycoffee.validate;

import com.kien.keycoffee.dto.TableInfoDTO;
import com.kien.keycoffee.constant.TableValidateResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TableValidator {

    public List<String> validateForCreate(TableInfoDTO formData) {
        List<String> errors = new ArrayList<>();
        validateCapacity(formData.getCapacity(), errors);
        return errors;
    }

    public List<String> validateForUpdate(TableInfoDTO formData) {
        List<String> errors = new ArrayList<>();

        Integer id = formData.getId();
        if (id == null || id <= 0) {
            errors.add(TableValidateResult.INVALID_ID.getMessage());
            return errors;
        }

        validateCapacity(formData.getCapacity(), errors);
        return errors;
    }

    private void validateCapacity(Integer capacity, List<String> errors) {
        if (capacity == null || capacity < 1 || capacity > 20) {
            errors.add(TableValidateResult.INVALID_CAPACITY.getMessage());
        }
    }
}
