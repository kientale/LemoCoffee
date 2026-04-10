/**
 * Table Selection Manager
 * Dùng chung cho Add Order / Edit Order
 */
(() => {
    const Order = (window.Order = window.Order || {});

    if (typeof Order.emit !== "function") {
        Order.emit = (name, detail) => {
            document.dispatchEvent(new CustomEvent(name, { detail }));
        };
    }

    function createTableSelectionManager(config) {
        let initialized = false;

        const STATE = {
            selected: null
        };

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
            return getEl(config.selectedTableIdId);
        }

        function getSelectedNameInput() {
            return getEl(config.selectedTableNameId);
        }

        function normalizeTable(item) {
            if (!item) return null;

            const rawId = item.id ?? item.tableId ?? item.value;
            const id = Number(rawId);

            if (!Number.isFinite(id) || id <= 0) return null;

            const name =
                item.name ??
                item.tableName ??
                item.code ??
                item.tableCode ??
                item.label ??
                "";

            return {
                id,
                name: String(name).trim()
            };
        }

        function loadFromInputs() {
            const idInput = getSelectedIdInput();
            const nameInput = getSelectedNameInput();

            const id = Number(idInput?.value || 0);
            const name = String(nameInput?.value || "").trim();

            if (!Number.isFinite(id) || id <= 0) {
                STATE.selected = null;
                return;
            }

            STATE.selected = {
                id,
                name
            };
        }

        function saveToInputs() {
            const idInput = getSelectedIdInput();
            const nameInput = getSelectedNameInput();

            if (!STATE.selected) {
                if (idInput) idInput.value = "";
                if (nameInput) nameInput.value = "";
                return;
            }

            if (idInput) idInput.value = String(STATE.selected.id);
            if (nameInput) nameInput.value = STATE.selected.name || "";
        }

        function render() {
            saveToInputs();

            if (typeof config.onRender === "function") {
                config.onRender(STATE.selected);
            }
        }

        function emitSelectedChanged() {
            Order.emit("order:tableSelectedChanged", {
                owner: config.owner,
                table: STATE.selected,
                tableId: STATE.selected?.id ?? null,
                tableName: STATE.selected?.name ?? ""
            });
        }

        function setSelected(item) {
            const table = normalizeTable(item);
            if (!table) return false;

            STATE.selected = table;
            render();
            emitSelectedChanged();
            return true;
        }

        function clear() {
            STATE.selected = null;
            render();
            emitSelectedChanged();
        }

        async function handleOpenTablePicker() {
            if (typeof config.openTablePicker === "function") {
                await config.openTablePicker();
                return;
            }

            await window.OrderTableModal?.openFor?.(config.owner);
        }

        function bindEvents() {
            const modal = getEl(config.modalId);
            const form = getForm();

            modal?.addEventListener("click", async (e) => {
                if (e.target.closest(`#${config.openBtnId}`)) {
                    e.preventDefault();
                    await handleOpenTablePicker();
                    return;
                }

                if (e.target.closest(`#${config.clearBtnId}`)) {
                    e.preventDefault();
                    clear();
                }
            });

            form?.addEventListener("submit", () => {
                saveToInputs();
            });

            form?.addEventListener("reset", () => {
                setTimeout(() => {
                    STATE.selected = null;
                    render();
                    emitSelectedChanged();
                }, 0);
            });
        }

        function init() {
            if (initialized) return;
            initialized = true;

            loadFromInputs();
            render();
            bindEvents();

            if (typeof config.afterInit === "function") {
                config.afterInit();
            }
        }

        return {
            init,
            setSelected,
            select: setSelected,
            clear,
            render,
            loadFromInputs,
            saveToInputs,
            getSelected: () => STATE.selected,
            getSelectedId: () => STATE.selected?.id ?? null,
            getSelectedName: () => STATE.selected?.name ?? "",
            hasSelected: () => !!STATE.selected
        };
    }

    window.createTableSelectionManager = createTableSelectionManager;
})();
