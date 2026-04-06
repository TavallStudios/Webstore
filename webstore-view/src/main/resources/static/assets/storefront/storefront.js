document.addEventListener("DOMContentLoaded", () => {
    const stickyBars = document.querySelectorAll("[data-sticky-bar]");
    const buyBox = document.querySelector("#buy-box");

    if (stickyBars.length > 0 && buyBox) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                stickyBars.forEach((bar) => {
                    bar.classList.toggle("is-visible", !entry.isIntersecting);
                });
            });
        }, {threshold: 0.2});
        observer.observe(buyBox);
    }

    document.querySelectorAll("[data-scroll-target]").forEach((button) => {
        button.addEventListener("click", () => {
            const target = document.querySelector(button.getAttribute("data-scroll-target"));
            if (target) {
                target.scrollIntoView({behavior: "smooth", block: "start"});
            }
        });
    });
});
