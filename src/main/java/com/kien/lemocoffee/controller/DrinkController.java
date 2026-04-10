package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.constant.DrinkManagementResult;
import com.kien.lemocoffee.constant.DrinkStatusEnum;
import com.kien.lemocoffee.dto.DrinkInfoDTO;
import com.kien.lemocoffee.dto.DrinkTableDTO;
import com.kien.lemocoffee.dto.IngredientInfoDTO;
import com.kien.lemocoffee.entity.DrinkIngredient;
import com.kien.lemocoffee.service.DrinkIngredientService;
import com.kien.lemocoffee.service.DrinkService;
import com.kien.lemocoffee.service.WarehouseService;
import com.kien.lemocoffee.validate.DrinkValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.WebDataBinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/drink-management")
public class DrinkController {

    private static final int PAGE_SIZE = 7;
    private static final int INGREDIENT_PAGE_SIZE = 10;
    private static final String CONTENT = "pages/drink/drink-management";
    private static final String LAYOUT = "layouts/admin-layout";
    private static final String EMPTY_SELECTED_INGREDIENTS_JSON = "[]";

    private final DrinkService drinkService;
    private final DrinkIngredientService drinkIngredientService;
    private final WarehouseService warehouseService;
    private final DrinkValidator drinkValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("image");
    }

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            boolean showDetailModal,
            List<String> errors,
            DrinkInfoDTO formData,

            DrinkInfoDTO editDrink,
            DrinkInfoDTO detailDrink,
            String selectedIngredientsJson,
            String selectedIngredientsJsonEdit
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("showDetailModal", showDetailModal);

        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData);
        model.addAttribute("editDrink", editDrink == null ? new DrinkInfoDTO() : editDrink);
        model.addAttribute("detailDrink", detailDrink);
        model.addAttribute("detailDrinkIngredients", Collections.emptyList());

        model.addAttribute("selectedIngredientsJson", defaultSelectedJson(selectedIngredientsJson));
        model.addAttribute("selectedIngredientsJsonEdit", defaultSelectedJson(selectedIngredientsJsonEdit));
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "drink-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            DrinkManagementResult result,
            int page,
            String keyword
    ) {
        redirectAttributes.addFlashAttribute("status", result);
        redirectAttributes.addFlashAttribute("message", result.getMessage());
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("keyword", keyword);

        return "redirect:/drink-management";
    }

    private void loadDrinkList(int page, String keyword, Model model) {

        Page<DrinkTableDTO> drinkPage = drinkService.getDrink(page, PAGE_SIZE, keyword);

        model.addAttribute("drinks", drinkPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(drinkPage.getTotalPages(), 1));
    }

    private void loadIngredientPicker(
            int ingPage,
            String ingKeyword,
            String ingField,
            String selectedIds,
            Model model
    ) {
        Page<IngredientInfoDTO> ingredientPage = warehouseService.getIngredient(
                ingPage,
                INGREDIENT_PAGE_SIZE,
                ingKeyword,
                ingField
        );

        model.addAttribute("ingredients", ingredientPage.getContent());
        model.addAttribute("ingPage", ingPage);
        model.addAttribute("ingTotalPages", Math.max(ingredientPage.getTotalPages(), 1));
        model.addAttribute("selectedIngIds", parseSelectedIds(selectedIds));
    }

    @GetMapping
    public String getAllDrinks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        loadDrinkList(page, keyword, model);
        setViewState(
                model,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return renderPage(model);
    }

    @GetMapping(params = "action=ingredientPicker")
    public String getIngredientPicker(
            @RequestParam(defaultValue = "1") int ingPage,
            @RequestParam(defaultValue = "") String ingKeyword,
            @RequestParam(defaultValue = "name") String ingField,
            @RequestParam(defaultValue = "") String selectedIds,
            Model model
    ) {
        loadIngredientPicker(ingPage, ingKeyword, ingField, selectedIds, model);
        return "pages/drink/ingredient-picker";
    }

    @PostMapping(params = "action=create")
    public String createDrink(
            @ModelAttribute("formData") DrinkInfoDTO formData,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = drinkValidator.validateForCreate(formData, imageFile);
        if (!errors.isEmpty()) {
            loadDrinkList(page, keyword, model);
            setViewState(
                    model,
                    true,
                    false,
                    false,
                    errors,
                    formData,
                    null,
                    null,
                    formData.getSelectedIngredientsJson(),
                    null
            );
            return renderPage(model);
        }

        DrinkManagementResult result = drinkService.createDrink(formData, imageFile);
        if (result != DrinkManagementResult.CREATE_SUCCESS) {
            loadDrinkList(page, keyword, model);
            setViewState(
                    model,
                    true,
                    false,
                    false,
                    List.of(result.getMessage()),
                    formData,
                    null,
                    null,
                    formData.getSelectedIngredientsJson(),
                    null
            );
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditDrink(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        DrinkInfoDTO editDrink = drinkService.getDrinkInfoById(id);
        if (editDrink == null) {
            return redirectToList(redirectAttributes, DrinkManagementResult.DRINK_NOT_FOUND, page, keyword);
        }

        loadDrinkList(page, keyword, model);
        setViewState(
                model,
                false,
                true,
                false,
                null,
                null,
                editDrink,
                null,
                null,
                drinkIngredientService.getSelectedIngredientsJsonByDrinkId(id)
        );
        return renderPage(model);
    }

    @PostMapping(params = "action=update")
    public String updateDrink(
            @ModelAttribute("formData") DrinkInfoDTO formData,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = drinkValidator.validateForUpdate(formData, imageFile);
        if (!errors.isEmpty()) {
            loadDrinkList(page, keyword, model);
            setViewState(
                    model,
                    false,
                    true,
                    false,
                    errors,
                    null,
                    formData,
                    null,
                    null,
                    formData.getSelectedIngredientsJson()
            );
            return renderPage(model);
        }

        DrinkManagementResult result = drinkService.updateDrink(formData, imageFile);
        if (result != DrinkManagementResult.UPDATE_SUCCESS) {
            loadDrinkList(page, keyword, model);
            setViewState(
                    model,
                    false,
                    true,
                    false,
                    List.of(result.getMessage()),
                    null,
                    formData,
                    null,
                    null,
                    formData.getSelectedIngredientsJson()
            );
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=detail", "id"})
    public String showDrinkDetail(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        DrinkInfoDTO detailDrink = drinkService.getDrinkInfoById(id);
        if (detailDrink == null) {
            return redirectToList(redirectAttributes, DrinkManagementResult.DRINK_NOT_FOUND, page, keyword);
        }

        List<DrinkIngredient> detailDrinkIngredients = drinkIngredientService.getDrinkIngredientsByDrinkId(id);

        loadDrinkList(page, keyword, model);
        setViewState(
                model,
                false,
                false,
                true,
                null,
                null,
                null,
                detailDrink,
                null,
                null
        );
        model.addAttribute("detailDrinkIngredients", detailDrinkIngredients);
        return renderPage(model);
    }

    @PostMapping(params = "action=available")
    public String markDrinkAvailable(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        DrinkManagementResult result = drinkService.changeDrinkStatus(id, DrinkStatusEnum.AVAILABLE);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @PostMapping(params = "action=unavailable")
    public String markDrinkUnavailable(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        DrinkManagementResult result = drinkService.changeDrinkStatus(id, DrinkStatusEnum.UNAVAILABLE);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @PostMapping(params = "action=delete")
    public String deleteDrink(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        DrinkManagementResult result = drinkService.deleteDrink(id, DrinkStatusEnum.DELETED);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    private List<Integer> parseSelectedIds(String selectedIds) {
        if (selectedIds == null || selectedIds.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(selectedIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::parseIntegerOrNull)
                .filter(id -> id != null && id > 0)
                .toList();
    }

    private Integer parseIntegerOrNull(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String defaultSelectedJson(String selectedIngredientsJson) {
        if (selectedIngredientsJson == null || selectedIngredientsJson.isBlank()) {
            return EMPTY_SELECTED_INGREDIENTS_JSON;
        }
        return selectedIngredientsJson;
    }
}
