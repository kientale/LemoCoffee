/**
 * Module điều khiển modal chọn Ingredient
 */
(() => {
    const Drink = window.Drink;

    let initialized = false;
    let activeOwner = "add";

    const ENDPOINT =
        window.DRINK_ENDPOINT || ((window.CTX || "") + "/drink-management");

    function setLoading(el, show) {
        if (!el) return;
        el.classList.toggle("d-none", !show);
    }

    function setActiveOwner(owner) {
        activeOwner = owner === "edit" ? "edit" : "add";
        window.DrinkIngredientPicker?.setOwner?.(activeOwner);
    }

    function getActiveModule() {
        return activeOwner === "edit" ? window.EditDrink : window.AddDrink;
    }

    function getSearchValues() {
        const form = Drink.$("ingredientPickerSearchForm");
        if (!form) return { keyword: "", field: "name" };

        const keyword = (form.querySelector("input[name='ingKeyword']")?.value || "").trim();
        const field = form.querySelector("select[name='ingField']")?.value || "name";
        return { keyword, field };
    }

    async function load(page = 1, options = {}) {
        const container = Drink.$("ingredientPickerContainer");
        const loading = Drink.$("ingredientPickerLoading");
        const errorBox = Drink.$("ingredientPickerError");
        if (!container) return;
        const showLoading = options.showLoading !== false;

        const { keyword, field } = getSearchValues();

        const ingPageInput = Drink.$("ingPageInput");
        if (ingPageInput) ingPageInput.value = String(page);

        const currentModule = getActiveModule();

        const params = new URLSearchParams({
            ajax: "true",
            action: "ingredientPicker",
            ingPage: String(page),
            ingKeyword: keyword,
            ingField: field,
            selectedIds: currentModule?.getSelectedIdsCsv?.() || ""
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

            Drink.emit("drink:pickerLoaded", {
                owner: activeOwner,
                containerId: "ingredientPickerContainer"
            });
        } catch (err) {
            console.error("[DrinkIngredientModal] load failed:", err);
            if (errorBox) {
                errorBox.textContent = "Cannot load ingredients. Please try again.";
                errorBox.classList.remove("d-none");
            }
        } finally {
            if (showLoading) {
                setLoading(loading, false);
            }
        }
    }

    async function openFor(owner = "add") {
        setActiveOwner(owner);

        const spinnerId = owner === "edit" ? "ingOpenSpinnerEdit" : "ingOpenSpinner";
        const spinner = Drink.$(spinnerId);

        setLoading(spinner, true);
        await load(1);
        setLoading(spinner, false);

        Drink.modal.show("addDrinkIngredientModal");
    }

    function reopenEditModalIfNeeded() {
        if (activeOwner !== "edit") return;

        const editModalEl = document.getElementById("editDrinkModal");
        if (!editModalEl || !window.bootstrap?.Modal) return;

        const editModal = bootstrap.Modal.getOrCreateInstance(editModalEl);
        editModal.show();
    }

    function bindEvents() {
        const searchForm = Drink.$("ingredientPickerSearchForm");
        const btnReset = Drink.$("btnResetIngSearch");
        const pickerModalEl = document.getElementById("addDrinkIngredientModal");

        searchForm?.addEventListener("submit", async (e) => {
            e.preventDefault();
            await load(1, { showLoading: false });
        });

        btnReset?.addEventListener("click", async () => {
            const form = Drink.$("ingredientPickerSearchForm");
            if (!form) return;

            const kw = form.querySelector("input[name='ingKeyword']");
            const fd = form.querySelector("select[name='ingField']");
            if (kw) kw.value = "";
            if (fd) fd.value = "name";

            await load(1, { showLoading: false });
        });

        pickerModalEl?.addEventListener("click", async (e) => {
            const pageBtn = e.target.closest("[data-ing-page]");
            if (!pageBtn) return;

            const page = Number(pageBtn.dataset.ingPage || 1);
            await load(Math.max(1, page), { showLoading: false });
        });

        pickerModalEl?.addEventListener("hidden.bs.modal", () => {
            reopenEditModalIfNeeded();
        });
    }

    function init() {
        if (initialized) return;
        initialized = true;
        bindEvents();
    }

    window.DrinkIngredientModal = {
        init,
        load,
        openFor
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
