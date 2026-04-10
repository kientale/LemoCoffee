(() => {
    function openDetailModalIfNeeded() {
        const flags = window.DRINK_MODAL_FLAGS || {};
        if (!flags.showDetailModal) return;

        const modalEl = document.getElementById("drinkDetailModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    const Drink = window.Drink;
    if (Drink?.ready) {
        Drink.ready(openDetailModalIfNeeded);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", openDetailModalIfNeeded);
    } else {
        openDetailModalIfNeeded();
    }
})();
