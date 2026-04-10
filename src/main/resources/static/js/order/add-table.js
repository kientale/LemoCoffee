(() => {
    const Order = window.Order;

    let initialized = false;
    let activeOwner = "add";

    const endpointFromDom = document.getElementById("tablePickerContainer")?.dataset?.endpoint || "";
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
        window.OrderTablePicker?.setOwner?.(activeOwner);
    }

    function getActiveModule() {
        return activeOwner === "edit"
            ? (window.EditOrderTableManager || window.EditOrder)
            : (window.AddOrderTableManager || window.AddOrder);
    }

    function getSearchValues() {
        const form = Order.$("tablePickerSearchForm");
        if (!form) return { keyword: "" };

        const keyword = (form.querySelector("input[name='tableKeyword']")?.value || "").trim();
        return { keyword };
    }

    async function load(page = 1, options = {}) {
        const container = Order.$("tablePickerContainer");
        const loading = Order.$("tablePickerLoading");
        const errorBox = Order.$("tablePickerError");
        if (!container) return;
        const showLoading = options.showLoading !== false;

        const { keyword } = getSearchValues();

        const tablePageInput = Order.$("tablePageInput");
        if (tablePageInput) tablePageInput.value = String(page);

        const currentModule = getActiveModule();
        const selectedTableId = currentModule?.getSelectedId?.() || currentModule?.getSelectedTableId?.() || "";

        const params = new URLSearchParams({
            ajax: "true",
            action: "tablePicker",
            tablePage: String(page),
            tableKeyword: keyword,
            selectedTableId: String(selectedTableId || "")
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

            Order.emit("order:tablePickerLoaded", {
                owner: activeOwner,
                containerId: "tablePickerContainer"
            });
        } catch (error) {
            console.error("[OrderTableModal] load failed:", error);
            if (errorBox) {
                errorBox.textContent = "Cannot load tables. Please try again.";
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
        if (!Order.modal?.isShown?.("tablePickerModal")) return;

        const current = Number(Order.$("tablePageInput")?.value || 1);
        await load(Math.max(1, current));
    }

    async function openFor(owner = "add") {
        setActiveOwner(owner);
        await waitForHidden(getOwnerModalId(owner));

        const spinnerId = owner === "edit" ? "tableOpenSpinnerEdit" : "tableOpenSpinner";
        const spinner = Order.$(spinnerId);

        setLoading(spinner, true);
        await load(1);
        setLoading(spinner, false);

        Order.modal.show("tablePickerModal");
    }

    function reopenOwnerModalIfNeeded() {
        Order.modal.show(getOwnerModalId(activeOwner));
    }

    function bindEvents() {
        const searchForm = Order.$("tablePickerSearchForm");
        const btnReset = Order.$("btnResetTableSearch");
        const btnClose = Order.$("btnCloseTableModal");
        const container = Order.$("tablePickerContainer");
        const pickerModalEl = document.getElementById("tablePickerModal");

        searchForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            await load(1, { showLoading: false });
        });

        btnReset?.addEventListener("click", async () => {
            const form = Order.$("tablePickerSearchForm");
            if (!form) return;

            const keywordInput = form.querySelector("input[name='tableKeyword']");
            if (keywordInput) keywordInput.value = "";

            await load(1, { showLoading: false });
        });

        btnClose?.addEventListener("click", () => {
            Order.modal.hide("tablePickerModal");
        });

        container?.addEventListener("click", async (event) => {
            const pageBtn = event.target.closest("[data-table-page]");
            if (!pageBtn) return;

            const page = Number(pageBtn.dataset.tablePage || 1);
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

    window.OrderTableModal = {
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

