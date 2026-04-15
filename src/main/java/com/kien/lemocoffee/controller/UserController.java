package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.constant.AccountStatusEnum;
import com.kien.lemocoffee.constant.PermissionEnum;
import com.kien.lemocoffee.dto.UserInfoDTO;
import com.kien.lemocoffee.dto.UserTableDTO;
import com.kien.lemocoffee.constant.UserManagementResult;
import com.kien.lemocoffee.normalizer.UserInfoNormalizer;
import com.kien.lemocoffee.service.UserService;
import com.kien.lemocoffee.validate.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final UserInfoNormalizer userInfoNormalizer;

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_VIEW.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_CREATE.name())")
    public String createUser(
            @ModelAttribute("formData") UserInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        formData = userInfoNormalizer.normalize(formData);
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_UPDATE.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_UPDATE.name())")
    public String editUser(
            @ModelAttribute("formData") UserInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "name") String field,
            Model model,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        formData = userInfoNormalizer.normalize(formData);
        preserveRestrictedUserFields(formData, authentication);
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_DELETE.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_VIEW.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_STATUS_UPDATE.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_STATUS_UPDATE.name())")
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
    @PreAuthorize("hasAuthority(T(com.kien.lemocoffee.constant.PermissionEnum).USER_RESET_PASSWORD.name())")
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

    private void preserveRestrictedUserFields(UserInfoDTO formData, Authentication authentication) {
        if (formData == null || formData.getAccountId() == null) {
            return;
        }

        boolean canUpdateRole = hasPermission(authentication, PermissionEnum.USER_ROLE_UPDATE);
        boolean canUpdateStatus = hasPermission(authentication, PermissionEnum.USER_STATUS_UPDATE);

        if (canUpdateRole && canUpdateStatus) {
            return;
        }

        UserInfoDTO currentUser = userService.getUserInfoByAccountId(formData.getAccountId());
        if (currentUser == null) {
            return;
        }

        if (!canUpdateRole) {
            formData.setRole(currentUser.getRole());
        }

        if (!canUpdateStatus) {
            formData.setStatus(currentUser.getStatus());
        }
    }

    private boolean hasPermission(Authentication authentication, PermissionEnum permission) {
        if (authentication == null || permission == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> permission.getAuthority().equals(authority.getAuthority()));
    }
}
