(() => {
    function openEditModalIfNeeded() {
        const flags = window.ORDER_MODAL_FLAGS || {};
        if (!flags.showEditModal) return;

        const modalEl = document.getElementById("editOrderModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    function initEditOrderModule() {
        if (!window.createTableSelectionManager) {
            console.error("[EditOrder] createTableSelectionManager is not available");
            openEditModalIfNeeded();
            return;
        }

        if (!window.createOrderDrinkSelectionManager) {
            console.error("[EditOrder] createOrderDrinkSelectionManager is not available");
            openEditModalIfNeeded();
            return;
        }

        if (!window.createCustomerSelectionManager) {
            console.error("[EditOrder] createCustomerSelectionManager is not available");
            openEditModalIfNeeded();
            return;
        }

        window.EditOrderTableManager = window.createTableSelectionManager({
            owner: "edit",
            modalId: "editOrderModal",
            formId: "editOrderForm",
            selectedTableIdId: "selectedTableIdEdit",
            selectedTableNameId: "selectedTableNameEdit",
            openBtnId: "btnOpenTableModalEdit",
            clearBtnId: "btnClearTableEdit",
            openTablePicker: async () => {
                await window.OrderTableModal?.openFor?.("edit");
            }
        });

        window.EditOrderDrinkManager = window.createOrderDrinkSelectionManager({
            owner: "edit",
            pageSize: 3,
            formId: "editOrderForm",
            modalId: "editOrderModal",
            hiddenJsonId: "selectedDrinksJsonEdit",
            hiddenInputsHolderId: "selectedDrinkHiddenInputsEdit",
            tbodyId: "selectedDrinkTbodyEdit",
            emptyRowId: "drinkEmptyRowEdit",
            pagerId: "selectedDrinkPagerEdit",
            pageInfoId: "selDrinkPageInfoEdit",
            prevBtnId: "btnSelDrinkPrevEdit",
            nextBtnId: "btnSelDrinkNextEdit",
            rowTplId: "selectedDrinkRowTplEdit",
            openBtnId: "btnOpenDrinkModalEdit",
            clearBtnId: "btnClearSelectedDrinksEdit",
            totalPreviewId: "orderTotalPreviewEdit",
            openDrinkPicker: async () => {
                await window.OrderDrinkModal?.openFor?.("edit");
            }
        });

        window.EditOrderCustomerManager = window.createCustomerSelectionManager({
            owner: "edit",
            modalId: "editOrderModal",
            formId: "editOrderForm",
            selectedCustomerIdId: "selectedCustomerIdEdit",
            selectedCustomerNameId: "selectedCustomerNameEdit",
            openBtnId: "btnOpenCustomerModalEdit",
            clearBtnId: "btnClearCustomerEdit",
            openCustomerPicker: async () => {
                await window.OrderCustomerModal?.openFor?.("edit");
            }
        });

        window.EditOrderTableManager.init();
        window.EditOrderDrinkManager.init();
        window.EditOrderCustomerManager.init();

        document.getElementById("btnOpenNewCustomerModalEdit")?.addEventListener("click", async (event) => {
            event.preventDefault();
            await window.OrderCustomerModal?.openCreateFor?.("edit", "newCustomerOpenSpinnerEdit");
        });

        openEditModalIfNeeded();
    }

    const Order = window.Order;
    if (Order?.ready) {
        Order.ready(initEditOrderModule);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initEditOrderModule);
    } else {
        initEditOrderModule();
    }
})();
