(() => {
    const REGEX = {
        username: /^[a-zA-Z0-9_]{4,20}$/
    };

    function getErrorEl(input) {
        const id = input?.id;
        if (!id) return null;
        return document.getElementById(id + "Error");
    }

    function setValid(input) {
        if (!input) return true;

        input.classList.remove("is-invalid");
        input.classList.add("is-valid");

        const err = getErrorEl(input);
        if (err) {
            err.textContent = "";
            err.classList.add("d-none");
        }

        return true;
    }

    function setInvalid(input, msg) {
        if (!input) return false;

        input.classList.remove("is-valid");
        input.classList.add("is-invalid");

        const err = getErrorEl(input);
        if (err) {
            err.textContent = msg || "Invalid value";
            err.classList.remove("d-none");
        }

        return false;
    }

    function clearValidation(input) {
        if (!input) return;

        input.classList.remove("is-valid", "is-invalid");

        const err = getErrorEl(input);
        if (err) {
            err.textContent = "";
            err.classList.add("d-none");
        }
    }

    function validateUsername(input) {
        const value = (input?.value || "").trim();

        if (!value) return setInvalid(input, "Username is required");
        if (!REGEX.username.test(value)) {
            return setInvalid(input, "Username 4-20 chars, only letters/numbers/_");
        }

        return setValid(input);
    }

    function validateLoginPassword(input) {
        const value = (input?.value || "").trim();

        if (!value) return setInvalid(input, "Password is required");

        return setValid(input);
    }

    window.AuthValidate = {
        validateUsername,
        validateLoginPassword,
        setValid,
        setInvalid,
        clearValidation
    };
})();