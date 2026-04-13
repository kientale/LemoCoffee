package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.dto.CustomerTableDTO;
import com.kien.lemocoffee.dto.OrderInfoDTO;
import com.kien.lemocoffee.entity.CustomerPointTransaction;
import com.kien.lemocoffee.constant.CustomerManagementResult;
import com.kien.lemocoffee.constant.CustomerStatusEnum;
import com.kien.lemocoffee.normalizer.CustomerInfoNormalizer;
import com.kien.lemocoffee.service.CustomerPointTransactionService;
import com.kien.lemocoffee.service.CustomerService;
import com.kien.lemocoffee.service.OrderService;
import com.kien.lemocoffee.validate.CustomerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer-management")
public class CustomerController {

    private static final int PAGE_SIZE = 12;
    private static final String CONTENT = "pages/customer-management";
    private static final String LAYOUT = "layouts/admin-layout";

    private final CustomerService customerService;
    private final CustomerPointTransactionService customerPointTransactionService;
    private final OrderService orderService;
    private final CustomerValidator customerValidator;
    private final CustomerInfoNormalizer customerInfoNormalizer;

    @GetMapping
    public String getAllCustomer (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        loadCustomerList(page, keyword, model);
        setViewState(model, false, false, false, null, null, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=create")
    public String createCustomer(
            @ModelAttribute("formData") CustomerInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        formData = customerInfoNormalizer.normalize(formData);
        List<String> errors = customerValidator.validateForCreate(formData);

        if (!errors.isEmpty()) {
            loadCustomerList(page, keyword, model);
            setViewState(model, true, false, false, errors, formData, null);
            return renderPage(model);
        }

        CustomerManagementResult result = customerService.createCustomer(formData);

        if (result != CustomerManagementResult.CREATE_SUCCESS) {
            loadCustomerList(page, keyword, model);
            setViewState(model, true, false, false, List.of(result.getMessage()), formData, null);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditCustomer(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CustomerInfoDTO formData = customerService.getCustomerInfoById(id);

        if (formData == null) {
            return redirectToList(
                    redirectAttributes,
                    CustomerManagementResult.CUSTOMER_NOT_FOUND,
                    page,
                    keyword
            );
        }

        loadCustomerList(page, keyword, model);
        setViewState(model, false, true, false, null, formData, null);
        return renderPage(model);
    }

    @PostMapping(params = "action=edit")
    public String editCustomer(
            @ModelAttribute("formData") CustomerInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        formData = customerInfoNormalizer.normalize(formData);
        List<String> errors = customerValidator.validateForUpdate(formData);

        if (!errors.isEmpty()) {
            loadCustomerList(page, keyword, model);
            setViewState(model, false, true, false, errors, formData, null);
            return renderPage(model);
        }

        CustomerManagementResult result = customerService.updateCustomer(formData);

        if (result != CustomerManagementResult.UPDATE_SUCCESS) {
            loadCustomerList(page, keyword, model);
            setViewState(model, false, true, false, List.of(result.getMessage()), formData, null);
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @PostMapping(params = "action=delete")
    public String deleteCustomer(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        CustomerManagementResult result = customerService.deleteCustomer(id, CustomerStatusEnum.DELETED);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=detail", "id"})
    public String showCustomerDetail(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CustomerInfoDTO customer = customerService.getCustomerInfoById(id);

        if (customer == null) {
            return redirectToList(
                    redirectAttributes,
                    CustomerManagementResult.CUSTOMER_NOT_FOUND,
                    page,
                    keyword
            );
        }

        loadCustomerList(page, keyword, model);
        setViewState(model, false, false, true, null, null, customer);
        return renderPage(model);
    }

    @GetMapping(params = {"view=point-history", "id"})
    public String showPointHistory(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CustomerInfoDTO customer = customerService.getCustomerInfoById(id);

        if (customer == null) {
            return redirectToList(
                    redirectAttributes,
                    CustomerManagementResult.CUSTOMER_NOT_FOUND,
                    page,
                    keyword
            );
        }

        loadCustomerList(page, keyword, model);
        setViewState(model, false, false, false, null, null, null);
        setPointHistoryState(model, customer, customerPointTransactionService.getTransactionsByCustomerId(id));
        return renderPage(model);
    }

    @GetMapping(params = {"view=point-history-order-detail", "id", "orderId"})
    public String showPointHistoryOrderDetail(
            @RequestParam("id") Integer id,
            @RequestParam("orderId") Integer orderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        CustomerInfoDTO customer = customerService.getCustomerInfoById(id);

        if (customer == null) {
            return redirectToList(
                    redirectAttributes,
                    CustomerManagementResult.CUSTOMER_NOT_FOUND,
                    page,
                    keyword
            );
        }

        OrderInfoDTO order = orderService.getOrderInfoById(orderId);

        if (order != null && (order.getCustomerId() == null || !order.getCustomerId().equals(id))) {
            order = null;
        }

        loadCustomerList(page, keyword, model);
        setViewState(model, false, false, false, null, null, null);
        setPointHistoryState(model, customer, customerPointTransactionService.getTransactionsByCustomerId(id));

        model.addAttribute("showOrderDetailModal", true);
        model.addAttribute("detailOrder", order);
        model.addAttribute("detailOrderItems", order == null ? Collections.emptyList() : order.getItems());
        return renderPage(model);
    }

    private void setPointHistoryState(
            Model model,
            CustomerInfoDTO customer,
            List<CustomerPointTransaction> pointTransactions
    ) {
        model.addAttribute("showPointHistoryModal", true);
        model.addAttribute("historyCustomer", customer);
        model.addAttribute("pointTransactions", pointTransactions == null ? Collections.emptyList() : pointTransactions);
    }

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            boolean showDetailModal,
            List<String> errors,
            CustomerInfoDTO formData,
            CustomerInfoDTO customer
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("showDetailModal", showDetailModal);

        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData);
        model.addAttribute("customer", customer);

        model.addAttribute("showPointHistoryModal", false);
        model.addAttribute("showOrderDetailModal", false);
        model.addAttribute("historyCustomer", null);
        model.addAttribute("pointTransactions", Collections.emptyList());
        model.addAttribute("detailOrder", null);
        model.addAttribute("detailOrderItems", Collections.emptyList());
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "customer-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            CustomerManagementResult result,
            int page,
            String keyword
    ) {
        redirectAttributes.addFlashAttribute("status", result);
        redirectAttributes.addFlashAttribute("message", result.getMessage());

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("keyword", keyword);

        return "redirect:/customer-management";
    }

    private void loadCustomerList(int page, String keyword, Model model) {

        Page<CustomerTableDTO> customerPage = customerService.getCustomer(page, PAGE_SIZE, keyword);

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(customerPage.getTotalPages(), 1));
    }
}
