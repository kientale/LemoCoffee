(() => {
    function openDetailModalIfNeeded() {
        const flags = window.ORDER_MODAL_FLAGS || {};
        if (!flags.showDetailModal) return;

        const modalEl = document.getElementById("orderDetailModal");
        if (!modalEl || !window.bootstrap?.Modal) return;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    const Order = window.Order;
    if (Order?.ready) {
        Order.ready(openDetailModalIfNeeded);
    } else if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", openDetailModalIfNeeded);
    } else {
        openDetailModalIfNeeded();
    }
})();
