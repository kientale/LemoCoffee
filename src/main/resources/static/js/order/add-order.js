(() => {
    function openAddModalIfNeeded() {
        const flags = window.ORDER_MODAL_FLAGS || {};
        if (!flags.showCreateModal) return;

        const modalEl = document.getElementById("addOrderModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    function initAddOrderModule() {
        if (!window.createTableSelectionManager) {
            console.error("[AddOrder] createTableSelectionManager is not available");
            openAddModalIfNeeded();
            return;
        }

        if (!window.createCustomerSelectionManager) {
            console.error("[AddOrder] createCustomerSelectionManager is not available");
            openAddModalIfNeeded();
            return;
        }

        if (!window.createOrderDrinkSelectionManager) {
            console.error("[AddOrder] createOrderDrinkSelectionManager is not available");
            openAddModalIfNeeded();
            return;
        }

        window.AddOrderTableManager = window.createTableSelectionManager({
            owner: "add",
            modalId: "addOrderModal",
            formId: "createOrderForm",
            selectedTableIdId: "selectedTableId",
            selectedTableNameId: "selectedTableName",
            openBtnId: "btnOpenTableModal",
            clearBtnId: "btnClearTable",
            openTablePicker: async () => {
                await window.OrderTableModal?.openFor?.("add");
            }
        });

        window.AddOrderDrinkManager = window.createOrderDrinkSelectionManager({
            owner: "add",
            pageSize: 3,
            formId: "createOrderForm",
            modalId: "addOrderModal",
            hiddenJsonId: "selectedDrinksJson",
            hiddenInputsHolderId: "selectedDrinkHiddenInputs",
            tbodyId: "selectedDrinkTbody",
            emptyRowId: "drinkEmptyRow",
            pagerId: "selectedDrinkPager",
            pageInfoId: "selDrinkPageInfo",
            prevBtnId: "btnSelDrinkPrev",
            nextBtnId: "btnSelDrinkNext",
            rowTplId: "selectedDrinkRowTpl",
            openBtnId: "btnOpenDrinkModal",
            clearBtnId: "btnClearSelectedDrinks",
            totalPreviewId: "orderTotalPreview",
            openDrinkPicker: async () => {
                await window.OrderDrinkModal?.openFor?.("add");
            }
        });

        window.AddOrderCustomerManager = window.createCustomerSelectionManager({
            owner: "add",
            modalId: "addOrderModal",
            formId: "createOrderForm",
            selectedCustomerIdId: "selectedCustomerId",
            selectedCustomerNameId: "selectedCustomerName",
            openBtnId: "btnOpenCustomerModal",
            clearBtnId: "btnClearCustomer",
            openCustomerPicker: async () => {
                await window.OrderCustomerModal?.openFor?.("add");
            }
        });

        window.AddOrderTableManager.init();
        window.AddOrderDrinkManager.init();
        window.AddOrderCustomerManager.init();

        document.getElementById("btnOpenNewCustomerModal")?.addEventListener("click", async (event) => {
            event.preventDefault();
            await window.OrderCustomerModal?.openCreateFor?.("add", "newCustomerOpenSpinner");
        });

        openAddModalIfNeeded();
    }

    const Order = window.Order;
    if (Order?.ready) {
        Order.ready(initAddOrderModule);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initAddOrderModule);
    } else {
        initAddOrderModule();
    }
})();
