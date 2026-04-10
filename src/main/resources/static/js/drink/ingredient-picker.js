(() => {
    "use strict";

    const Drink = window.Drink || {};
    let initialized = false;
    let activeOwner = "add";

    const ERROR_TARGET = {
        add: {
            boxId: "createDrinkClientErrors",
            listId: "createDrinkClientErrorsList"
        },
        edit: {
            boxId: "editDrinkClientErrors",
            listId: "editDrinkClientErrorsList"
        }
    };

    function setOwner(owner) {
        activeOwner = owner === "edit" ? "edit" : "add";
    }

    function getOwnerModule() {
        return activeOwner === "edit" ? window.EditDrink : window.AddDrink;
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
        button.textContent = "Add to Drink";
    }

    function syncSelectedState(container) {
        if (!container) return;

        const selectedSet = parseSelectedSet();

        container.querySelectorAll("tbody tr").forEach((row) => {
            row.classList.remove("self-row");
        });

        container.querySelectorAll(".btn-pick-ingredient").forEach((button) => {
            const id = String(Number(button.dataset.id || 0));
            const selected = selectedSet.has(id);

            setPickButtonState(button, selected);
            button.closest("tr")?.classList.toggle("self-row", selected);
        });
    }

    function bindPickEvents() {
        const container = Drink.$?.("ingredientPickerContainer") || document.getElementById("ingredientPickerContainer");
        if (!container) return;

        container.addEventListener("click", (event) => {
            const button = event.target.closest(".btn-pick-ingredient");
            if (!button) return;

            const id = Number(button.dataset.id || 0);
            if (!id) {
                showClientErrors(["Invalid ingredient."]);
                return;
            }

            const ownerModule = getOwnerModule();
            if (!ownerModule?.add) {
                showClientErrors(["Cannot determine target form."]);
                return;
            }

            const added = ownerModule.add({
                id,
                name: button.dataset.name || "",
                unit: button.dataset.unit || "",
                quantity: 1
            });

            if (!added) return;
            syncSelectedState(container);
        });
    }

    function bindSyncEvents() {
        const onEvent = Drink.on || ((eventName, handler) => document.addEventListener(eventName, handler));

        onEvent("drink:pickerLoaded", (event) => {
            setOwner(event.detail?.owner);

            const containerId = event.detail?.containerId || "ingredientPickerContainer";
            const container = document.getElementById(containerId);
            if (!container) return;

            syncSelectedState(container);
        });

        onEvent("drink:selectedChanged", (event) => {
            setOwner(event.detail?.owner);
            const container = document.getElementById("ingredientPickerContainer");
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

    window.DrinkIngredientPicker = {
        setOwner,
        syncSelectedState
    };

    if (Drink.ready) {
        Drink.ready(init);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
