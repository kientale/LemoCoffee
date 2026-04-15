package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.constant.CustomerManagementResult;
import com.kien.lemocoffee.constant.OrderManagementResult;
import com.kien.lemocoffee.dto.CustomerInfoDTO;
import com.kien.lemocoffee.dto.CustomerTableDTO;
import com.kien.lemocoffee.dto.DrinkTableDTO;
import com.kien.lemocoffee.dto.OrderInfoDTO;
import com.kien.lemocoffee.dto.OrderItemDTO;
import com.kien.lemocoffee.dto.OrderTableDTO;
import com.kien.lemocoffee.dto.TableTableDTO;
import com.kien.lemocoffee.service.CustomerService;
import com.kien.lemocoffee.service.DrinkService;
import com.kien.lemocoffee.service.OrderItemService;
import com.kien.lemocoffee.service.OrderService;
import com.kien.lemocoffee.service.TableService;
import com.kien.lemocoffee.validate.CustomerValidator;
import com.kien.lemocoffee.validate.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order-management")
public class OrderController {

    private static final int PAGE_SIZE = 10;
    private static final int PICKER_PAGE_SIZE = 10;
    private static final String CONTENT = "pages/order/order-management";
    private static final String LAYOUT = "layouts/admin-layout";
    private static final String EMPTY_SELECTED_DRINKS_JSON = "[]";

    private final OrderService orderService;
    private final OrderValidator orderValidator;
    private final TableService tableService;
    private final CustomerService customerService;
    private final DrinkService drinkService;
    private final OrderItemService orderItemService;
    private final CustomerValidator customerValidator;

    @GetMapping
    public String getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        loadOrderList(page, keyword, model);
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

    @GetMapping(params = "action=tablePicker")
    public String getTablePicker(
            @RequestParam(defaultValue = "1") int tablePage,
            @RequestParam(defaultValue = "") String tableKeyword,
            @RequestParam(value = "selectedTableId", required = false) Integer selectedTableId,
        Model model
    ) {
        loadTablePicker(tablePage, tableKeyword, selectedTableId, model);
        return "pages/order/table-picker";
    }

    @GetMapping(params = "action=customerPicker")
    public String getCustomerPicker(
            @RequestParam(defaultValue = "1") int customerPage,
            @RequestParam(defaultValue = "") String customerKeyword,
            @RequestParam(value = "selectedCustomerId", required = false) Integer selectedCustomerId,
        Model model
    ) {
        loadCustomerPicker(customerPage, customerKeyword, selectedCustomerId, model);
        return "pages/order/customer-picker";
    }

    @GetMapping(params = "action=drinkPicker")
    public String getDrinkPicker(
            @RequestParam(defaultValue = "1") int drinkPage,
            @RequestParam(defaultValue = "") String drinkKeyword,
            @RequestParam(defaultValue = "") String selectedDrinkIds,
        Model model
    ) {
        loadDrinkPicker(drinkPage, drinkKeyword, selectedDrinkIds, model);
        return "pages/order/drink-picker";
    }

    @GetMapping(params = "action=download-invoice")
    public ResponseEntity<byte[]> downloadInvoice(
            @RequestParam("id") Integer id
    ) {
        return orderService.downloadInvoice(id);
    }

