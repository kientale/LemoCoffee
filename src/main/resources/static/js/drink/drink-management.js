(() => {
    const Drink = (window.Drink ||= {});
    const ctx = window.CTX || "";

    function $(id) {
        return document.getElementById(id);
    }

    function emit(name, detail) {
        document.dispatchEvent(new CustomEvent(name, { detail }));
    }

    function on(name, handler) {
        document.addEventListener(name, handler);
    }

    function ready(handler) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", handler);
            return;
        }
        handler();
    }

    function getModalEl(id) {
        if (!id || !window.bootstrap?.Modal) return null;
        return $(id);
    }

    const modal = {
        show(id) {
            const el = getModalEl(id);
            if (!el) return;
            bootstrap.Modal.getOrCreateInstance(el).show();
        },
        hide(id) {
            const el = getModalEl(id);
            if (!el) return;
            bootstrap.Modal.getOrCreateInstance(el).hide();
        },
        isShown(id) {
            const el = $(id);
            return !!(el && el.classList.contains("show"));
        }
    };

    Object.assign(Drink, {
        CTX: ctx,
        ENDPOINT: window.DRINK_ENDPOINT || `${ctx}/drink-management`,
        $,
        emit,
        on,
        ready,
        modal
    });

    window.openConfirmModal = ({
                                   title = "Confirm Action",
                                   message = "Are you sure?",
                                   action = "",
                                   drinkId = "",
                                   btnClass = "btn-danger",
                                   formAction = ""
                               } = {}) => {
        const titleEl = $("confirmModalTitle");
        const messageEl = $("confirmModalMessage");
        const actionInput = $("confirmActionInput");
        const idInput = $("confirmDrinkIdInput");
        const form = $("confirmActionForm");
        const confirmBtn = $("confirmActionBtn");

        if (titleEl) titleEl.textContent = title;
        if (messageEl) messageEl.textContent = message;
        if (actionInput) actionInput.value = action;
        if (idInput) idInput.value = drinkId;
        if (formAction && form) form.action = formAction;
        if (confirmBtn) confirmBtn.className = `btn ${btnClass}`;

        modal.show("confirmActionModal");
    };
})();
