body {
    font-family: Arial, sans-serif;
    text-align: center;
    background-color: #F1F5F9;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100vh;
    justify-content: flex-start; /* Align content to the top */
}

.header {
    background-color: #01A0C0;
    color: white;
    padding: 30px;
    font-size: 24px;
    width: 100%;
    text-align: center; /* Center the text */
}

.header img {
    display: inline; /* Make the image inline with text */
    vertical-align: middle; /* Align the image vertically with text */
    margin-right: 10px; /* Add some spacing between the image and text */
    height: 30px; /* Adjust the image height to 30px */
}

.countdown-timers {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    margin-top: 20px;
}

.countdown-timer {
    background-color: #EC008C;
    color: white;
    border-radius: 5px;
    padding: 10px;
    font-size: 20px;
    text-align: center;
    transition: background-color 0.5s;
}

.countdown-label {
    font-size: 18px;
    margin-top: 10px;
}

.button {
    position: absolute;
    bottom: 20px;
    right: 20px;
    background-color: #01A0C0;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    font-size: 18px;
    text-decoration: none;
}