    @PostMapping(params = "action=create")
    public String createOrder(
            @ModelAttribute("formData") OrderInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = orderValidator.validateForCreate(formData);

        if (!errors.isEmpty()) {
            loadOrderList(page, keyword, model);
            setViewState(
                    model,
                    true,
                    false,
                    false,
                    errors,
                    formData,
                    null,
                    null,
                    formData.getEffectiveSelectedDrinksJson(),
                    null
            );
            return renderPage(model);
        }

        OrderManagementResult result = orderService.createOrder(formData);

        if (result != OrderManagementResult.CREATE_SUCCESS) {
            loadOrderList(page, keyword, model);
            setViewState(
                    model,
                    true,
                    false,
                    false,
                    List.of(result.getMessage()),
                    formData,
                    null,
                    null,
                    formData.getEffectiveSelectedDrinksJson(),
                    null
            );
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=edit", "id"})
    public String showEditOrder(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        OrderInfoDTO editOrder = orderService.getOrderInfoById(id);
        if (editOrder == null) {
            return redirectToList(redirectAttributes, OrderManagementResult.ORDER_NOT_FOUND, page, keyword);
        }

        if (isTerminal(editOrder)) {
            return redirectToList(redirectAttributes, OrderManagementResult.ORDER_CANNOT_BE_EDITED, page, keyword);
        }

        loadOrderList(page, keyword, model);
        setViewState(
                model,
                false,
                true,
                false,
                null,
                null,
                editOrder,
                null,
                null,
                orderItemService.getSelectedDrinksJsonByOrderId(id)
        );
        return renderPage(model);
    }

    @PostMapping(params = "action=update")
    public String updateOrder(
            @ModelAttribute("formData") OrderInfoDTO formData,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = orderValidator.validateForUpdate(formData);
        if (!errors.isEmpty()) {
            loadOrderList(page, keyword, model);
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
                    formData.getEffectiveSelectedDrinksJson()
            );
            return renderPage(model);
        }

        OrderManagementResult result = orderService.updateOrder(formData);
        if (result != OrderManagementResult.UPDATE_SUCCESS) {
            loadOrderList(page, keyword, model);
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
                    formData.getEffectiveSelectedDrinksJson()
            );
            return renderPage(model);
        }

        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @GetMapping(params = {"view=detail", "id"})
    public String showOrderDetail(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        OrderInfoDTO detailOrder = orderService.getOrderInfoById(id);
        if (detailOrder == null) {
            return redirectToList(redirectAttributes, OrderManagementResult.ORDER_NOT_FOUND, page, keyword);
        }

        loadOrderList(page, keyword, model);
        setViewState(
                model,
                false,
                false,
                true,
                null,
                null,
                null,
                detailOrder,
                null,
                null
        );
        return renderPage(model);
    }

    @PostMapping(params = "action=cancel")
    public String cancelOrder(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = orderValidator.validateForCancel(id);
        if (!errors.isEmpty()) {
            return redirectWithValidationErrors(redirectAttributes, errors, page, keyword);
        }

        OrderManagementResult result = orderService.cancelOrder(id);
        return redirectToList(redirectAttributes, result, page, keyword);
    }

    @PostMapping(params = "action=check-out")
    public String checkoutOrder(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "earn") String loyaltyAction,
            @RequestParam(value = "freeDrinkId", required = false) Integer freeDrinkId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        List<String> errors = orderValidator.validateForCheckout(id, loyaltyAction, freeDrinkId);
        if (!errors.isEmpty()) {
            return redirectWithValidationErrors(redirectAttributes, errors, page, keyword);
        }

        OrderManagementResult result = orderService.checkoutOrder(id, loyaltyAction, freeDrinkId);
        String redirect = redirectToList(redirectAttributes, result, page, keyword);
        if (result == OrderManagementResult.CHECKOUT_SUCCESS) {
            redirectAttributes.addAttribute("invoiceOrderId", id);
        }
        return redirect;
    }

    @PostMapping(
            value = "/create-customer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> createCustomerFromOrder(
            @RequestBody Map<String, String> payload
    ) {
        CustomerInfoDTO formData = CustomerInfoDTO.builder()
                .fullName(payload == null ? "" : payload.get("fullName"))
                .phone(payload == null ? "" : payload.get("phone"))
                .build();

        List<String> errors = customerValidator.validateForCreate(formData);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(buildCustomerCreateErrorResponse(errors));
        }

        CustomerManagementResult result = customerService.createCustomer(formData);
        if (result != CustomerManagementResult.CREATE_SUCCESS) {
            return ResponseEntity.badRequest().body(buildCustomerCreateErrorResponse(List.of(result.getMessage())));
        }

        CustomerInfoDTO createdCustomer = customerService.getCustomerInfoByPhone(formData.getPhone());
        if (createdCustomer == null) {
            return ResponseEntity.badRequest().body(buildCustomerCreateErrorResponse(
                    List.of(OrderManagementResult.CUSTOMER_NOT_FOUND.getMessage())
            ));
        }

