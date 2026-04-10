(() => {
    const Order = (window.Order ||= {});
    const ctx = window.CTX || "";
    const POINTS_REQUIRED_FOR_REDEEM = 10;

    const checkoutState = {
        orderId: "",
        totalAmount: 0,
        customerId: 0,
        customerPoints: 0,
        hasCustomer: false,
        redeemOptions: []
    };

    function $(id) {
        return document.getElementById(id);
    }

    function emit(name, detail) {
        document.dispatchEvent(new CustomEvent(name, { detail }));
    }

    function on(name, handler) {
        document.addEventListener(name, handler);
    }

    function ready(handler) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", handler);
            return;
        }
        handler();
    }

    function getModalEl(id) {
        if (!id || !window.bootstrap?.Modal) return null;
        return $(id);
    }

    const modal = {
        show(id) {
            const el = getModalEl(id);
            if (!el) return;
            bootstrap.Modal.getOrCreateInstance(el).show();
        },
        hide(id) {
            const el = getModalEl(id);
            if (!el) return;
            bootstrap.Modal.getOrCreateInstance(el).hide();
        },
        isShown(id) {
            const el = $(id);
            return !!(el && el.classList.contains("show"));
        }
    };

    function formatCurrency(value) {
        const amount = Number(value || 0);
        if (!Number.isFinite(amount)) return "0 VND";

        return `${new Intl.NumberFormat("vi-VN", {
            maximumFractionDigits: 0
        }).format(amount)} VND`;
    }

    function openConfirmModal({
        title = "Confirm Action",
        message = "Are you sure?",
        action = "",
        orderId = "",
        btnClass = "btn-danger",
        formAction = ""
    } = {}) {
        const titleEl = $("confirmModalTitle");
        const messageEl = $("confirmModalMessage");
        const actionInput = $("confirmActionInput");
        const idInput = $("confirmOrderIdInput");
        const form = $("confirmActionForm");
        const confirmBtn = $("confirmActionBtn");

        if (titleEl) titleEl.textContent = title;
        if (messageEl) messageEl.textContent = message;
        if (actionInput) actionInput.value = action;
        if (idInput) idInput.value = orderId;
        if (formAction && form) form.action = formAction;
        if (confirmBtn) confirmBtn.className = `btn ${btnClass}`;

        modal.show("confirmActionModal");
    }

    function openCheckoutModal({
        orderId = "",
        tableName = "-",
        totalAmount = 0,
        customerId = "",
        customerName = "-",
        customerPhone = "",
        customerPoints = 0
    } = {}) {
        const orderIdInput = $("checkoutOrderIdInput");
        const tableNameEl = $("checkoutTableName");
        const customerNameEl = $("checkoutCustomerName");
        const customerPhoneEl = $("checkoutCustomerPhone");
        const customerPointsEl = $("checkoutCustomerPoints");
        const totalAmountEl = $("checkoutTotalAmount");
        const originalAmountEl = $("checkoutOriginalAmount");
        const discountAmountEl = $("checkoutDiscountAmount");
        const itemsListEl = $("checkoutItemsList");

        const parsedCustomerId = Number(customerId || 0);
        const parsedCustomerPoints = Math.max(0, Number(customerPoints || 0));
        const parsedTotal = Number(totalAmount || 0);

        checkoutState.orderId = String(orderId || "");
        checkoutState.totalAmount = Number.isFinite(parsedTotal) ? parsedTotal : 0;
        checkoutState.customerId = Number.isFinite(parsedCustomerId) ? parsedCustomerId : 0;
        checkoutState.customerPoints = Number.isFinite(parsedCustomerPoints) ? parsedCustomerPoints : 0;
        checkoutState.hasCustomer = checkoutState.customerId > 0;

        if (orderIdInput) orderIdInput.value = String(orderId || "");
        if (tableNameEl) tableNameEl.textContent = tableName || "-";
        if (customerNameEl) customerNameEl.textContent = customerName || "-";
        if (customerPhoneEl) customerPhoneEl.textContent = customerPhone || "";
        if (customerPointsEl) customerPointsEl.textContent = String(checkoutState.customerPoints);
        if (totalAmountEl) totalAmountEl.textContent = formatCurrency(checkoutState.totalAmount);
        if (originalAmountEl) originalAmountEl.textContent = formatCurrency(checkoutState.totalAmount);
        if (discountAmountEl) discountAmountEl.textContent = formatCurrency(0);

        const source = document.getElementById(`checkout-items-${orderId}`);
        if (itemsListEl) {
            itemsListEl.innerHTML = source
                ? source.innerHTML
                : "<div class='text-muted'>No drinks in this order.</div>";
        }

        checkoutState.redeemOptions = buildRedeemOptions(source);
        renderCheckoutLoyaltyUI();
        modal.show("checkOutModal");
    }

    function buildRedeemOptions(source) {
        if (!source) return [];

        const byId = new Map();
        source.querySelectorAll("[data-drink-id]").forEach((itemEl) => {
            const drinkId = Number(itemEl.dataset.drinkId || 0);
            if (!drinkId || byId.has(drinkId)) return;

            const drinkName = (itemEl.dataset.drinkName || "").trim();
            const unitPrice = Number(itemEl.dataset.unitPrice || 0);

            byId.set(drinkId, {
                drinkId,
                drinkName: drinkName || `Drink #${drinkId}`,
                unitPrice: Number.isFinite(unitPrice) ? Math.max(unitPrice, 0) : 0
            });
        });

        return [...byId.values()];
    }

    function setCheckoutNotice(message) {
        const noticeEl = $("checkoutLoyaltyNotice");
        if (noticeEl) noticeEl.textContent = message || "";
    }

    function renderRedeemSelectOptions() {
        const select = $("checkoutFreeDrinkSelect");
        if (!select) return;

        select.innerHTML = "";
        checkoutState.redeemOptions.forEach((option) => {
            const opt = document.createElement("option");
            opt.value = String(option.drinkId);
            opt.textContent = `${option.drinkName} (-${formatCurrency(option.unitPrice)})`;
            opt.dataset.price = String(option.unitPrice);
            select.appendChild(opt);
        });
    }

    function getSelectedLoyaltyAction() {
        const checked = document.querySelector("input[name='checkoutLoyaltyAction']:checked");
        return checked?.value || "earn";
    }

    function updateCheckoutFormHiddenInputs(action, freeDrinkId) {
        const actionInput = $("checkoutLoyaltyActionInput");
        const freeDrinkIdInput = $("checkoutFreeDrinkIdInput");

        if (actionInput) actionInput.value = action || "earn";
        if (freeDrinkIdInput) freeDrinkIdInput.value = freeDrinkId ? String(freeDrinkId) : "";
    }

    function refreshCheckoutPricing() {
        const totalAmountEl = $("checkoutTotalAmount");
        const discountAmountEl = $("checkoutDiscountAmount");
        const redeemWrap = $("checkoutRedeemWrap");
        const freeDrinkSelect = $("checkoutFreeDrinkSelect");
        const submitBtn = $("checkoutSubmitBtn");

        let action = getSelectedLoyaltyAction();
        let discount = 0;
        let selectedFreeDrinkId = 0;

        const canRedeem = checkoutState.hasCustomer
            && checkoutState.customerPoints >= POINTS_REQUIRED_FOR_REDEEM
            && checkoutState.redeemOptions.length > 0;

        if (!checkoutState.hasCustomer) {
            action = "none";
        }

        if (action === "redeem" && canRedeem) {
            selectedFreeDrinkId = Number(freeDrinkSelect?.value || 0);
            const selectedOption = freeDrinkSelect?.selectedOptions?.[0];
            discount = Number(selectedOption?.dataset?.price || 0);

            if (redeemWrap) redeemWrap.classList.remove("d-none");
        } else {
            if (redeemWrap) redeemWrap.classList.add("d-none");
            action = checkoutState.hasCustomer ? "earn" : "none";
        }

        const finalAmount = Math.max(0, checkoutState.totalAmount - Math.max(discount, 0));

        if (discountAmountEl) discountAmountEl.textContent = formatCurrency(discount);
        if (totalAmountEl) totalAmountEl.textContent = formatCurrency(finalAmount);
        if (submitBtn) submitBtn.disabled = action === "redeem" && selectedFreeDrinkId <= 0;

        updateCheckoutFormHiddenInputs(action, selectedFreeDrinkId);
    }

    function renderCheckoutLoyaltyUI() {
        const earnRadio = $("checkoutLoyaltyEarn");
        const redeemRadio = $("checkoutLoyaltyRedeem");
        const redeemWrap = $("checkoutRedeemWrap");

        if (!earnRadio || !redeemRadio || !redeemWrap) {
            refreshCheckoutPricing();
            return;
        }

        if (!checkoutState.hasCustomer) {
            earnRadio.checked = true;
            earnRadio.disabled = true;
            redeemRadio.checked = false;
            redeemRadio.disabled = true;
            redeemWrap.classList.add("d-none");
            setCheckoutNotice("Guest order: loyalty points are not available.");
            refreshCheckoutPricing();
            return;
        }

        earnRadio.disabled = false;

        const canRedeem = checkoutState.customerPoints >= POINTS_REQUIRED_FOR_REDEEM
            && checkoutState.redeemOptions.length > 0;

        redeemRadio.disabled = !canRedeem;

        if (!canRedeem && redeemRadio.checked) {
            earnRadio.checked = true;
        }

        renderRedeemSelectOptions();

        if (!canRedeem) {
            redeemWrap.classList.add("d-none");
            setCheckoutNotice(
                `Need at least ${POINTS_REQUIRED_FOR_REDEEM} points to redeem 1 free drink.`
            );
        } else {
            setCheckoutNotice(
                `Redeem available: spend ${POINTS_REQUIRED_FOR_REDEEM} points to get 1 free drink (one drink per checkout).`
            );
        }

        refreshCheckoutPricing();
    }

    function bindCheckoutLoyaltyEvents() {
        const earnRadio = $("checkoutLoyaltyEarn");
        const redeemRadio = $("checkoutLoyaltyRedeem");
        const freeDrinkSelect = $("checkoutFreeDrinkSelect");

        earnRadio?.addEventListener("change", () => {
            refreshCheckoutPricing();
        });

        redeemRadio?.addEventListener("change", () => {
            refreshCheckoutPricing();
        });

        freeDrinkSelect?.addEventListener("change", () => {
            refreshCheckoutPricing();
        });
    }

    function bindCheckoutButtons() {
        document.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-open-checkout");
            if (!button) return;

            event.preventDefault();
            event.stopPropagation();

            openCheckoutModal({
                orderId: button.dataset.orderId,
                tableName: button.dataset.tableName,
                totalAmount: button.dataset.totalAmount,
                customerId: button.dataset.customerId,
                customerName: button.dataset.customerName,
                customerPhone: button.dataset.customerPhone,
                customerPoints: button.dataset.customerPoints
            });
        });
    }

    function bindConfirmButtons() {
        document.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-open-confirm");
            if (!button) return;

            event.preventDefault();
            event.stopPropagation();

            openConfirmModal({
                title: button.dataset.confirmTitle,
                message: button.dataset.confirmMessage,
                action: button.dataset.confirmAction,
                orderId: button.dataset.confirmOrderId,
                btnClass: button.dataset.confirmBtnClass
            });
        });
    }

    function init() {
        bindCheckoutButtons();
        bindConfirmButtons();
        bindCheckoutLoyaltyEvents();
    }

    Object.assign(Order, {
        CTX: ctx,
        ENDPOINT: window.ORDER_ENDPOINT || `${ctx}/order-management`,
        $,
        emit,
        on,
        ready,
        modal,
        formatCurrency
    });

    window.openConfirmModal = openConfirmModal;
    window.openCheckoutModal = openCheckoutModal;

    ready(init);
})();
