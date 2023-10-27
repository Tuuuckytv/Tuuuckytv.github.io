document.addEventListener("DOMContentLoaded", function () {
    const hole = document.querySelector(".hole");
    const content = document.querySelector(".content");

    setTimeout(function () {
        hole.style.transform = "scale(1)";
        content.style.transform = "scale(1)";
    }, 1000); // Adjust the delay (in milliseconds) as needed
});
