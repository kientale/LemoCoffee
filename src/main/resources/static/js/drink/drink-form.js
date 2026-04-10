(() => {
    const Drink = window.Drink || {};
    let initialized = false;

    const RULES = {
        name: /^(?=.{2,100}$)[\p{L}0-9]+(?:\s[\p{L}0-9]+)*$/u,
        descMax: 255,
        maxImageBytes: 5 * 1024 * 1024,
        status: /^(AVAILABLE|UNAVAILABLE)$/
    };

    const CLIENT_ERROR_BY_FORM = {
        createDrinkForm: {
            boxId: "createDrinkClientErrors",
            listId: "createDrinkClientErrorsList"
        },
        editDrinkForm: {
            boxId: "editDrinkClientErrors",
            listId: "editDrinkClientErrorsList"
        }
    };

    function getClientErrorRefs(formId) {
        const config = CLIENT_ERROR_BY_FORM[formId];
        if (!config) return { box: null, list: null };

        return {
            box: document.getElementById(config.boxId),
            list: document.getElementById(config.listId)
        };
    }

    function clearClientErrors(formId) {
        const { box, list } = getClientErrorRefs(formId);
        if (!box || !list) return;

        list.innerHTML = "";
        box.classList.add("d-none");
    }

    function showClientErrors(formId, errors = []) {
        const { box, list } = getClientErrorRefs(formId);
        if (!box || !list || !errors.length) return;

        list.innerHTML = "";
        errors.forEach((message) => {
            const li = document.createElement("li");
            li.textContent = message;
            list.appendChild(li);
        });

        box.classList.remove("d-none");
    }

    function ensureFeedbackEl(input) {
        let feedback = input.parentElement.querySelector(".invalid-feedback");
        if (feedback) return feedback;

        feedback = document.createElement("div");
        feedback.className = "invalid-feedback";
        input.insertAdjacentElement("afterend", feedback);
        return feedback;
    }

    function setValid(input) {
        input.classList.remove("is-invalid");
        input.classList.add("is-valid");
        ensureFeedbackEl(input).textContent = "";
        return true;
    }

    function setInvalid(input, message) {
        input.classList.remove("is-valid");
        input.classList.add("is-invalid");
        ensureFeedbackEl(input).textContent = message || "Invalid value";
        return false;
    }

    function clearValidationState(input) {
        input.classList.remove("is-valid", "is-invalid");
        ensureFeedbackEl(input).textContent = "";
        return true;
    }

    function validateName(input) {
        const value = (input.value || "").trim();
        if (!value) return setInvalid(input, "Drink name is required");
        if (!RULES.name.test(value)) {
            return setInvalid(input, "Name must be 2-100 characters, letters/numbers, single spaces only");
        }
        return setValid(input);
    }

    function validatePrice(input) {
        const raw = (input.value || "").trim();
        if (!raw) return setInvalid(input, "Price is required");

        const value = Number(raw);
        if (!Number.isFinite(value)) return setInvalid(input, "Price must be a valid number");
        if (value <= 0) return setInvalid(input, "Price must be greater than 0");

        return setValid(input);
    }

    function validateDescription(input) {
        const text = (input.value || "").trim();
        if (!text) return clearValidationState(input);

        if (text.length > RULES.descMax) {
            return setInvalid(input, `Description must be <= ${RULES.descMax} characters`);
        }
        return setValid(input);
    }

    function validateStatus(input) {
        const value = (input.value || "").trim();
        if (!value) return setInvalid(input, "Status is required");
        if (!RULES.status.test(value)) return setInvalid(input, "Invalid status");
        return setValid(input);
    }

    function validateImage(input, required) {
        const file = input.files?.[0];
        if (!file) return required ? setInvalid(input, "Image is required") : clearValidationState(input);

        if (!file.type?.startsWith("image/")) return setInvalid(input, "Invalid image type");
        if (file.size > RULES.maxImageBytes) return setInvalid(input, "Image size must be <= 5MB");

        return setValid(input);
    }

    function validateQtyInput(input) {
        const raw = (input.value || "").trim();
        if (!raw) return setInvalid(input, "Quantity is required");

        const quantity = Number(raw);
        if (!Number.isFinite(quantity)) return setInvalid(input, "Quantity must be a number");
        if (quantity <= 0) return setInvalid(input, "Quantity must be greater than 0");

        return setValid(input);
    }

    function getSelectedCountForCreate() {
        const selected = window.AddDrink?.getSelected?.();
        if (Array.isArray(selected)) return selected.length;

        const hidden = document.getElementById("selectedIngredientsJson");
        try {
            const parsed = JSON.parse(hidden?.value || "[]");
            return Array.isArray(parsed) ? parsed.length : 0;
        } catch {
            return 0;
        }
    }

    function resetFormVisualState(form) {
        if (!form) return;

        form.querySelectorAll(".is-valid, .is-invalid").forEach((el) => {
            el.classList.remove("is-valid", "is-invalid");
        });

        form.querySelectorAll(".invalid-feedback").forEach((el) => {
            el.textContent = "";
        });

        clearClientErrors(form.id);
    }

    function bindValidation(form, { requireImage = false } = {}) {
        if (!form) return;

        const nameInput = form.querySelector('[name="name"]');
        const priceInput = form.querySelector('[name="price"]');
        const descInput = form.querySelector('[name="description"]');
        const statusInput = form.querySelector('[name="status"]');
        const imageInput = form.querySelector('[name="image"]');

        nameInput?.addEventListener("input", () => validateName(nameInput));
        priceInput?.addEventListener("input", () => validatePrice(priceInput));
        descInput?.addEventListener("input", () => validateDescription(descInput));
        statusInput?.addEventListener("change", () => validateStatus(statusInput));
        imageInput?.addEventListener("change", () => validateImage(imageInput, requireImage));

        form.addEventListener("submit", (event) => {
            clearClientErrors(form.id);

            let isValid = true;
            const errors = [];

            if (nameInput) isValid = validateName(nameInput) && isValid;
            if (priceInput) isValid = validatePrice(priceInput) && isValid;
            if (descInput) isValid = validateDescription(descInput) && isValid;
            if (statusInput && statusInput.tagName === "SELECT") {
                isValid = validateStatus(statusInput) && isValid;
            }
            if (imageInput) isValid = validateImage(imageInput, requireImage) && isValid;

            const qtyInputs = Array.from(form.querySelectorAll(".selected-qty"));
            qtyInputs.forEach((qtyInput) => {
                isValid = validateQtyInput(qtyInput) && isValid;
            });

            if (form.id === "createDrinkForm" && getSelectedCountForCreate() === 0) {
                isValid = false;
                errors.push("A drink must contain at least one selected ingredient.");
            }

            if (errors.length) {
                showClientErrors(form.id, errors);
                getClientErrorRefs(form.id).box?.scrollIntoView({ behavior: "smooth", block: "start" });
            }

            if (!isValid) {
                event.preventDefault();
                event.stopPropagation();

                const firstInvalidQty = form.querySelector(".selected-qty.is-invalid");
                firstInvalidQty?.focus();
                firstInvalidQty?.scrollIntoView({ behavior: "smooth", block: "center" });
                return;
            }

            if (form.id === "createDrinkForm") {
                window.AddDrink?.saveToHidden?.();
            }
        });
    }

    function bindAddModalReset(addForm) {
        const addModal = document.getElementById("addDrinkModal");
        if (!addModal || !addForm) return;

        addModal.addEventListener("show.bs.modal", () => {
            resetFormVisualState(addForm);
        });

        addModal.addEventListener("hidden.bs.modal", () => {
            addForm.reset();
            resetFormVisualState(addForm);

            window.AddDrink?.clear?.();

            const hiddenJson = document.getElementById("selectedIngredientsJson");
            if (hiddenJson) hiddenJson.value = "[]";
        });
    }

    function init() {
        if (initialized) return;
        initialized = true;

        const addForm = document.getElementById("createDrinkForm") || document.querySelector("#addDrinkModal form");
        const editForm = document.getElementById("editDrinkForm") || document.querySelector("#editDrinkModal form");

        bindValidation(addForm, { requireImage: true });
        bindValidation(editForm, { requireImage: false });
        bindAddModalReset(addForm);
    }

    window.DrinkForm = {
        init,
        validateQtyInput
    };

    if (Drink.ready) {
        Drink.ready(init);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
