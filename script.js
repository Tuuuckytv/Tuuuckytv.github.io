// Konami Code sequence: Up, Up, Down, Down, Left, Right, Left, Right, B, A
const konamiCode = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'b', 'a'];
let konamiCodePosition = 0;

document.addEventListener('keydown', function (event) {
    if (event.key.toLowerCase() === konamiCode[konamiCodePosition]) {
        konamiCodePosition++;
        if (konamiCodePosition === konamiCode.length) {
            // You can replace this alert with any action you want to trigger.
            alert('Konami Code activated!');
            konamiCodePosition = 0; // Reset the position
        }
    } else {
        konamiCodePosition = 0; // Reset if the entered key doesn't match
    }
});
