document.addEventListener("DOMContentLoaded", () => {
    const video = document.getElementById("dashboardVideo");
    if (!video) return;

    const playPromise = video.play();
    if (playPromise && typeof playPromise.catch === "function") {
        playPromise.catch(() => {
            video.controls = true;
        });
    }
});