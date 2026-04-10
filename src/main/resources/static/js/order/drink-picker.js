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
        return activeOwner === "edit" ? window.EditOrderDrinkManager : window.AddOrderDrinkManager;
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

    function parseSelectedSet() {
        const selectedCsv = getOwnerModule()?.getSelectedIdsCsv?.() || "";

        return new Set(
            selectedCsv
                .split(",")
                .map((id) => String(Number(id)))
                .filter((id) => id !== "0")
        );
    }

    function setPickButtonState(button, selected) {
        if (!button) return;

        if (selected) {
            button.disabled = true;
            button.classList.remove("btn-success");
            button.classList.add("btn-outline-secondary");
            button.textContent = "Added";
            return;
        }

        button.disabled = false;
        button.classList.remove("btn-outline-secondary");
        button.classList.add("btn-success");
        button.textContent = "Add to Order";
    }

    function syncSelectedState(container) {
        if (!container) return;

        const selectedSet = parseSelectedSet();

        container.querySelectorAll(".drink-card").forEach((card) => {
            card.classList.remove("drink-card--selected");
        });

        container.querySelectorAll("tbody tr").forEach((row) => {
            row.classList.remove("self-row");
        });

        container.querySelectorAll(".btn-pick-drink").forEach((button) => {
            const id = String(Number(button.dataset.id || 0));
            const selected = selectedSet.has(id);

            setPickButtonState(button, selected);

            button.closest(".drink-card")?.classList.toggle("drink-card--selected", selected);
            button.closest("tr")?.classList.toggle("self-row", selected);
        });
    }

    function bindPickEvents() {
        const container = Order.$?.("drinkPickerContainer") || document.getElementById("drinkPickerContainer");
        if (!container) return;

        container.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-pick-drink");
            if (!button) return;

            const id = Number(button.dataset.id || 0);
            if (!id) {
                showClientErrors(["Invalid drink."]);
                return;
            }

            const manager = getOwnerModule();
            if (!manager?.add) {
                showClientErrors(["Cannot determine target form."]);
                return;
            }

            const added = manager.add({
                id,
                name: button.dataset.name || "",
                price: Number(button.dataset.price || 0),
                quantity: 1
            });

            if (!added) return;
            syncSelectedState(container);
        });
    }

    function bindSyncEvents() {
        const onEvent = Order.on || ((eventName, handler) => document.addEventListener(eventName, handler));

        onEvent("order:drinkPickerLoaded", (event) => {
            setOwner(event.detail?.owner);

            const containerId = event.detail?.containerId || "drinkPickerContainer";
            const container = document.getElementById(containerId);
            if (!container) return;

            syncSelectedState(container);
        });

        onEvent("order:drinkSelectedChanged", (event) => {
            setOwner(event.detail?.owner);

            const container = document.getElementById("drinkPickerContainer");
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

    window.OrderDrinkPicker = {
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

