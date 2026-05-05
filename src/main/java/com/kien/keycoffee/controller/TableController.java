package com.kien.keycoffee.controller;

import com.kien.keycoffee.dto.TableInfoDTO;
import com.kien.keycoffee.dto.TableTableDTO;
import com.kien.keycoffee.constant.TableManagementResult;
import com.kien.keycoffee.constant.TableStatusEnum;
import com.kien.keycoffee.service.TableService;
import com.kien.keycoffee.validate.TableValidator;
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
@RequestMapping("/table-management")
public class TableController {

    private static final int PAGE_SIZE = 9;
    private static final String CONTENT = "pages/table-management";
    private static final String LAYOUT = "layouts/admin-layout";

    private final TableService tableService;
    private final TableValidator tableValidator;

    @GetMapping
    public String getAllTables(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        loadTableList(page, keyword, model);
        setViewState(model, false, false, null, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=create")
    public String createTable(
            @ModelAttribute("formData") TableInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = tableValidator.validateForCreate(formData);

        if (!errors.isEmpty()) {
            loadTableList(page, keyword, model);
            setViewState(model, true, false, errors, formData);
            return renderPage(model);
        }

        TableManagementResult result = tableService.createTable(formData);

        if (result != TableManagementResult.CREATE_SUCCESS) {
            loadTableList(page, keyword, model);
            setViewState(model, true, false, List.of(result.getMessage()), formData);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditTable(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        TableInfoDTO formData = tableService.getTableInfoById(id);

        if (formData == null) {
            return redirectToList(
                    redirectAttributes,
                    TableManagementResult.TABLE_NOT_FOUND,
                    page,
                    keyword
            );
        }

        loadTableList(page, keyword, model);
        setViewState(model, false, true, null, formData);
        return renderPage(model);
    }

    @PostMapping(params = "action=edit")
    public String editTable(
            @ModelAttribute("formData") TableInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = tableValidator.validateForUpdate(formData);

        if (!errors.isEmpty()) {
            loadTableList(page, keyword, model);
            setViewState(model, false, true, errors, formData);
            return renderPage(model);
        }

        TableManagementResult result = tableService.updateTable(formData);

        if (result != TableManagementResult.UPDATE_SUCCESS) {
            loadTableList(page, keyword, model);
            setViewState(model, false, true, List.of(result.getMessage()), formData);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @PostMapping(params = "action=delete")
    public String deleteTable(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        TableManagementResult result = tableService.deleteTable(id, TableStatusEnum.DELETED);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            List<String> errors,
            TableInfoDTO formData
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData);
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "table-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            TableManagementResult result,
            int page,
            String keyword
    ) {
        redirectAttributes.addFlashAttribute("status", result);
        redirectAttributes.addFlashAttribute("message", result.getMessage());
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("keyword", keyword);

        return "redirect:/table-management";
    }

    private void loadTableList(int page, String keyword, Model model) {
        Page<TableTableDTO> tablePage = tableService.getTable(page, PAGE_SIZE, keyword);
        model.addAttribute("tables", tablePage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(tablePage.getTotalPages(), 1));
    }
}
