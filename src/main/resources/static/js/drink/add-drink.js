(() => {
    function openAddModalIfNeeded() {
        const flags = window.DRINK_MODAL_FLAGS || {};
        if (!flags.showCreateModal) return;

        const modalEl = document.getElementById("addDrinkModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    async function openIngredientPickerFromAdd() {
        if (!window.DrinkIngredientModal?.openFor) {
            console.error("[AddDrink] DrinkIngredientModal is not available");
            return;
        }

        await window.DrinkIngredientModal.openFor("add");
    }

    function initAddDrinkModule() {
        if (!window.createDrinkSelectionManager) {
            console.error("[AddDrink] createDrinkSelectionManager is not available");
            return;
        }

        window.AddDrink = window.createDrinkSelectionManager({
            owner: "add",
            pageSize: 3,
            formId: "createDrinkForm",
            modalId: "addDrinkModal",
            hiddenJsonId: "selectedIngredientsJson",
            tbodyId: "selectedIngredientTbody",
            emptyRowId: "ingredientEmptyRow",
            pagerId: "selectedPager",
            pageInfoId: "selPageInfo",
            prevBtnId: "btnSelPrev",
            nextBtnId: "btnSelNext",
            rowTplId: "selectedIngredientRowTpl",
            openBtnId: "btnOpenIngModal",
            clearBtnId: "btnClearIngredients",
            openIngredientPicker: openIngredientPickerFromAdd,
            afterInit: openAddModalIfNeeded
        });

        window.AddDrink.init();
    }

    const Drink = window.Drink;
    if (Drink?.ready) {
        Drink.ready(initAddDrinkModule);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initAddDrinkModule);
    } else {
        initAddDrinkModule();
    }
})();