        return ResponseEntity.ok(buildCustomerCreateSuccessResponse(createdCustomer, result.getMessage()));
    }

    private void loadOrderList(int page, String keyword, Model model) {

        Page<OrderTableDTO> orderPage = orderService.getOrder(page, PAGE_SIZE, keyword);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", Math.max(orderPage.getTotalPages(), 1));
    }

    private void loadTablePicker(
            int tablePage,
            String tableKeyword,
            Integer selectedTableId,
            Model model
    ) {
        int pageNo = Math.max(1, tablePage);
        Page<TableTableDTO> tables = tableService.getAvailableTables(pageNo, PICKER_PAGE_SIZE, tableKeyword);

        model.addAttribute("tables", tables.getContent());
        model.addAttribute("tablePage", pageNo);
        model.addAttribute("tableTotalPages", Math.max(tables.getTotalPages(), 1));
        model.addAttribute("selectedTableId", selectedTableId);
    }

    private void loadCustomerPicker(
            int customerPage,
            String customerKeyword,
            Integer selectedCustomerId,
            Model model
    ) {
        int pageNo = Math.max(1, customerPage);
        Page<CustomerTableDTO> customers = customerService.getCustomer(pageNo, PICKER_PAGE_SIZE, customerKeyword);

        model.addAttribute("customers", customers.getContent());
        model.addAttribute("customerPage", pageNo);
        model.addAttribute("customerTotalPages", Math.max(customers.getTotalPages(), 1));
        model.addAttribute("selectedCustomerId", selectedCustomerId);
    }

    private void loadDrinkPicker(
            int drinkPage,
            String drinkKeyword,
            String selectedDrinkIds,
            Model model
    ) {
        int pageNo = Math.max(1, drinkPage);
        Page<DrinkTableDTO> drinks = drinkService.getAvailableDrinks(pageNo, PICKER_PAGE_SIZE, drinkKeyword);

        model.addAttribute("drinks", drinks.getContent());
        model.addAttribute("drinkPage", pageNo);
        model.addAttribute("drinkTotalPages", Math.max(drinks.getTotalPages(), 1));
        model.addAttribute("selectedDrinkIds", parseSelectedIds(selectedDrinkIds));
    }

    private String redirectWithValidationErrors(
            RedirectAttributes redirectAttributes,
            List<String> errors,
            int page,
            String keyword
    ) {
        return redirectToList(
                redirectAttributes,
                OrderManagementResult.VALIDATION_FAILED,
                String.join("; ", errors),
                page,
                keyword
        );
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            OrderManagementResult result,
            int page,
            String keyword
    ) {
        return redirectToList(redirectAttributes, result, result.getMessage(), page, keyword);
    }

    private String redirectToList(
            RedirectAttributes redirectAttributes,
            Object status,
            String message,
            int page,
            String keyword
    ) {
        redirectAttributes.addFlashAttribute("status", status);
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addAttribute("page", Math.max(1, page));
        redirectAttributes.addAttribute("keyword", keyword == null ? "" : keyword);

        return "redirect:/order-management";
    }

    private List<OrderItemDTO> getOrderItems(OrderInfoDTO order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        return order.getItems();
    }

    private List<Integer> parseSelectedIds(String selectedIds) {
        if (!StringUtils.hasText(selectedIds)) {
            return Collections.emptyList();
        }

        return Arrays.stream(selectedIds.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
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

    private String defaultSelectedJson(String selectedDrinksJson) {
        if (!StringUtils.hasText(selectedDrinksJson)) {
            return EMPTY_SELECTED_DRINKS_JSON;
        }

        return selectedDrinksJson;
    }

    private Map<String, Object> buildCustomerCreateSuccessResponse(CustomerInfoDTO customer, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("customer", buildCustomerPayload(customer));
        return response;
    }

    private Map<String, Object> buildCustomerPayload(CustomerInfoDTO customer) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", customer.getId());
        payload.put("name", customer.getFullName());
        payload.put("fullName", customer.getFullName());
        payload.put("phone", customer.getPhone());
        payload.put("points", customer.getPoints() == null ? 0 : customer.getPoints());
        return payload;
    }

    private Map<String, Object> buildCustomerCreateErrorResponse(List<String> errors) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("errors", errors == null || errors.isEmpty() ? List.of("Cannot create customer") : errors);
        return response;
    }

    private boolean isTerminal(OrderInfoDTO order) {
        String status = order == null ? "" : order.getStatus();
        return "COMPLETED".equals(status) || "CANCELLED".equals(status);
    }

    private void setViewState(
            Model model,
            boolean showCreateModal,
            boolean showEditModal,
            boolean showDetailModal,
            List<String> errors,
            OrderInfoDTO formData,
            OrderInfoDTO editOrder,
            OrderInfoDTO detailOrder,
            String selectedDrinksJson,
            String selectedDrinksJsonEdit
    ) {
        model.addAttribute("showCreateModal", showCreateModal);
        model.addAttribute("showEditModal", showEditModal);
        model.addAttribute("showDetailModal", showDetailModal);

        model.addAttribute("errors", errors);
        model.addAttribute("formData", formData == null ? new OrderInfoDTO() : formData);
        model.addAttribute("editOrder", editOrder == null ? new OrderInfoDTO() : editOrder);
        model.addAttribute("detailOrder", detailOrder);
        model.addAttribute("detailOrderItems", getOrderItems(detailOrder));

        model.addAttribute("selectedDrinksJson", defaultSelectedJson(selectedDrinksJson));
        model.addAttribute("selectedDrinksJsonEdit", defaultSelectedJson(selectedDrinksJsonEdit));
    }

    private String renderPage(Model model) {
        model.addAttribute("activePage", "order-management");
        model.addAttribute("content", CONTENT);
        return LAYOUT;
    }
}
