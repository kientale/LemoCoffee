(() => {
    const REGEX = {
        capacity: /^(?:[1-9]|1[0-9]|20)$/
    };

    function init() {
        bindRealtimeValidation("#addTableModal form");
        bindRealtimeValidation("#editTableModal form");
        bindDropdownState();
    }

    function openConfirmModal(button) {
        const modalEl = document.getElementById("confirmActionModal");
        const modalTitle = document.getElementById("confirmModalTitle");
        const modalMessage = document.getElementById("confirmModalMessage");
        const actionInput = document.getElementById("confirmActionInput");
        const tableIdInput = document.getElementById("confirmTableIdInput");
        const confirmBtn = document.getElementById("confirmActionBtn");

        if (!button || !modalEl || !modalTitle || !modalMessage || !actionInput || !tableIdInput || !confirmBtn) {
            console.error("Confirm modal DOM missing.");
            return;
        }

        const title = button.dataset.confirmTitle || "Confirm Action";
        const message = button.dataset.confirmMessage || "Are you sure?";
        const action = button.dataset.confirmAction || "";
        const tableId = button.dataset.confirmTableId || "";
        const btnClass = button.dataset.confirmBtnClass || "btn-danger";

        modalTitle.textContent = title;
        modalMessage.textContent = message;
        actionInput.value = action;
        tableIdInput.value = tableId;

        confirmBtn.className = `btn ${btnClass}`;
        confirmBtn.textContent = "Confirm";

        if (window.bootstrap?.Modal) {
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    }

    function bindRealtimeValidation(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        const capacityInput = form.querySelector('[name="capacity"]');

        capacityInput?.addEventListener("input", () => validateCapacity(capacityInput));

        form.addEventListener("submit", (event) => {
            let valid = true;
            valid = validateIfPresent(capacityInput, validateCapacity) && valid;

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

    function validateCapacity(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Capacity is required");
        }

        if (!REGEX.capacity.test(value)) {
            return setInvalid(input, "Capacity must be 1-20");
        }

        setValid(input);
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

    function bindDropdownState() {
        document.addEventListener("show.bs.dropdown", (event) => {
            const card = event.target.closest(".table-card");
            if (card) {
                card.classList.add("dropdown-open");
            }
        });

        document.addEventListener("hide.bs.dropdown", (event) => {
            const card = event.target.closest(".table-card");
            if (card) {
                card.classList.remove("dropdown-open");
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    window.TableManagement = {
        openConfirmModal
    };

    window.openConfirmModal = openConfirmModal;
})();
