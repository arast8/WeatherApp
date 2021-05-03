package com.rasta.weatherapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.time.Duration

/**
 * Class for managing WeatherState I/O.
 *
 * @author Andrew Rast
 */
class WeatherFetcher {
    companion object {

        private const val apiSite = "https://api.openweathermap.org/data/2.5/weather"

        /**
         * Returns all the previously downloaded weather data for the location.
         */
        suspend fun getAllFromFile(filesDir: File, location: String): MutableList<WeatherState> {
            return withContext(Dispatchers.IO) {
                val files = File(filesDir, location).listFiles()
                val weatherStates: MutableList<WeatherState> = mutableListOf()

                if (files != null) {
                    for (file in files) {
                        val json = file.readText()

                        weatherStates.add(WeatherState(json))
                    }
                }

                weatherStates.sort()

                return@withContext weatherStates
            }
        }

        /**
         * Fetches the most recent weather data for a location from the weather API.
         */
        suspend fun getFromNet(location: String, apiKey: String): WeatherState {
            return withContext(Dispatchers.IO) {
                val url = URL("$apiSite?q=$location&appid=$apiKey")

                val json = url.readText()

                return@withContext WeatherState(json)
            }
        }

        /**
         * Saves a WeatherState's original json data to persistent storage. The file is saved to a
         * subdirectory named after the location, and the file is named after the time the data was
         * calculated.
         */
        suspend fun saveToFile(
            filesDir: File, location: String, weatherState: WeatherState
        ) {
            withContext(Dispatchers.IO) {
                val dir = File(filesDir, location)
                val file = File(dir.toString(), "${weatherState.time.toEpochSecond()}.json")

                if (!dir.exists()) dir.mkdir()

                file.writeText(weatherState.json)
            }
        }

        /**
         * For the provided location, deletes weather data older than the "delete_after_choice"
         * setting. Returns the number of files deleted.
         */
        suspend fun deleteOldFiles(
            filesDir: File,
            location: String,
            weatherStates: MutableList<WeatherState>
        ): Int {
            return withContext(Dispatchers.IO) {
                var numDeleted = 0

                val deleteAfterDuration: Duration? =
                    when (settings.getInt("delete_after_choice")) {
                        0 -> Duration.ofDays(1) // 1 day
                        1 -> Duration.ofDays(7) // 1 week
                        2 -> Duration.ofDays(31) // 1 month
                        3 -> Duration.ofDays(356) // 1 year
                        else -> null // never
                    }

                if (deleteAfterDuration != null) {
                    val locationDir = File(filesDir, location).takeIf { it.isDirectory }

                    if (locationDir != null) {
                        var index = 0

                        while (index < weatherStates.size) {
                            val weatherState = weatherStates[index]

                            if (weatherState.getTimeSinceCalculated() > deleteAfterDuration) {
                                val file = File(
                                    locationDir, "${weatherState.time.toEpochSecond()}.json"
                                )

                                if (file.isFile) {
                                    file.delete()

                                    weatherStates.remove(weatherState)

                                    numDeleted++
                                    index--
                                }
                            }

                            index++
                        }
                    }
                }

                return@withContext numDeleted
            }
        }

    }
}