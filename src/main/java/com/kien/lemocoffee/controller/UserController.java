package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.constant.AccountStatusEnum;
import com.kien.lemocoffee.dto.UserInfoDTO;
import com.kien.lemocoffee.dto.UserTableDTO;
import com.kien.lemocoffee.constant.UserManagementResult;
import com.kien.lemocoffee.service.UserService;
import com.kien.lemocoffee.validate.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user-management")
public class UserController {

    private static final int PAGE_SIZE = 12;
    private static final String CONTENT = "pages/user-management";
    private static final String LAYOUT = "layouts/admin-layout";

    private final UserService userService;
    private final UserValidator userValidator;

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            boolean showDetailModal,
            List<String> errors,
            UserInfoDTO formData,
            UserInfoDTO user
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("showDetailModal", showDetailModal);

        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData);
        model.addAttribute("user", user);
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "user-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            UserManagementResult result,
            int page,
            String keyword,
            String field
    ) {
        redirectAttributes.addFlashAttribute("status", result);
        redirectAttributes.addFlashAttribute("message", result.getMessage());

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("field", field);

        return "redirect:/user-management";
    }

    private void loadUserList(int page, String keyword, String field, Model model) {

        Page<UserTableDTO> userPage = userService.getUser(page, PAGE_SIZE, keyword, field);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(userPage.getTotalPages(), 1));
    }

    @GetMapping
    public String getAllUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model
    ) {
        loadUserList(page, keyword, field, model);
        setViewState(model, false, false, false, null, null, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=create")
    public String createUser(
            @ModelAttribute("formData") UserInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = userValidator.validateForCreate(formData);

        if (!errors.isEmpty()) {
            loadUserList(page, keyword, field, model);
            setViewState(model, true, false, false, errors, formData, null);
            return renderPage(model);
        }

        UserManagementResult result = userService.createUser(formData);

        if (result != UserManagementResult.CREATE_SUCCESS) {
            loadUserList(page, keyword, field, model);
            setViewState(model, true, false, false, List.of(result.getMessage()), formData, null);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditUser(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        UserInfoDTO formData = userService.getUserInfoByAccountId(accountId);

        if (formData == null) {
            return redirectToList(
                    redirectAttributes,
                    UserManagementResult.USER_NOT_FOUND,
                    page,
                    keyword,
                    field
            );
        }

        loadUserList(page, keyword, field, model);
        setViewState(model, false, true, false, null, formData, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=edit")
    public String editUser(
            @ModelAttribute("formData") UserInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = userValidator.validateForUpdate(formData);

        if (!errors.isEmpty()) {
            loadUserList(page, keyword, field, model);
            setViewState(model, false, true, false, errors, formData, null);
            return renderPage(model);
        }

        UserManagementResult result = userService.updateUser(formData);

        if (result != UserManagementResult.UPDATE_SUCCESS) {
            loadUserList(page, keyword, field, model);
            setViewState(model, false, true, false, List.of(result.getMessage()), formData, null);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @PostMapping(params = "action=delete")
    public String deleteUser(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            RedirectAttributes redirectAttributes
    ) {
        UserManagementResult result = userService.deleteUser(accountId);
        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @GetMapping(params = {"view=detail", "id"})
    public String showUserDetail(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        UserInfoDTO user = userService.getUserInfoByAccountId(accountId);

        if (user == null) {
            return redirectToList(
                    redirectAttributes,
                    UserManagementResult.USER_NOT_FOUND,
                    page,
                    keyword,
                    field
            );
        }

        loadUserList(page, keyword, field, model);
        setViewState(model, false, false, true, null, null, user);
        return renderPage(model);
    }

    @PostMapping(params = "action=lock")
    public String lockUser(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            RedirectAttributes redirectAttributes
    ) {
        UserManagementResult result = userService.changeUserStatus(accountId, AccountStatusEnum.LOCKED);
        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @PostMapping(params = "action=unlock")
    public String unlockUser(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            RedirectAttributes redirectAttributes
    ) {
        UserManagementResult result = userService.changeUserStatus(accountId, AccountStatusEnum.ACTIVE);
        return redirectToList(redirectAttributes, result, page, keyword, field);
    }

    @PostMapping(params = "action=reset-password")
    public String resetPassword(
            @RequestParam("id") Integer accountId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            RedirectAttributes redirectAttributes
    ) {
        UserManagementResult result = userService.resetPassword(accountId);
        return redirectToList(redirectAttributes, result, page, keyword, field);
    }
}
