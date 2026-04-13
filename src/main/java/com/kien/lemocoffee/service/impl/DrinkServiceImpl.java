package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.constant.DrinkManagementResult;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.dto.DrinkInfoDTO;
import com.kien.lemocoffee.dto.DrinkTableDTO;
import com.kien.lemocoffee.entity.Drink;
import com.kien.lemocoffee.mapper.DrinkMapper;
import com.kien.lemocoffee.repository.DrinkRepository;
import com.kien.lemocoffee.service.DrinkIngredientService;
import com.kien.lemocoffee.service.DrinkService;
import com.kien.lemocoffee.utils.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DrinkServiceImpl implements DrinkService {

    private static final String STATIC_ROOT_DIR = "src/main/resources/static";
    private static final String UPLOAD_DIR = STATIC_ROOT_DIR + "/img/drink";
    private static final String PUBLIC_IMAGE_DIR = "img/drink";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");

    private final DrinkRepository drinkRepository;
    private final DrinkMapper drinkMapper;
    private final DrinkIngredientService drinkIngredientService;

    @Override
    public Page<DrinkTableDTO> getDrink(int page, int size, String keyword) {

        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Drink> drinkPage = kw.isEmpty() ?
                drinkRepository.findByStatusNot(DrinkStatusEnum.DELETED, pageable) :
                drinkRepository.findByNameContainingIgnoreCaseAndStatusNot(kw, DrinkStatusEnum.DELETED, pageable);

        return drinkPage.map(drinkMapper::toDrinkTableDTO);
    }

    @Override
    public Page<DrinkTableDTO> getAvailableDrinks(int page, int size, String keyword) {

        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        String kw = normalize(keyword);

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.ASC, "name"));

        Page<Drink> drinkPage;
        if (kw.isEmpty()) {
            drinkPage = drinkRepository.findByStatus(DrinkStatusEnum.AVAILABLE, pageable);
        } else {
            drinkPage = drinkRepository.findByNameContainingIgnoreCaseAndStatus(
                    kw,
                    DrinkStatusEnum.AVAILABLE,
                    pageable
            );
        }

        return drinkPage.map(drinkMapper::toDrinkTableDTO);
    }

    @Override
    @Transactional
    public DrinkManagementResult createDrink(DrinkInfoDTO formData, MultipartFile imageFile) {
        String savedImagePath = null;

        try {
            if (drinkRepository.existsByNameIgnoreCase(formData.getName())) {
                return DrinkManagementResult.DRINK_ALREADY_EXISTS;
            }

            savedImagePath = saveImage(imageFile);

            Drink drink = Drink.builder()
                    .name(formData.getName())
                    .price(formData.getPrice())
                    .description(formData.getDescription())
                    .image(StringUtils.hasText(savedImagePath) ? savedImagePath : formData.getImage())
                    .status(DrinkStatusEnum.AVAILABLE)
                    .build();

            drinkRepository.saveAndFlush(drink);
            drinkIngredientService.replaceDrinkIngredients(
                    drink,
                    formData.getSelectedIngredientsJson());

            return DrinkManagementResult.CREATE_SUCCESS;

        } catch (ImageStorageException e) {
            log.error("Failed to save drink image for name={}", formData.getName(), e);
            rollbackCurrentTransaction();
            return DrinkManagementResult.IMAGE_SAVE_FAILED;
        } catch (Exception e) {
            deleteSavedImage(savedImagePath);
            rollbackCurrentTransaction();
            log.error("Failed to create drink with name={}", formData.getName(), e);
            return DrinkManagementResult.CREATE_FAILED;
        }
    }

    @Override
    public DrinkInfoDTO getDrinkInfoById(Integer id) {
        Drink drink = findDrinkById(id);
        if (drink == null || drink.getStatus() == DrinkStatusEnum.DELETED) {
            return null;
        }

        return drinkMapper.toDrinkInfoDTO(drink);
    }

    @Override
    @Transactional
    public DrinkManagementResult updateDrink(DrinkInfoDTO formData, MultipartFile imageFile) {
        String savedImagePath = null;
        try {
            Integer id = formData.getId();
            Drink drink = findDrinkById(id);

            if (drink == null || drink.getStatus() == DrinkStatusEnum.DELETED) {
                return DrinkManagementResult.DRINK_NOT_FOUND;
            }

            if (drinkRepository.existsByNameIgnoreCaseAndIdNot(formData.getName(), id)) {
                return DrinkManagementResult.DRINK_ALREADY_EXISTS;
            }

            savedImagePath = saveImage(imageFile);

            drink.setName(formData.getName());
            drink.setPrice(formData.getPrice());
            drink.setDescription(formData.getDescription());
            drink.setStatus(formData.getStatus());
            drink.setImage(savedImagePath);

            drinkRepository.saveAndFlush(drink);
            drinkIngredientService.replaceDrinkIngredients(drink, formData.getSelectedIngredientsJson());
            return DrinkManagementResult.UPDATE_SUCCESS;

        } catch (ImageStorageException e) {
            log.error("Failed to save drink image for id={}", formData.getId(), e);
            rollbackCurrentTransaction();
            return DrinkManagementResult.IMAGE_SAVE_FAILED;
        } catch (Exception e) {
            deleteSavedImage(savedImagePath);
            rollbackCurrentTransaction();
            log.error("Failed to update drink id={}", formData.getId(), e);
            return DrinkManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public DrinkManagementResult changeDrinkStatus(Integer id, DrinkStatusEnum status) {
        try {
            if (status == null || status == DrinkStatusEnum.DELETED) {
                return DrinkManagementResult.CHANGE_STATUS_FAILED;
            }

            Drink drink = findDrinkById(id);

            if (drink == null || drink.getStatus() == DrinkStatusEnum.DELETED) {
                return DrinkManagementResult.DRINK_NOT_FOUND;
            }

            drink.setStatus(status);
            drinkRepository.save(drink);
            return DrinkManagementResult.CHANGE_STATUS_SUCCESS;
        } catch (Exception e) {
            log.error("Failed to change drink status id={}, status={}", id, status, e);
            return DrinkManagementResult.CHANGE_STATUS_FAILED;
        }
    }

    @Override
    @Transactional
    public DrinkManagementResult deleteDrink(Integer id, DrinkStatusEnum status) {
        try {
            if (status == null) {
                return DrinkManagementResult.DELETE_FAILED;
            }

            Drink drink = findDrinkById(id);
            if (drink == null) {
                return DrinkManagementResult.DRINK_NOT_FOUND;
            }

            drink.setStatus(status);
            drinkRepository.save(drink);
            return DrinkManagementResult.DELETE_SUCCESS;
        } catch (Exception e) {
            log.error("Failed to delete drink id={}", id, e);
            return DrinkManagementResult.DELETE_FAILED;
        }
    }

    private Drink findDrinkById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return drinkRepository.findById(id).orElse(null);
    }

    private String saveImage(MultipartFile imageFile) {

        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = normalize(imageFile.getOriginalFilename());
            String extension = getExtension(originalFilename);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Invalid image extension");
            }

            return FileStorageUtil.saveFile(imageFile, UPLOAD_DIR, PUBLIC_IMAGE_DIR);
        } catch (Exception e) {
            throw new ImageStorageException(e);
        }
    }

    private void deleteSavedImage(String imagePath) {
        if (StringUtils.hasText(imagePath)) {
            FileStorageUtil.deletePublicFile(imagePath, STATIC_ROOT_DIR);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
    private void rollbackCurrentTransaction() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
        }
    }

    private static class ImageStorageException extends RuntimeException {
        private ImageStorageException(Throwable cause) {
            super(cause);
        }
    }
}
