# WeatherApp

This is an Android app for logging weather data. It was my final project for Intro to Mobile App Development class in fall 2020.

Highlights:
- Written in Kotlin using Android Studio
- Gets current weather data from the [OpenWeatherMap API](https://openweathermap.org/api)
- Receives weather data in JSON format and stores the original JSON response
- Uses a Kotlin coroutine for network and file operations so that the main thread is not blocked
- Main, settings, and info screens are implemented as separate Activities
- Displays list of past weather in a RecyclerView
- Tapping elements in the RecyclerView causes that data to be displayed on the main controls
- The user can choose units of measurement, location, and how long to keep data
- Settings are also stored as JSON

The program requires that the user input their own API key on the settings screen.
