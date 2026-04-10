(() => {
    const REGEX = {
        fullName: /^[\p{L}]+(?:\s[\p{L}]+)*$/u,
        phone: /^[0-9]{9,11}$/
    };

    function init() {
        bindRealtimeValidation("#addCustomerModal form");
        bindRealtimeValidation("#editCustomerModal form");
    }

    function openConfirmModal(button) {
        const modalEl = document.getElementById("confirmActionModal");
        const modalTitle = document.getElementById("confirmModalTitle");
        const modalMessage = document.getElementById("confirmModalMessage");
        const actionInput = document.getElementById("confirmActionInput");
        const customerIdInput = document.getElementById("confirmCustomerIdInput");
        const confirmBtn = document.getElementById("confirmActionBtn");

        if (!button || !modalEl || !modalTitle || !modalMessage || !actionInput || !customerIdInput || !confirmBtn) {
            console.error("Confirm modal DOM missing.");
            return;
        }

        const title = button.dataset.confirmTitle || "Confirm Action";
        const message = button.dataset.confirmMessage || "Are you sure?";
        const action = button.dataset.confirmAction || "";
        const customerId = button.dataset.confirmUserId || "";
        const btnClass = button.dataset.confirmBtnClass || "btn-danger";

        modalTitle.textContent = title;
        modalMessage.textContent = message;
        actionInput.value = action;
        customerIdInput.value = customerId;

        confirmBtn.className = `btn ${btnClass}`;
        confirmBtn.textContent = "Confirm";

        if (window.bootstrap?.Modal) {
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    }

    function bindRealtimeValidation(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        const fullNameInput = form.querySelector('[name="fullName"]');
        const phoneInput = form.querySelector('[name="phone"]');

        fullNameInput?.addEventListener("input", () => validateFullName(fullNameInput));
        phoneInput?.addEventListener("input", () => validatePhone(phoneInput));

        form.addEventListener("submit", (event) => {
            let valid = true;

            if (fullNameInput) {
                validateFullName(fullNameInput);
                valid = valid && !fullNameInput.classList.contains("is-invalid");
            }

            if (phoneInput) {
                validatePhone(phoneInput);
                valid = valid && !phoneInput.classList.contains("is-invalid");
            }

            if (!valid) {
                event.preventDefault();
                event.stopPropagation();
            }
        });
    }

    function validateFullName(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Full name is required");
        }

        if (!REGEX.fullName.test(value)) {
            return setInvalid(input, "Full name only contains letters and spaces");
        }

        setValid(input);
    }

    function validatePhone(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Phone is required");
        }

        if (!REGEX.phone.test(value)) {
            return setInvalid(input, "Phone must be 9-11 digits");
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

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    window.CustomerManagement = {
        openConfirmModal
    };

    window.openConfirmModal = openConfirmModal;
})();
