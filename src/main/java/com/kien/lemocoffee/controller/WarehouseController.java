package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.constant.IngredientStatusEnum;
import com.kien.lemocoffee.constant.WarehouseManagementResult;
import com.kien.lemocoffee.dto.IngredientInfoDTO;
import com.kien.lemocoffee.normalizer.IngredientNormalizer;
import com.kien.lemocoffee.service.WarehouseService;
import com.kien.lemocoffee.validate.IngredientValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping({"/warehouse-management", "/ingredient-management"})
public class WarehouseController {

    private static final int PAGE_SIZE = 12;
    private static final String CONTENT = "pages/warehouse-management";
    private static final String LAYOUT = "layouts/admin-layout";

    private final WarehouseService warehouseService;
    private final IngredientValidator ingredientValidator;
    private final IngredientNormalizer ingredientNormalizer;

    @GetMapping
    public String getAllIngredients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model
    ) {
        loadIngredientList(page, keyword, field, model);
        setViewState(model, false, false, null, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=create")
    public String createIngredient(
            @ModelAttribute("formData") IngredientInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        formData = ingredientNormalizer.normalize(formData);
        List<String> errors = ingredientValidator.validateForCreate(formData);

        if (!errors.isEmpty()) {
            loadIngredientList(page, keyword, field, model);
            setViewState(model, true, false, errors, formData);
            return renderPage(model);
        }

        WarehouseManagementResult result = warehouseService.createIngredient(formData);

        if (result != WarehouseManagementResult.CREATE_SUCCESS) {
            loadIngredientList(page, keyword, field, model);
            setViewState(model, true, false, List.of(result.getMessage()), formData);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditIngredient(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        IngredientInfoDTO formData = warehouseService.getIngredientInfoById(id);

        if (formData == null) {
            return redirectToList(
                    redirectAttributes,
                    WarehouseManagementResult.INGREDIENT_NOT_FOUND,
                    page,
                    keyword,
                    field
            );
        }

        loadIngredientList(page, keyword, field, model);
        setViewState(model, false, true, null, formData);
        return renderPage(model);
    }

    @PostMapping(params = "action=edit")
    public String editIngredient(
            @ModelAttribute("formData") IngredientInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        formData = ingredientNormalizer.normalize(formData);
        List<String> errors = ingredientValidator.validateForUpdate(formData);

        if (!errors.isEmpty()) {
            loadIngredientList(page, keyword, field, model);
            setViewState(model, false, true, errors, formData);
            return renderPage(model);
        }

        WarehouseManagementResult result = warehouseService.updateIngredient(formData);

        if (result != WarehouseManagementResult.UPDATE_SUCCESS) {
            loadIngredientList(page, keyword, field, model);
            setViewState(model, false, true, List.of(result.getMessage()), formData);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @PostMapping(params = "action=delete")
    public String deleteIngredient(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            RedirectAttributes redirectAttributes
    ) {
        WarehouseManagementResult result = warehouseService.deleteIngredient(id, IngredientStatusEnum.DELETED);
        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            List<String> errors,
            IngredientInfoDTO formData
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData);
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "warehouse-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            WarehouseManagementResult result,
            int page,
            String keyword,
            String field
    ) {
        redirectAttributes.addFlashAttribute("status", result);
        redirectAttributes.addFlashAttribute("message", result.getMessage());
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("field", field);

        return "redirect:/warehouse-management";
    }

    private void loadIngredientList(int page, String keyword, String field, Model model) {

        Page<IngredientInfoDTO> ingredientPage = warehouseService.getIngredient(page, PAGE_SIZE, keyword, field);

        model.addAttribute("ingredients", ingredientPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(ingredientPage.getTotalPages(), 1));
    }
}
