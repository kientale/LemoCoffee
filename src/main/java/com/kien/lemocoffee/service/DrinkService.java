package com.kien.lemocoffee.service;

import com.kien.lemocoffee.constant.DrinkManagementResult;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.dto.DrinkInfoDTO;
import com.kien.lemocoffee.dto.DrinkTableDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface DrinkService {

    Page<DrinkTableDTO> getDrink(int page, int size, String keyword);

    Page<DrinkTableDTO> getAvailableDrinks(int page, int size, String keyword);

    DrinkManagementResult createDrink(DrinkInfoDTO formData, MultipartFile imageFile);

    DrinkInfoDTO getDrinkInfoById(Integer id);

    DrinkManagementResult updateDrink(DrinkInfoDTO formData, MultipartFile imageFile);

    DrinkManagementResult changeDrinkStatus(Integer id, DrinkStatusEnum status);

    DrinkManagementResult deleteDrink(Integer id, DrinkStatusEnum status);
}
