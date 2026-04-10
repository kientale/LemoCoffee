(() => {
    function createDrinkSelectionManager(config) {
        let initialized = false;

        const PAGE_SIZE = Math.max(1, Number(config.pageSize) || 3);
        const STATE = {
            selected: [],
            page: 1
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

        function normalizeQuantity(rawQty) {
            const quantity = Number(rawQty);
            return Number.isFinite(quantity) && quantity > 0 ? quantity : 1;
        }

        function normalizeItem(item) {
            if (!item) return null;

            const id = Number(item.id);
            if (!Number.isFinite(id) || id <= 0) return null;

            return {
                id,
                name: String(item.name || "").trim(),
                unit: String(item.unit || "").trim(),
                quantity: normalizeQuantity(item.quantity)
            };
        }

        function loadFromHidden() {
            const hidden = getEl(config.hiddenJsonId);

            try {
                const data = JSON.parse(hidden?.value || "[]");
                if (!Array.isArray(data)) {
                    STATE.selected = [];
                    return;
                }

                STATE.selected = data
                    .map(normalizeItem)
                    .filter(Boolean);
            } catch {
                STATE.selected = [];
            }
        }

        function saveToHidden() {
            const hidden = getEl(config.hiddenJsonId);
            if (!hidden) return;
            hidden.value = JSON.stringify(STATE.selected);
        }

        function syncHiddenInputs() {
            saveToHidden();
        }

        function getSelectedIdsCsv() {
            return STATE.selected.map((item) => item.id).join(",");
        }

        function emitSelectedChanged() {
            const Drink = window.Drink;
            if (!Drink?.emit) return;

            Drink.emit("drink:selectedChanged", {
                owner: config.owner,
                ids: getSelectedIdsCsv(),
                selected: STATE.selected
            });
        }

        function removeRenderedRows(tbody) {
            [...tbody.querySelectorAll("tr")].forEach((tr) => {
                if (tr.id !== config.emptyRowId) tr.remove();
            });
        }

        function updatePager({ info, prevBtn, nextBtn, pager }, totalPages) {
            info.textContent = `${STATE.page} / ${totalPages}`;
            prevBtn.disabled = STATE.page <= 1;
            nextBtn.disabled = STATE.page >= totalPages;
            pager.classList.toggle("d-none", STATE.selected.length <= PAGE_SIZE);
        }

        function render() {
            const tbody = getEl(config.tbodyId);
            const emptyRow = getEl(config.emptyRowId);
            const pager = getEl(config.pagerId);
            const info = getEl(config.pageInfoId);
            const prevBtn = getEl(config.prevBtnId);
            const nextBtn = getEl(config.nextBtnId);
            const rowTemplate = getEl(config.rowTplId);

            if (!tbody || !emptyRow || !pager || !info || !prevBtn || !nextBtn || !rowTemplate) {
                syncHiddenInputs();
                return;
            }

            removeRenderedRows(tbody);

            if (!STATE.selected.length) {
                emptyRow.classList.remove("d-none");
                pager.classList.add("d-none");
                info.textContent = "1 / 1";
                prevBtn.disabled = true;
                nextBtn.disabled = true;
                syncHiddenInputs();
                return;
            }

            emptyRow.classList.add("d-none");

            const totalPages = Math.max(1, Math.ceil(STATE.selected.length / PAGE_SIZE));
            STATE.page = Math.min(Math.max(STATE.page, 1), totalPages);

            const start = (STATE.page - 1) * PAGE_SIZE;
            const items = STATE.selected.slice(start, start + PAGE_SIZE);

            items.forEach((item, index) => {
                const node = rowTemplate.content.cloneNode(true);
                const qtyInput = node.querySelector(".selected-qty");
                const removeBtn = node.querySelector(".btn-remove-selected");

                node.querySelector(".col-no").textContent = String(start + index + 1);
                node.querySelector(".col-name").textContent = item.name;
                node.querySelector(".col-unit").textContent = item.unit;

                if (qtyInput) {
                    qtyInput.dataset.id = String(item.id);
                    qtyInput.value = String(item.quantity);
                    window.DrinkForm?.validateQtyInput?.(qtyInput);
                }

                if (removeBtn) {
                    removeBtn.dataset.id = String(item.id);
                }

                tbody.appendChild(node);
            });

            updatePager({ info, prevBtn, nextBtn, pager }, totalPages);
            syncHiddenInputs();
        }

        function findById(id) {
            return STATE.selected.find((item) => Number(item.id) === Number(id));
        }

        function setQuantity(id, rawQty) {
            const item = findById(id);
            if (!item) return false;

            item.quantity = normalizeQuantity(rawQty);
            syncHiddenInputs();
            return true;
        }

        function add(rawItem) {
            const item = normalizeItem(rawItem);
            if (!item) return false;

            const alreadyExists = STATE.selected.some((selected) => Number(selected.id) === Number(item.id));
            if (alreadyExists) return false;

            STATE.selected.unshift(item);
            STATE.page = 1;
            render();
            emitSelectedChanged();
            return true;
        }

        function remove(id) {
            const before = STATE.selected.length;
            STATE.selected = STATE.selected.filter((item) => Number(item.id) !== Number(id));
            if (STATE.selected.length === before) return;

            render();
            emitSelectedChanged();
        }

        function clear() {
            if (!STATE.selected.length) {
                render();
                return;
            }

            STATE.selected = [];
            STATE.page = 1;
            render();
            emitSelectedChanged();
        }

        async function handleOpenIngredientPicker() {
            if (typeof config.openIngredientPicker === "function") {
                await config.openIngredientPicker();
                return;
            }

            await window.DrinkIngredientModal?.openFor?.(config.owner);
        }

        function bindEvents() {
            const modal = getEl(config.modalId);
            const form = getForm();

            modal?.addEventListener("click", async (event) => {
                if (event.target.closest(`#${config.prevBtnId}`)) {
                    STATE.page -= 1;
                    render();
                    return;
                }

                if (event.target.closest(`#${config.nextBtnId}`)) {
                    STATE.page += 1;
                    render();
                    return;
                }

                if (event.target.closest(`#${config.clearBtnId}`)) {
                    clear();
                    return;
                }

                if (event.target.closest(`#${config.openBtnId}`)) {
                    event.preventDefault();
                    await handleOpenIngredientPicker();
                    return;
                }

                const removeBtn = event.target.closest(".btn-remove-selected");
                if (removeBtn) {
                    remove(Number(removeBtn.dataset.id));
                }
            });

            modal?.addEventListener("input", (event) => {
                const qtyInput = event.target.closest(".selected-qty");
                if (!qtyInput) return;

                const id = Number(qtyInput.dataset.id);
                if (!id) return;

                const isValid = window.DrinkForm?.validateQtyInput?.(qtyInput);
                if (!isValid) return;

                setQuantity(id, qtyInput.value);
            });

            form?.addEventListener("submit", () => {
                syncHiddenInputs();
            });
        }

        function init() {
            if (initialized) return;
            initialized = true;

            loadFromHidden();
            render();
            bindEvents();

            if (typeof config.afterInit === "function") {
                config.afterInit();
            }
        }

        return {
            init,
            add,
            remove,
            clear,
            render,
            saveToHidden,
            getSelectedIdsCsv,
            getSelected: () => STATE.selected
        };
    }

    window.createDrinkSelectionManager = createDrinkSelectionManager;
})();
