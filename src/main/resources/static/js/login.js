(() => {
    function init() {
        const loginForm = document.getElementById("loginForm");
        if (!loginForm) return;

        if (window.TogglePassword) {
            window.TogglePassword.bindTogglePassword("togglePassword", "password");
        }

        setupClientValidation(loginForm);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

    function setupClientValidation(form) {
        const username = form.querySelector("#username");
        const password = form.querySelector("#password");

        username?.addEventListener("input", () => {
            if (window.AuthValidate) {
                window.AuthValidate.validateUsername(username);
            }
        });

        password?.addEventListener("input", () => {
            if (window.AuthValidate) {
                window.AuthValidate.validateLoginPassword(password);
            }
        });

        form.addEventListener("submit", (e) => {
            let ok = true;

            if (window.AuthValidate) {
                if (username) ok = window.AuthValidate.validateUsername(username) && ok;
                if (password) ok = window.AuthValidate.validateLoginPassword(password) && ok;
            }

            if (!ok) {
                e.preventDefault();
            }
        });
    }
})();