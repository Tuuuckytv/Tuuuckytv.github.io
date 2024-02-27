// JavaScript redirect to remove ".html" extension from URL
if (location.pathname.endsWith('/eat.html')) {
  const newUrl = location.pathname.replace('/eat.html', '/eat');
  window.location.href = newUrl;
}

// Rest of your JavaScript code goes here
async function fetchMealsFromFile(file) {
  try {
    const response = await fetch(file);
    const text = await response.text();
    return text.split('\n').map(line => line.trim()).filter(Boolean);
  } catch (error) {
    console.error('Error fetching meals:', error);
    return [];
  }
}

const meals = {
  breakfast: [],
  dinner: [],
  tea: [],
  payday: []
};

fetchMealsFromFile('breakfast.txt').then(data => {
  meals.breakfast = data;
});

fetchMealsFromFile('dinner.txt').then(data => {
  meals.dinner = data;
});

fetchMealsFromFile('tea.txt').then(data => {
  meals.tea = data;
});

fetchMealsFromFile('payday.txt').then(data => {
  meals.payday = data;
});

function updateCountdown() {
  const countdownElement = document.getElementById('countdown-timer');
  const now = new Date();
  const targetDate = new Date('2025-01-01T00:00:00');
  const timeDifference = targetDate - now;

  if (timeDifference > 0) {
    const days = Math.floor(timeDifference / (1000 * 60 * 60 * 24));
    const hours = Math.floor((timeDifference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((timeDifference % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((timeDifference % (1000 * 60)) / 1000);

    countdownElement.textContent = `${formatTime(days)} : ${formatTime(hours)} : ${formatTime(minutes)} : ${formatTime(seconds)}`;
  } else {
    countdownElement.textContent = '00 : 00 : 00 : 00';
  }
}

function formatTime(time) {
  return time < 10 ? `0${time}` : time;
}

setInterval(updateCountdown, 1000);

let timeoutId;

let lastInteractionTime = Date.now();

function resetTimeout() {
  clearTimeout(timeoutId);

  timeoutId = setTimeout(() => {
    const currentTime = Date.now();
    const timeSinceLastInteraction = currentTime - lastInteractionTime;

    if (timeSinceLastInteraction >= 30000) {
      location.reload();
    } else {
      resetTimeout();
    }
  }, 30000);
}

document.addEventListener('click', () => {
  lastInteractionTime = Date.now();
  resetTimeout();
});

document.addEventListener('keydown', () => {
  lastInteractionTime = Date.now();
  resetTimeout();
});

async function generateMeal(category) {
  const mealContainer = document.getElementById('meal-container');
  const categoryMeals = meals[category];

  if (categoryMeals.length === 0) {
    mealContainer.innerHTML = `<p>No meals available for ${category}.</p>`;
    return;
  }

  const randomIndex = Math.floor(Math.random() * categoryMeals.length);
  const generatedMeal = categoryMeals[randomIndex];

  mealContainer.innerHTML = `<p>${generatedMeal}</p>`;
  mealContainer.innerHTML += `<button class="generate-button" onclick="generateMeal('${category}')">Generate New ${category.charAt(0).toUpperCase() + category.slice(1)} Meal</button>`;

  document.getElementById('breakfastButton').disabled = false;
  document.getElementById('dinnerButton').disabled = false;
  document.getElementById('teaButton').disabled = false;
  document.getElementById('paydayButton').disabled = false;

  const clickedButton = document.getElementById(`${category}Button`);
  clickedButton.disabled = true;

  playSound('audioElement');

  clearTimeout(timeoutId);
  timeoutId = setTimeout(() => {
    mealContainer.innerHTML = '';
    clickedButton.disabled = false;
  }, 20000);
}

function playSound(elementId) {
  const audioElement = document.getElementById(elementId);
  audioElement.play();
}

function toggleMenu() {
  const menu = document.getElementById('menu');
  menu.classList.toggle('show');
  
  const burger = document.querySelector('.burger');
  burger.classList.toggle('active');
  
  // Adjust burger menu position based on menu visibility
  const offset = menu.classList.contains('show') ? 220 : 0;
  burger.style.left = `${20 + offset}px`; // Adjust burger position dynamically
}
