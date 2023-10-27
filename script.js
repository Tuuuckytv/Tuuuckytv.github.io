document.addEventListener("DOMContentLoaded", function () {
    const hole = document.querySelector(".hole");
    const content = document.querySelector(".content");

    setTimeout(function () {
        hole.style.transform = "scale(1)";
        content.style.transform = "scale(1)";
    }, 1000); // Adjust the delay (in milliseconds) as needed
});

<script>
    // Konami Code script
    const konamiCode = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'b', 'a', 'Enter'];
    let konamiIndex = 0;

    document.addEventListener('keydown', (e) => {
        if (e.key === konamiCode[konamiIndex]) {
            konamiIndex++;

            if (konamiIndex === konamiCode.length) {
                alert('Konami Code activated! 🎮');
                konamiIndex = 0;
            }
        } else {
            konamiIndex = 0;
        }
    });
</script>
