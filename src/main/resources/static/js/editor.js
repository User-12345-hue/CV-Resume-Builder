document.querySelectorAll("[data-format]").forEach((button) => {
    button.addEventListener("click", () => {
        const field = document.getElementById(button.dataset.target);
        const marker = button.dataset.format;
        if (!(field instanceof HTMLTextAreaElement) || !marker) {
            return;
        }
        const start = field.selectionStart;
        const end = field.selectionEnd;
        const selected = field.value.slice(start, end) || "your text";
        const wrapped = `${marker}${selected}${marker}`;
        field.setRangeText(wrapped, start, end, "select");
        field.focus();
    });
});
