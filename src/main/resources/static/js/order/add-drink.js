(() => {
    const Order = window.Order;

    let initialized = false;
    let activeOwner = "add";

    const endpointFromDom = document.getElementById("drinkPickerContainer")?.dataset?.endpoint || "";
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
        window.OrderDrinkPicker?.setOwner?.(activeOwner);
    }

    function getActiveModule() {
        return activeOwner === "edit"
            ? window.EditOrderDrinkManager
            : window.AddOrderDrinkManager;
    }

    function getSearchValues() {
        const form = Order.$("drinkPickerSearchForm");
        if (!form) return { keyword: "" };

        const keyword = (form.querySelector("input[name='drinkKeyword']")?.value || "").trim();
        return { keyword };
    }

    async function load(page = 1, options = {}) {
        const container = Order.$("drinkPickerContainer");
        const loading = Order.$("drinkPickerLoading");
        const errorBox = Order.$("drinkPickerError");
        if (!container) return;

        const showLoading = options.showLoading !== false;
        const { keyword } = getSearchValues();

        const drinkPageInput = Order.$("drinkPageInput");
        if (drinkPageInput) drinkPageInput.value = String(page);

        const selectedDrinkIds = getActiveModule()?.getSelectedIdsCsv?.() || "";
        const params = new URLSearchParams({
            ajax: "true",
            action: "drinkPicker",
            drinkPage: String(page),
            drinkKeyword: keyword,
            selectedDrinkIds
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

            Order.emit("order:drinkPickerLoaded", {
                owner: activeOwner,
                containerId: "drinkPickerContainer"
            });
        } catch (error) {
            console.error("[OrderDrinkModal] load failed:", error);
            if (errorBox) {
                errorBox.textContent = "Cannot load drinks. Please try again.";
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
        if (!Order.modal?.isShown?.("drinkPickerModal")) return;

        const current = Number(Order.$("drinkPageInput")?.value || 1);
        await load(Math.max(1, current));
    }

    async function openFor(owner = "add") {
        setActiveOwner(owner);
        await waitForHidden(getOwnerModalId(owner));

        const spinnerId = owner === "edit" ? "drinkOpenSpinnerEdit" : "drinkOpenSpinner";
        const spinner = Order.$(spinnerId);

        setLoading(spinner, true);
        await load(1);
        setLoading(spinner, false);

        Order.modal.show("drinkPickerModal");
    }

    function reopenOwnerModalIfNeeded() {
        Order.modal.show(getOwnerModalId(activeOwner));
    }

    function bindEvents() {
        const searchForm = Order.$("drinkPickerSearchForm");
        const btnReset = Order.$("btnResetDrinkSearch");
        const container = Order.$("drinkPickerContainer");
        const pickerModalEl = document.getElementById("drinkPickerModal");

        searchForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            await load(1, { showLoading: false });
        });

        btnReset?.addEventListener("click", async () => {
            const form = Order.$("drinkPickerSearchForm");
            if (!form) return;

            const keywordInput = form.querySelector("input[name='drinkKeyword']");
            if (keywordInput) keywordInput.value = "";

            await load(1, { showLoading: false });
        });

        container?.addEventListener("click", async (event) => {
            const pageBtn = event.target.closest("[data-drink-page]");
            if (!pageBtn) return;

            const page = Number(pageBtn.dataset.drinkPage || 1);
            await load(Math.max(1, page), { showLoading: false });
        });

        pickerModalEl?.addEventListener("hidden.bs.modal", () => {
            reopenOwnerModalIfNeeded();
        });
    }

    function init() {
        if (initialized) return;
        initialized = true;
        bindEvents();
    }

    window.OrderDrinkModal = {
        init,
        load,
        openFor,
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
