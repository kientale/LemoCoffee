(() => {
    const Order = window.Order || {};

    let initialized = false;
    let activeOwner = "add";

    const ERROR_TARGET = {
        add: {
            boxId: "createOrderClientErrors",
            listId: "createOrderClientErrorsList"
        },
        edit: {
            boxId: "updateOrderClientErrors",
            listId: "updateOrderClientErrorsList"
        }
    };

    function setOwner(owner) {
        activeOwner = owner === "edit" ? "edit" : "add";
    }

    function getOwnerModule() {
        return activeOwner === "edit" ? window.EditOrderTableManager : window.AddOrderTableManager;
    }

    function getErrorRefs() {
        const config = ERROR_TARGET[activeOwner] || ERROR_TARGET.add;
        return {
            box: document.getElementById(config.boxId),
            list: document.getElementById(config.listId)
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

    function getSelectedTableId() {
        return Number(getOwnerModule()?.getSelectedId?.() || getOwnerModule()?.getSelectedTableId?.() || 0);
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
        button.textContent = "Select Table";
    }

    function syncSelectedState(container) {
        if (!container) return;

        const selectedId = getSelectedTableId();

        container.querySelectorAll("tbody tr").forEach((row) => {
            row.classList.remove("self-row");
        });

        container.querySelectorAll(".btn-pick-table").forEach((button) => {
            const id = Number(button.dataset.id || 0);
            const selected = selectedId > 0 && id === selectedId;

            setPickButtonState(button, selected);
            button.closest("tr")?.classList.toggle("self-row", selected);
        });
    }

    function bindPickEvents() {
        const container = Order.$?.("tablePickerContainer") || document.getElementById("tablePickerContainer");
        if (!container) return;

        container.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-pick-table");
            if (!button) return;

            const id = Number(button.dataset.id || 0);
            const name = (button.dataset.name || "").trim();
            if (!id) {
                showClientErrors(["Invalid table."]);
                return;
            }

            const manager = getOwnerModule();
            if (!manager?.select) {
                showClientErrors(["Cannot determine target form."]);
                return;
            }

            manager.select({ id, name });
            syncSelectedState(container);
            Order.modal?.hide?.("tablePickerModal");
        });
    }

    function bindSyncEvents() {
        const onEvent = Order.on || ((eventName, handler) => document.addEventListener(eventName, handler));

        onEvent("order:tablePickerLoaded", (event) => {
            setOwner(event.detail?.owner);

            const containerId = event.detail?.containerId || "tablePickerContainer";
            const container = document.getElementById(containerId);
            if (!container) return;

            syncSelectedState(container);
        });

        onEvent("order:tableSelectedChanged", (event) => {
            setOwner(event.detail?.owner);

            const container = document.getElementById("tablePickerContainer");
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

    window.OrderTablePicker = {
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

