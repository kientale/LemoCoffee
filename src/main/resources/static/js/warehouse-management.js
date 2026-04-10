(() => {
    const REGEX = {
        nameOrSupplier: /^(?=.{2,100}$)[\p{L}0-9]+(?:\s[\p{L}0-9]+)*$/u,
        unit: /^(?=.{1,20}$)[\p{L}0-9]+$/u,
        quantity: /^\d+(\.\d+)?$/,
        status: /^(ACTIVE|DELETED)$/
    };

    const RULES = {
        descriptionMax: 255
    };

    function init() {
        bindRealtimeValidation("#addIngredientModal form");
        bindRealtimeValidation("#editIngredientModal form");
    }

    function openConfirmModal(button) {
        const modalEl = document.getElementById("confirmActionModal");
        const modalTitle = document.getElementById("confirmModalTitle");
        const modalMessage = document.getElementById("confirmModalMessage");
        const actionInput = document.getElementById("confirmActionInput");
        const ingredientIdInput = document.getElementById("confirmIngredientIdInput");
        const confirmBtn = document.getElementById("confirmActionBtn");

        if (!button || !modalEl || !modalTitle || !modalMessage || !actionInput || !ingredientIdInput || !confirmBtn) {
            console.error("Confirm modal DOM missing.");
            return;
        }

        const title = button.dataset.confirmTitle || "Confirm Action";
        const message = button.dataset.confirmMessage || "Are you sure?";
        const action = button.dataset.confirmAction || "";
        const ingredientId = button.dataset.confirmIngredientId || "";
        const btnClass = button.dataset.confirmBtnClass || "btn-danger";

        modalTitle.textContent = title;
        modalMessage.textContent = message;
        actionInput.value = action;
        ingredientIdInput.value = ingredientId;

        confirmBtn.className = `btn ${btnClass}`;
        confirmBtn.textContent = "Confirm";

        if (window.bootstrap?.Modal) {
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    }

    function bindRealtimeValidation(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        const fields = {
            name: form.querySelector('[name="name"]'),
            quantity: form.querySelector('[name="quantity"]'),
            unit: form.querySelector('[name="unit"]'),
            supplier: form.querySelector('[name="supplier"]'),
            description: form.querySelector('[name="description"]'),
            status: form.querySelector('[name="status"]')
        };

        fields.name?.addEventListener("input", () => validateName(fields.name));
        fields.quantity?.addEventListener("input", () => validateQuantity(fields.quantity));
        fields.unit?.addEventListener("input", () => validateUnit(fields.unit));
        fields.supplier?.addEventListener("input", () => validateSupplier(fields.supplier));
        fields.description?.addEventListener("input", () => validateDescription(fields.description));
        fields.status?.addEventListener("change", () => validateStatus(fields.status));

        form.addEventListener("submit", (event) => {
            let valid = true;

            valid = validateIfPresent(fields.name, validateName) && valid;
            valid = validateIfPresent(fields.quantity, validateQuantity) && valid;
            valid = validateIfPresent(fields.unit, validateUnit) && valid;
            valid = validateIfPresent(fields.supplier, validateSupplier) && valid;
            valid = validateIfPresent(fields.description, validateDescription) && valid;

            if (fields.status && fields.status.tagName === "SELECT") {
                valid = validateIfPresent(fields.status, validateStatus) && valid;
            }

            if (!valid) {
                event.preventDefault();
                event.stopPropagation();
            }
        });
    }

    function validateIfPresent(input, validator) {
        if (!input) return true;
        validator(input);
        return !input.classList.contains("is-invalid");
    }

    function validateName(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Ingredient name is required");
        }

        if (!REGEX.nameOrSupplier.test(value)) {
            return setInvalid(input, "Name must be 2-100 chars, letters/numbers, single spaces only");
        }

        setValid(input);
    }

    function validateSupplier(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Supplier is required");
        }

        if (!REGEX.nameOrSupplier.test(value)) {
            return setInvalid(input, "Supplier must be 2-100 chars, letters/numbers, single spaces only");
        }

        setValid(input);
    }

    function validateUnit(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Unit is required");
        }

        if (!REGEX.unit.test(value)) {
            return setInvalid(input, "Unit must be 1-20 chars, only letters/numbers (no spaces)");
        }

        setValid(input);
    }

    function validateQuantity(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Quantity is required");
        }

        if (!REGEX.quantity.test(value)) {
            return setInvalid(input, "Quantity must be a valid number (e.g. 1 or 1.5)");
        }

        const numberValue = Number(value);
        if (!Number.isFinite(numberValue) || numberValue <= 0) {
            return setInvalid(input, "Quantity must be greater than 0");
        }

        setValid(input);
    }

    function validateDescription(input) {
        const value = input.value.trim();
        if (value.length > RULES.descriptionMax) {
            return setInvalid(input, `Description must be at most ${RULES.descriptionMax} characters`);
        }

        setValid(input);
    }

    function validateStatus(select) {
        if (!select.value) {
            return setInvalid(select, "Status is required");
        }

        if (!REGEX.status.test(select.value)) {
            return setInvalid(select, "Invalid status value");
        }

        setValid(select);
    }

    function ensureFeedbackEl(input) {
        if (!input) return null;

        if (input.closest(".input-group")) {
            const inputGroup = input.closest(".input-group");
            let feedback = inputGroup.parentElement.querySelector(".invalid-feedback");

            if (!feedback) {
                feedback = document.createElement("div");
                feedback.className = "invalid-feedback";
                inputGroup.insertAdjacentElement("afterend", feedback);
            }

            return feedback;
        }

        let feedback = input.parentElement.querySelector(".invalid-feedback");

        if (!feedback) {
            feedback = document.createElement("div");
            feedback.className = "invalid-feedback";
            input.insertAdjacentElement("afterend", feedback);
        }

        return feedback;
    }

    function setValid(input) {
        input.classList.remove("is-invalid");
        input.classList.add("is-valid");

        const feedback = ensureFeedbackEl(input);
        if (feedback) {
            feedback.textContent = "";
        }
    }

    function setInvalid(input, message) {
        input.classList.remove("is-valid");
        input.classList.add("is-invalid");

        const feedback = ensureFeedbackEl(input);
        if (feedback) {
            feedback.textContent = message || "Invalid value";
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    window.WarehouseManagement = {
        openConfirmModal
    };

    window.openConfirmModal = openConfirmModal;
})();
