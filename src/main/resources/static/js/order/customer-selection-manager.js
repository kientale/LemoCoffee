(() => {
    const Order = (window.Order = window.Order || {});

    if (typeof Order.emit !== "function") {
        Order.emit = (name, detail) => {
            document.dispatchEvent(new CustomEvent(name, { detail }));
        };
    }

    function createCustomerSelectionManager(config) {
        let initialized = false;
        const state = { selected: null };

        function $(id) {
            return document.getElementById(id);
        }

        function getEl(id) {
            return id ? $(id) : null;
        }

        function getForm() {
            return getEl(config.formId);
        }

        function getSelectedIdInput() {
            return getEl(config.selectedCustomerIdId);
        }

        function getSelectedNameInput() {
            return getEl(config.selectedCustomerNameId);
        }

        function normalizeCustomer(item) {
            if (!item) return null;

            const rawId = item.id ?? item.customerId ?? item.value;
            const id = Number(rawId);
            if (!Number.isFinite(id) || id <= 0) return null;

            const name = item.name ?? item.customerName ?? item.fullName ?? item.label ?? "";
            return { id, name: String(name).trim() };
        }

        function loadFromInputs() {
            const idInput = getSelectedIdInput();
            const nameInput = getSelectedNameInput();

            const id = Number(idInput?.value || 0);
            const name = String(nameInput?.value || "").trim();

            if (!Number.isFinite(id) || id <= 0) {
                state.selected = null;
                return;
            }

            state.selected = { id, name };
        }

        function saveToInputs() {
            const idInput = getSelectedIdInput();
            const nameInput = getSelectedNameInput();

            if (!state.selected) {
                if (idInput) idInput.value = "";
                if (nameInput) nameInput.value = "";
                return;
            }

            if (idInput) idInput.value = String(state.selected.id);
            if (nameInput) nameInput.value = state.selected.name || "";
        }

        function emitSelectedChanged() {
            Order.emit("order:customerSelectedChanged", {
                owner: config.owner,
                customer: state.selected,
                customerId: state.selected?.id ?? null,
                customerName: state.selected?.name ?? ""
            });
        }

        function render() {
            saveToInputs();
            if (typeof config.onRender === "function") {
                config.onRender(state.selected);
            }
        }

        function select(item) {
            const customer = normalizeCustomer(item);
            if (!customer) return false;

            state.selected = customer;
            render();
            emitSelectedChanged();
            return true;
        }

        function clear() {
            state.selected = null;
            render();
            emitSelectedChanged();
        }

        async function handleOpenCustomerPicker() {
            if (typeof config.openCustomerPicker === "function") {
                await config.openCustomerPicker();
                return;
            }

            await window.OrderCustomerModal?.openFor?.(config.owner);
        }

        function bindEvents() {
            const modal = getEl(config.modalId);
            const form = getForm();

            modal?.addEventListener("click", async (event) => {
                if (event.target.closest(`#${config.openBtnId}`)) {
                    event.preventDefault();
                    await handleOpenCustomerPicker();
                    return;
                }

                if (event.target.closest(`#${config.clearBtnId}`)) {
                    event.preventDefault();
                    clear();
                }
            });

            form?.addEventListener("submit", () => {
                saveToInputs();
            });
        }

        function init() {
            if (initialized) return;
            initialized = true;

            loadFromInputs();
            render();
            bindEvents();
        }

        return {
            init,
            select,
            setSelected: select,
            clear,
            render,
            getSelected: () => state.selected,
            getSelectedId: () => state.selected?.id ?? null,
            getSelectedName: () => state.selected?.name ?? "",
            hasSelected: () => !!state.selected
        };
    }

    window.createCustomerSelectionManager = createCustomerSelectionManager;
})();
