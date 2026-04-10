(() => {
    const REGEX = {
        username: /^[a-zA-Z0-9_]{4,20}$/,
        fullName: /^[\p{L}]+(?:\s[\p{L}]+)*$/u,
        phone: /^[0-9]{9,11}$/,
        email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
        password: /^(?=.*[A-Za-z])(?=.*\d).{7,}$/
    };

    function init() {
        bindRealtimeValidation("#addUserModal form", { requirePassword: true });
        bindRealtimeValidation("#editUserModal form", { requirePassword: false });
    }

    function togglePassword(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn?.querySelector("i");

        if (!input || !icon) return;

        const isPassword = input.type === "password";
        input.type = isPassword ? "text" : "password";

        icon.classList.toggle("fa-eye", !isPassword);
        icon.classList.toggle("fa-eye-slash", isPassword);
    }

    function openConfirmModal(button) {
        const modalEl = document.getElementById("confirmActionModal");
        const modalTitle = document.getElementById("confirmModalTitle");
        const modalMessage = document.getElementById("confirmModalMessage");
        const actionInput = document.getElementById("confirmActionInput");
        const userIdInput = document.getElementById("confirmUserIdInput");
        const confirmBtn = document.getElementById("confirmActionBtn");

        if (!button || !modalEl || !modalTitle || !modalMessage || !actionInput || !userIdInput || !confirmBtn) {
            console.error("Confirm modal DOM missing.");
            return;
        }

        const title = button.dataset.confirmTitle || "Confirm Action";
        const message = button.dataset.confirmMessage || "Are you sure?";
        const action = button.dataset.confirmAction || "";
        const userId = button.dataset.confirmUserId || "";
        const btnClass = button.dataset.confirmBtnClass || "btn-danger";

        modalTitle.textContent = title;
        modalMessage.textContent = message;
        actionInput.value = action;
        userIdInput.value = userId;

        confirmBtn.className = `btn ${btnClass}`;
        confirmBtn.textContent = "Confirm";

        if (window.bootstrap?.Modal) {
            bootstrap.Modal.getOrCreateInstance(modalEl).show();
        }
    }

    function bindRealtimeValidation(formSelector, options = {}) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        const fields = {
            username: form.querySelector('[name="username"]'),
            fullName: form.querySelector('[name="fullName"]'),
            email: form.querySelector('[name="email"]'),
            phone: form.querySelector('[name="phone"]'),
            role: form.querySelector('[name="role"]'),
            status: form.querySelector('[name="status"]'),
            password: form.querySelector('[name="password"]'),
            confirmPassword: form.querySelector('[name="confirmPassword"]')
        };

        fields.username?.addEventListener("input", () => validateUsername(fields.username));
        fields.fullName?.addEventListener("input", () => validateFullName(fields.fullName));
        fields.email?.addEventListener("input", () => validateEmail(fields.email));
        fields.phone?.addEventListener("input", () => validatePhone(fields.phone));
        fields.role?.addEventListener("change", () => validateRole(fields.role));
        fields.status?.addEventListener("change", () => validateStatus(fields.status));

        if (fields.password) {
            fields.password.addEventListener("input", () => {
                validatePassword(fields.password);

                if (fields.confirmPassword && fields.confirmPassword.value) {
                    validateConfirmPassword(fields.password, fields.confirmPassword);
                }
            });
        }

        if (fields.password && fields.confirmPassword) {
            fields.confirmPassword.addEventListener("input", () => {
                validateConfirmPassword(fields.password, fields.confirmPassword);
            });
        }

        form.addEventListener("submit", (e) => {
            let valid = true;

            valid = validateIfPresent(fields.username, validateUsername) && valid;
            valid = validateIfPresent(fields.fullName, validateFullName) && valid;
            valid = validateIfPresent(fields.email, validateEmail) && valid;
            valid = validateIfPresent(fields.phone, validatePhone) && valid;
            valid = validateIfPresent(fields.role, validateRole) && valid;
            valid = validateIfPresent(fields.status, validateStatus) && valid;

            if (options.requirePassword) {
                valid = validateIfPresent(fields.password, validatePassword) && valid;

                if (fields.password && fields.confirmPassword) {
                    validateConfirmPassword(fields.password, fields.confirmPassword);
                    valid = !fields.confirmPassword.classList.contains("is-invalid") && valid;
                }
            }

            if (!valid) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    }

    function validateIfPresent(input, validator) {
        if (!input) return true;
        validator(input);
        return !input.classList.contains("is-invalid");
    }

    function validateUsername(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Username is required");
        }

        if (!REGEX.username.test(value)) {
            return setInvalid(input, "Username must be 4-20 characters and contain only letters, numbers, or _");
        }

        setValid(input);
    }

    function validateFullName(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Full name is required");
        }

        if (!REGEX.fullName.test(value)) {
            return setInvalid(input, "Full name must contain only letters and spaces");
        }

        setValid(input);
    }

    function validateEmail(input) {
        const value = input.value.trim();

        if (!value) {
            return setInvalid(input, "Email is required");
        }

        if (!REGEX.email.test(value)) {
            return setInvalid(input, "Invalid email format");
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

    function validatePassword(input) {
        const value = input.value;

        if (!value) {
            return setInvalid(input, "Password is required");
        }

        if (!REGEX.password.test(value)) {
            return setInvalid(input, "Password must be at least 7 characters and contain both letters and numbers");
        }

        setValid(input);
    }

    function validateConfirmPassword(passwordInput, confirmPasswordInput) {
        const passwordValue = passwordInput.value;
        const confirmValue = confirmPasswordInput.value;

        if (!confirmValue) {
            return setInvalid(confirmPasswordInput, "Confirm password is required");
        }

        if (passwordValue !== confirmValue) {
            return setInvalid(confirmPasswordInput, "Passwords do not match");
        }

        setValid(confirmPasswordInput);
    }

    function validateRole(select) {
        if (!select.value) {
            return setInvalid(select, "Role is required");
        }

        setValid(select);
    }

    function validateStatus(select) {
        if (!select.value) {
            return setInvalid(select, "Status is required");
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

    window.UserManagement = {
        togglePassword,
        openConfirmModal
    };

    window.togglePassword = togglePassword;
    window.openConfirmModal = openConfirmModal;
})();