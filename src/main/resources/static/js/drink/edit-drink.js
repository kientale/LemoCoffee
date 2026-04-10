(() => {
    function openEditModalIfNeeded() {
        const flags = window.DRINK_MODAL_FLAGS || {};
        if (!flags.showEditModal) return;

        const modalEl = document.getElementById("editDrinkModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    async function openIngredientPickerFromEdit() {
        const editModalEl = document.getElementById("editDrinkModal");
        if (!editModalEl || !window.bootstrap?.Modal) {
            console.error("[EditDrink] editDrinkModal or bootstrap is missing");
            return;
        }

        const editModal = bootstrap.Modal.getOrCreateInstance(editModalEl);

        editModalEl.addEventListener(
            "hidden.bs.modal",
            async () => {
                await window.DrinkIngredientModal?.openFor?.("edit");
            },
            { once: true }
        );

        editModal.hide();
    }

    function initEditDrinkModule() {
        if (!window.createDrinkSelectionManager) {
            console.error("[EditDrink] createDrinkSelectionManager is not available");
            return;
        }

        window.EditDrink = window.createDrinkSelectionManager({
            owner: "edit",
            pageSize: 3,
            formId: "editDrinkForm",
            modalId: "editDrinkModal",
            hiddenJsonId: "selectedIngredientsJsonEdit",
            tbodyId: "selectedIngredientTbodyEdit",
            emptyRowId: "ingredientEmptyRowEdit",
            pagerId: "selectedPagerEdit",
            pageInfoId: "selPageInfoEdit",
            prevBtnId: "btnSelPrevEdit",
            nextBtnId: "btnSelNextEdit",
            rowTplId: "selectedIngredientRowTplEdit",
            openBtnId: "btnOpenIngModalEdit",
            clearBtnId: "btnClearIngredientsEdit",
            openIngredientPicker: openIngredientPickerFromEdit,
            afterInit: openEditModalIfNeeded
        });

        window.EditDrink.init();
    }

    const Drink = window.Drink;
    if (Drink?.ready) {
        Drink.ready(initEditDrinkModule);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initEditDrinkModule);
    } else {
        initEditDrinkModule();
    }
})();
