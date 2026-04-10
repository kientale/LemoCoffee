(() => {
    const Order = window.Order;

    let initialized = false;
    let activeOwner = "add";

    const endpointFromDom = document.getElementById("customerPickerContainer")?.dataset?.endpoint || "";
    const ENDPOINT = window.ORDER_ENDPOINT || endpointFromDom || `${window.CTX || ""}/order-management`;

    function setLoading(el, show) {
        if (!el) return;
        el.classList.toggle("d-none", !show);
    }

    function getOwnerModalId(owner = activeOwner) {
        return owner === "edit" ? "editOrderModal" : "addOrderModal";
    }

    function hideModalById(id) {
        const el = document.getElementById(id);
        if (!el || !window.bootstrap?.Modal) return;
        bootstrap.Modal.getOrCreateInstance(el).hide();
    }

    function waitForHidden(id, timeoutMs = 350) {
        return new Promise((resolve) => {
            const el = document.getElementById(id);
            if (!el || !el.classList.contains("show")) {
                resolve();
                return;
            }

            let done = false;
            const finish = () => {
                if (done) return;
                done = true;
                el.removeEventListener("hidden.bs.modal", onHidden);
                resolve();
            };

            const onHidden = () => finish();
            el.addEventListener("hidden.bs.modal", onHidden, { once: true });
            setTimeout(finish, timeoutMs);
            hideModalById(id);
        });
    }

    function setActiveOwner(owner) {
        activeOwner = owner === "edit" ? "edit" : "add";
        window.OrderCustomerPicker?.setOwner?.(activeOwner);

        const ownerInput = Order.$("orderCreateCustomerOwner");
        if (ownerInput) ownerInput.value = activeOwner;
    }

    function getActiveModule() {
        return activeOwner === "edit"
            ? window.EditOrderCustomerManager
            : window.AddOrderCustomerManager;
    }

    function clearCreateErrors() {
        const box = Order.$("orderCreateCustomerErrors");
        const list = Order.$("orderCreateCustomerErrorsList");

        if (list) list.innerHTML = "";
        if (box) box.classList.add("d-none");
    }

    function showCreateErrors(messages = []) {
        const box = Order.$("orderCreateCustomerErrors");
        const list = Order.$("orderCreateCustomerErrorsList");
        if (!box || !list) return;

        list.innerHTML = "";
        messages.forEach((message) => {
            const li = document.createElement("li");
            li.textContent = message;
            list.appendChild(li);
        });

        box.classList.remove("d-none");
    }

    function setCreateSubmitLoading(show) {
        const button = Order.$("btnSubmitCreateCustomer");
        const spinner = Order.$("createCustomerSubmitSpinner");
        const text = button?.querySelector(".btn-text");

        if (button) button.disabled = show;
        if (spinner) spinner.classList.toggle("d-none", !show);
        if (text) text.classList.toggle("d-none", show);
    }

    function getCsrfHeaders(form) {
        const input = form?.querySelector("[data-csrf-token='true']");
        const headerName = input?.dataset?.headerName;
        const token = input?.value;

        return headerName && token ? { [headerName]: token } : {};
    }

    function buildCustomerPayload(form) {
        return {
            fullName: String(form?.querySelector("[name='fullName']")?.value || "").trim(),
            phone: String(form?.querySelector("[name='phone']")?.value || "").trim()
        };
    }

    function resetCreateForm() {
        const form = Order.$("orderCreateCustomerForm");
        form?.reset();
        clearCreateErrors();

        const ownerInput = Order.$("orderCreateCustomerOwner");
        if (ownerInput) ownerInput.value = activeOwner;
    }

    function normalizeErrorMessages(payload, fallback) {
        if (Array.isArray(payload?.errors) && payload.errors.length) {
            return payload.errors;
        }

        if (payload?.message) {
            return [payload.message];
        }

        return [fallback];
    }

    function selectCreatedCustomer(customer) {
        const id = Number(customer?.id || 0);
        const name = String(customer?.name || customer?.fullName || "").trim();
        if (!id || !name) {
            showCreateErrors(["Invalid created customer data."]);
            return false;
        }

        const manager = getActiveModule();
        if (!manager?.select) {
            showCreateErrors(["Cannot determine target order form."]);
            return false;
        }

        manager.select({ id, name });
        return true;
    }

    function getSearchValues() {
        const form = Order.$("customerPickerSearchForm");
        if (!form) return { keyword: "" };

        const keyword = (form.querySelector("input[name='customerKeyword']")?.value || "").trim();
        return { keyword };
    }

    async function load(page = 1, options = {}) {
        const container = Order.$("customerPickerContainer");
        const loading = Order.$("customerPickerLoading");
        const errorBox = Order.$("customerPickerError");
        if (!container) return;

        const showLoading = options.showLoading !== false;
        const { keyword } = getSearchValues();

        const customerPageInput = Order.$("customerPageInput");
        if (customerPageInput) customerPageInput.value = String(page);

        const selectedCustomerId = getActiveModule()?.getSelectedId?.() || "";
        const params = new URLSearchParams({
            ajax: "true",
            action: "customerPicker",
            customerPage: String(page),
            customerKeyword: keyword,
            selectedCustomerId: String(selectedCustomerId || "")
        });

        if (errorBox) {
            errorBox.classList.add("d-none");
            errorBox.textContent = "";
        }

        if (showLoading) {
            setLoading(loading, true);
        }

        try {
            const res = await fetch(`${ENDPOINT}?${params.toString()}`, {
                headers: { "X-Requested-With": "XMLHttpRequest" },
                credentials: "same-origin"
            });

            const html = await res.text();
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            container.innerHTML = html;

            Order.emit("order:customerPickerLoaded", {
                owner: activeOwner,
                containerId: "customerPickerContainer"
            });
        } catch (error) {
            console.error("[OrderCustomerModal] load failed:", error);
            if (errorBox) {
                errorBox.textContent = "Cannot load customers. Please try again.";
                errorBox.classList.remove("d-none");
            }
        } finally {
            if (showLoading) {
                setLoading(loading, false);
            }
        }
    }

    async function refreshIfOpen(owner = activeOwner) {
        if (owner !== activeOwner) return;
        if (!Order.modal?.isShown?.("customerPickerModal")) return;

        const current = Number(Order.$("customerPageInput")?.value || 1);
        await load(Math.max(1, current));
    }

    async function openFor(owner = "add") {
        setActiveOwner(owner);
        await waitForHidden(getOwnerModalId(owner));

        const spinnerId = owner === "edit" ? "customerOpenSpinnerEdit" : "customerOpenSpinner";
        const spinner = Order.$(spinnerId);

        setLoading(spinner, true);
        await load(1);
        setLoading(spinner, false);

        Order.modal.show("customerPickerModal");
    }

    async function openCreateFor(owner = "add", spinnerId = "") {
        setActiveOwner(owner);
        await waitForHidden(getOwnerModalId(owner));
        resetCreateForm();

        const spinner = spinnerId ? Order.$(spinnerId) : null;
        setLoading(spinner, true);
        setLoading(spinner, false);

        Order.modal.show("orderCreateCustomerModal");
    }

    async function handleCreateCustomerSubmit(event) {
        event.preventDefault();
        clearCreateErrors();

        const form = event.currentTarget;
        const body = JSON.stringify(buildCustomerPayload(form));

        setCreateSubmitLoading(true);

        try {
            const response = await fetch(form.action || ENDPOINT, {
                method: "POST",
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/json",
                    "X-Requested-With": "XMLHttpRequest",
                    ...getCsrfHeaders(form)
                },
                body,
                credentials: "same-origin"
            });

            let payload = null;
            let rawText = "";
            try {
                rawText = await response.text();
                payload = rawText ? JSON.parse(rawText) : null;
            } catch {
                payload = null;
            }

            if (!response.ok || !payload?.success) {
                showCreateErrors(normalizeErrorMessages(
                    payload,
                    rawText
                        ? `Cannot create customer. Server response: ${rawText.slice(0, 180)}`
                        : `Cannot create customer. Please check the entered information. (HTTP ${response.status})`
                ));
                return;
            }

            if (!selectCreatedCustomer(payload.customer)) {
                return;
            }

            form.reset();
            Order.modal.hide("orderCreateCustomerModal");
        } catch (error) {
            console.error("[OrderCustomerModal] create customer failed:", error);
            showCreateErrors(["Cannot create customer. Please try again."]);
        } finally {
            setCreateSubmitLoading(false);
        }
    }

    function reopenOwnerModalIfNeeded() {
        Order.modal.show(getOwnerModalId(activeOwner));
    }

    function bindEvents() {
        const searchForm = Order.$("customerPickerSearchForm");
        const btnReset = Order.$("btnResetCustomerSearch");
        const btnClose = Order.$("btnCloseCustomerModal");
        const container = Order.$("customerPickerContainer");
        const pickerModalEl = document.getElementById("customerPickerModal");
        const createModalEl = document.getElementById("orderCreateCustomerModal");
        const createForm = Order.$("orderCreateCustomerForm");

        searchForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            await load(1, { showLoading: false });
        });

        btnReset?.addEventListener("click", async () => {
            const form = Order.$("customerPickerSearchForm");
            if (!form) return;

            const keywordInput = form.querySelector("input[name='customerKeyword']");
            if (keywordInput) keywordInput.value = "";

            await load(1, { showLoading: false });
        });

        btnClose?.addEventListener("click", () => {
            Order.modal.hide("customerPickerModal");
        });

        container?.addEventListener("click", async (event) => {
            const pageBtn = event.target.closest("[data-customer-page]");
            if (!pageBtn) return;

            const page = Number(pageBtn.dataset.customerPage || 1);
            await load(Math.max(1, page), { showLoading: false });
        });

        pickerModalEl?.addEventListener("hidden.bs.modal", () => {
            reopenOwnerModalIfNeeded();
        });

        createModalEl?.addEventListener("hidden.bs.modal", () => {
            reopenOwnerModalIfNeeded();
        });

        createForm?.addEventListener("submit", handleCreateCustomerSubmit);
    }

    function init() {
        if (initialized) return;
        initialized = true;
        bindEvents();
    }

    window.OrderCustomerModal = {
        init,
        load,
        openFor,
        openCreateFor,
        refreshIfOpen,
        getActiveOwner: () => activeOwner
    };

    if (Order?.ready) {
        Order.ready(init);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
