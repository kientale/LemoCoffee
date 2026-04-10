(() => {
    const Order = window.Order || {};

    let initialized = false;
    let activeOwner = "add";

    const ERROR_TARGET = {
        add: { boxId: "createOrderClientErrors", listId: "createOrderClientErrorsList" },
        edit: { boxId: "updateOrderClientErrors", listId: "updateOrderClientErrorsList" }
    };

    function setOwner(owner) {
        activeOwner = owner === "edit" ? "edit" : "add";
    }

    function getOwnerModule() {
        return activeOwner === "edit"
            ? window.EditOrderCustomerManager
            : window.AddOrderCustomerManager;
    }

    function getErrorRefs() {
        const cfg = ERROR_TARGET[activeOwner] || ERROR_TARGET.add;
        return {
            box: document.getElementById(cfg.boxId),
            list: document.getElementById(cfg.listId)
        };
    }

    function showClientErrors(messages = []) {
        const { box, list } = getErrorRefs();
        if (!box || !list || !messages.length) return;

        list.innerHTML = "";
        messages.forEach((message) => {
            const li = document.createElement("li");
            li.textContent = message;
            list.appendChild(li);
        });

        box.classList.remove("d-none");
    }

    function getSelectedCustomerId() {
        return Number(getOwnerModule()?.getSelectedId?.() || 0);
    }

    function setPickButtonState(button, selected) {
        if (!button) return;

        if (selected) {
            button.disabled = true;
            button.classList.remove("btn-success");
            button.classList.add("btn-outline-secondary");
            button.textContent = "Selected";
            return;
        }

        button.disabled = false;
        button.classList.remove("btn-outline-secondary");
        button.classList.add("btn-success");
        button.textContent = "Select Customer";
    }

    function syncSelectedState(container) {
        if (!container) return;

        const selectedId = getSelectedCustomerId();

        container.querySelectorAll("tbody tr").forEach((row) => {
            row.classList.remove("self-row");
        });

        container.querySelectorAll(".btn-pick-customer").forEach((button) => {
            const id = Number(button.dataset.id || 0);
            const selected = selectedId > 0 && id === selectedId;

            setPickButtonState(button, selected);
            button.closest("tr")?.classList.toggle("self-row", selected);
        });
    }

    function bindPickEvents() {
        const container = Order.$?.("customerPickerContainer") || document.getElementById("customerPickerContainer");
        if (!container) return;

        container.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-pick-customer");
            if (!button) return;

            const id = Number(button.dataset.id || 0);
            const name = (button.dataset.name || "").trim();
            if (!id) {
                showClientErrors(["Invalid customer."]);
                return;
            }

            const manager = getOwnerModule();
            if (!manager?.select) {
                showClientErrors(["Cannot determine target form."]);
                return;
            }

            manager.select({ id, name });
            syncSelectedState(container);
            Order.modal?.hide?.("customerPickerModal");
        });
    }

    function bindSyncEvents() {
        const onEvent = Order.on || ((eventName, handler) => document.addEventListener(eventName, handler));

        onEvent("order:customerPickerLoaded", (event) => {
            setOwner(event.detail?.owner);

            const containerId = event.detail?.containerId || "customerPickerContainer";
            const container = document.getElementById(containerId);
            if (!container) return;

            syncSelectedState(container);
        });

        onEvent("order:customerSelectedChanged", (event) => {
            setOwner(event.detail?.owner);

            const container = document.getElementById("customerPickerContainer");
            if (!container) return;

            syncSelectedState(container);
        });
    }

    function init() {
        if (initialized) return;
        initialized = true;
        bindPickEvents();
        bindSyncEvents();
    }

    window.OrderCustomerPicker = {
        setOwner,
        syncSelectedState
    };

    if (Order.ready) {
        Order.ready(init);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
