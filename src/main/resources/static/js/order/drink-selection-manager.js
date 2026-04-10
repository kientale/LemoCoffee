/**
 * Drink selection manager shared by add/edit order forms.
 */
(() => {
    const Order = (window.Order = window.Order || {});

    if (typeof Order.emit !== "function") {
        Order.emit = (name, detail) => {
            document.dispatchEvent(new CustomEvent(name, { detail }));
        };
    }

    function createOrderDrinkSelectionManager(config) {
        let initialized = false;

        const CONFIG = {
            SEL_PAGE_SIZE: Math.max(1, Number(config.pageSize) || 3)
        };

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

        function normalizePrice(rawPrice) {
            const n = Number(rawPrice);
            return Number.isFinite(n) && n >= 0 ? n : 0;
        }

        function normalizeQuantity(rawQty) {
            const n = Math.floor(Number(rawQty));
            return Number.isFinite(n) && n > 0 ? n : 1;
        }

        function normalizeDrink(item) {
            if (!item) return null;

            const id = Number(item.id ?? item.drinkId ?? item.value);
            if (!Number.isFinite(id) || id <= 0) return null;

            return {
                id,
                name: String(item.name ?? item.drinkName ?? item.label ?? "").trim(),
                price: normalizePrice(item.price ?? item.unitPrice),
                quantity: normalizeQuantity(item.quantity)
            };
        }

        function loadFromHidden() {
            const hidden = getEl(config.hiddenJsonId);
            try {
                const arr = JSON.parse(hidden?.value || "[]");
                if (!Array.isArray(arr)) {
                    STATE.selected = [];
                    return;
                }

                STATE.selected = arr
                    .map(normalizeDrink)
                    .filter((x) => !!x);
            } catch {
                STATE.selected = [];
            }
        }

        function saveToHidden() {
            const hidden = getEl(config.hiddenJsonId);
            if (!hidden) return;

            hidden.value = JSON.stringify(
                STATE.selected.map((d) => ({
                    id: d.id,
                    drinkId: d.id,
                    name: d.name,
                    drinkName: d.name,
                    price: d.price,
                    unitPrice: d.price,
                    quantity: d.quantity
                }))
            );
        }

        function rebuildHiddenInputs(form) {
            if (!form) return;

            let holder = getEl(config.hiddenInputsHolderId);
            if (!holder) {
                holder = document.createElement("div");
                holder.id = config.hiddenInputsHolderId;
                form.appendChild(holder);
            }

            holder.innerHTML = "";
        }

        function syncHiddenInputs() {
            saveToHidden();
            rebuildHiddenInputs(getForm());
        }

        function getSelectedIdsCsv() {
            return STATE.selected.map((x) => x.id).join(",");
        }

        function emitSelectedChanged() {
            Order.emit("order:drinkSelectedChanged", {
                owner: config.owner,
                ids: getSelectedIdsCsv(),
                selected: STATE.selected
            });
        }

        function formatCurrency(value) {
            const safeValue = normalizePrice(value);
            try {
                return `${new Intl.NumberFormat("vi-VN", {
                    maximumFractionDigits: 0
                }).format(safeValue)} đ`;
            } catch {
                return `${Math.round(safeValue)} đ`;
            }
        }

        function getSubtotal(drink) {
            return normalizePrice(drink.price) * normalizeQuantity(drink.quantity);
        }

        function getTotalAmount() {
            return STATE.selected.reduce((sum, d) => sum + getSubtotal(d), 0);
        }

        function renderTotal() {
            const totalEl = getEl(config.totalPreviewId);
            if (!totalEl) return;
            totalEl.textContent = formatCurrency(getTotalAmount());
        }

        function updateRowSubtotal(row, item) {
            if (!row || !item) return;
            const subtotalCell = row.querySelector(".col-subtotal");
            if (!subtotalCell) return;
            subtotalCell.textContent = formatCurrency(getSubtotal(item));
        }

        function updateQtyControlState(row, qty) {
            if (!row) return;
            const decreaseBtn = row.querySelector(".qty-decrease");
            if (decreaseBtn) {
                decreaseBtn.disabled = normalizeQuantity(qty) <= 1;
            }
        }

        function applyQtyDelta(row, qtyInput, delta) {
            if (!row || !qtyInput) return;

            const id = Number(qtyInput.dataset.id);
            const currentQty = normalizeQuantity(qtyInput.value);
            const safeQty = normalizeQuantity(currentQty + delta);

            qtyInput.value = String(safeQty);
            updateQtyControlState(row, safeQty);

            if (!updateQuantity(id, safeQty)) return;

            updateRowSubtotal(row, findById(id));
            emitSelectedChanged();
        }

        function findById(id) {
            return STATE.selected.find((x) => Number(x.id) === Number(id));
        }

        function render() {
            const tbody = getEl(config.tbodyId);
            const emptyRow = getEl(config.emptyRowId);
            const pager = getEl(config.pagerId);
            const pageInfo = getEl(config.pageInfoId);
            const prevBtn = getEl(config.prevBtnId);
            const nextBtn = getEl(config.nextBtnId);
            const tpl = getEl(config.rowTplId);

            if (!tbody || !emptyRow || !pager || !pageInfo || !prevBtn || !nextBtn || !tpl) {
                syncHiddenInputs();
                renderTotal();
                return;
            }

            [...tbody.querySelectorAll("tr")].forEach((tr) => {
                if (tr.id !== config.emptyRowId) tr.remove();
            });

            if (!STATE.selected.length) {
                emptyRow.classList.remove("d-none");
                pager.classList.add("d-none");
                pageInfo.textContent = "1 / 1";
                prevBtn.disabled = true;
                nextBtn.disabled = true;
                syncHiddenInputs();
                renderTotal();
                return;
            }

            emptyRow.classList.add("d-none");

            const totalPages = Math.max(
                1,
                Math.ceil(STATE.selected.length / CONFIG.SEL_PAGE_SIZE)
            );

            STATE.page = Math.min(Math.max(STATE.page, 1), totalPages);

            const start = (STATE.page - 1) * CONFIG.SEL_PAGE_SIZE;
            const pageItems = STATE.selected.slice(start, start + CONFIG.SEL_PAGE_SIZE);

            pageItems.forEach((drink, idx) => {
                const node = tpl.content.cloneNode(true);
                const row = node.querySelector("tr");
                const qtyInput = node.querySelector(".selected-drink-qty");
                const removeBtn = node.querySelector(".btn-remove-selected-drink");

                if (row) {
                    row.dataset.id = String(drink.id);
                }

                node.querySelector(".col-no").textContent = String(start + idx + 1);
                node.querySelector(".col-name").textContent = drink.name;
                node.querySelector(".col-price").textContent = formatCurrency(drink.price);
                node.querySelector(".col-subtotal").textContent = formatCurrency(getSubtotal(drink));

                if (qtyInput) {
                    qtyInput.dataset.id = String(drink.id);
                    qtyInput.value = String(drink.quantity);
                }

                if (removeBtn) {
                    removeBtn.dataset.id = String(drink.id);
                }

                const decreaseBtn = node.querySelector(".qty-decrease");
                const increaseBtn = node.querySelector(".qty-increase");

                if (decreaseBtn && row && qtyInput) {
                    decreaseBtn.addEventListener("click", (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        applyQtyDelta(row, qtyInput, -1);
                    });
                }

                if (increaseBtn && row && qtyInput) {
                    increaseBtn.addEventListener("click", (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        applyQtyDelta(row, qtyInput, 1);
                    });
                }

                tbody.appendChild(node);

                if (row) {
                    updateQtyControlState(row, drink.quantity);
                }
            });

            pageInfo.textContent = `${STATE.page} / ${totalPages}`;
            prevBtn.disabled = STATE.page <= 1;
            nextBtn.disabled = STATE.page >= totalPages;
            pager.classList.toggle("d-none", STATE.selected.length <= CONFIG.SEL_PAGE_SIZE);

            syncHiddenInputs();
            renderTotal();
        }

        function add(item) {
            const normalized = normalizeDrink(item);
            if (!normalized) return false;

            const exists = STATE.selected.some((x) => Number(x.id) === Number(normalized.id));
            if (exists) return false;

            STATE.selected.unshift(normalized);
            STATE.page = 1;
            render();
            emitSelectedChanged();
            return true;
        }

        function remove(id) {
            const before = STATE.selected.length;
            STATE.selected = STATE.selected.filter((x) => Number(x.id) !== Number(id));
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

        function updateQuantity(id, rawQty) {
            const item = findById(id);
            if (!item) return false;

            item.quantity = normalizeQuantity(rawQty);
            syncHiddenInputs();
            renderTotal();
            return true;
        }

        async function handleOpenDrinkPicker() {
            if (typeof config.openDrinkPicker === "function") {
                await config.openDrinkPicker();
                return;
            }

            await window.OrderDrinkModal?.openFor?.(config.owner);
        }

        function bindEvents() {
            const modal = getEl(config.modalId);
            const form = getForm();

            modal?.addEventListener("click", async (e) => {
                if (e.target.closest(`#${config.prevBtnId}`)) {
                    STATE.page--;
                    render();
                    return;
                }

                if (e.target.closest(`#${config.nextBtnId}`)) {
                    STATE.page++;
                    render();
                    return;
                }

                if (e.target.closest(`#${config.clearBtnId}`)) {
                    clear();
                    return;
                }

                if (e.target.closest(`#${config.openBtnId}`)) {
                    e.preventDefault();
                    await handleOpenDrinkPicker();
                    return;
                }

                const removeBtn = e.target.closest(".btn-remove-selected-drink");
                if (removeBtn) {
                    remove(Number(removeBtn.dataset.id));
                    return;
                }

            });

            modal?.addEventListener("keydown", (e) => {
                const qtyInput = e.target.closest(".selected-drink-qty");
                if (!qtyInput) return;

                if (e.key === "ArrowUp" || e.key === "ArrowDown") {
                    e.preventDefault();
                }
            });

            modal?.addEventListener("input", (e) => {
                const qtyInput = e.target.closest(".selected-drink-qty");
                if (!qtyInput) return;

                const id = Number(qtyInput.dataset.id);
                const raw = (qtyInput.value || "").trim();
                if (!raw) return;

                const parsed = Math.floor(Number(raw));
                if (!Number.isFinite(parsed)) return;

                const safeQty = normalizeQuantity(parsed);
                if (String(safeQty) !== qtyInput.value) {
                    qtyInput.value = String(safeQty);
                }

                if (!updateQuantity(id, safeQty)) return;

                const row = qtyInput.closest("tr");
                updateRowSubtotal(row, findById(id));
                updateQtyControlState(row, safeQty);
            });

            modal?.addEventListener("change", (e) => {
                const qtyInput = e.target.closest(".selected-drink-qty");
                if (!qtyInput) return;

                const id = Number(qtyInput.dataset.id);
                const safeQty = normalizeQuantity(qtyInput.value);
                qtyInput.value = String(safeQty);

                if (!updateQuantity(id, safeQty)) return;

                const row = qtyInput.closest("tr");
                updateRowSubtotal(row, findById(id));
                updateQtyControlState(row, safeQty);
                emitSelectedChanged();
            });

            form?.addEventListener("submit", () => {
                syncHiddenInputs();
            });

            form?.addEventListener("reset", () => {
                setTimeout(() => {
                    STATE.selected = [];
                    STATE.page = 1;
                    render();
                    emitSelectedChanged();
                }, 0);
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
            rebuildHiddenInputs,
            updateQuantity,
            getSelectedIdsCsv,
            getSelected: () => STATE.selected,
            getSelectedCount: () => STATE.selected.length
        };
    }

    window.createOrderDrinkSelectionManager = createOrderDrinkSelectionManager;
})();
