(() => {
    "use strict";

    function togglePassword(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn?.querySelector("i");

        if (!input) {
            return;
        }

        const isPassword = input.type === "password";
        input.type = isPassword ? "text" : "password";

        if (icon) {
            icon.classList.toggle("fa-eye", !isPassword);
            icon.classList.toggle("fa-eye-slash", isPassword);
        }
    }

    function bindPasswordToggles(scope = document) {
        const buttons = scope.querySelectorAll("[data-toggle-password]");

        buttons.forEach((btn) => {
            btn.addEventListener("click", () => {
                const target = btn.dataset.target;
                if (!target) {
                    return;
                }

                const input = document.querySelector(target);
                if (!input) {
                    return;
                }

                const icon = btn.querySelector("i");
                const isPassword = input.type === "password";

                input.type = isPassword ? "text" : "password";

                if (icon) {
                    icon.classList.toggle("fa-eye", !isPassword);
                    icon.classList.toggle("fa-eye-slash", isPassword);
                }
            });
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        bindPasswordToggles();
    });

    window.togglePassword = togglePassword;
    window.bindPasswordToggles = bindPasswordToggles;
})();