package com.rasta.weatherapp

import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Represents a location and contains data about the weather at that location. It calls
 * [WeatherFetcher] functions for getting weather data.
 *
 * @author Andrew Rast
 */
class Location(
    val city: String,
    /** can be empty, but not null */
    val state: String,
    val country: String,
    private val mainActivity: MainActivity
) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private var lastCall: Instant? = null
    private var refreshLock = false
    val weatherStateAdapter = WeatherStateAdapter(this, mainActivity)
    /** contains the weather data for this location **/
    var weatherStates: MutableList<WeatherState> = mutableListOf()
    /** the index of the currently selected WeatherState */
    var selected: Int = 0
        set(value) {
            field = value
            mainActivity.updateVisualElements()
        }

    /**
     * Handles the logic of when to call the weather API and shows appropriate notices to the user
     * if it's not going to.
     */
    fun refresh() {
        coroutineScope.launch {
            if (!refreshLock) {
                refreshLock = true

                if (weatherStates.isEmpty()) {
                    initialize()
                }

                val apiKey = settings.getString("api_key")

                if (!apiKey.isNullOrBlank()) {
                    val timeUntilNextCall = getTimeUntilNextCall()

                    if (timeUntilNextCall <= Duration.ZERO) {

                        callTheAPI(apiKey)

                    } else {
                        val minutes: Long = timeUntilNextCall.toMinutes() + 1
                        val seconds: Long = timeUntilNextCall.seconds

                        Toast.makeText(
                            mainActivity,
                            "Please wait "
                                    + if (minutes > 0) {
                                "$minutes minute" + if (minutes != 1L) "s" else ""
                            } else {
                                "$seconds second" + if (seconds != 1L) "s" else ""
                            } + ".",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(mainActivity, "No API key.", Toast.LENGTH_SHORT).show()
                }

                refreshLock = false
            } else {
                Toast.makeText(
                    mainActivity, "Update already in progress.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Gets the most recent weather data that is stored locally, and deletes old data. Updates the
     * screen after each operation.
     */
    private suspend fun initialize() {
        try {

            weatherStates = WeatherFetcher.getAllFromFile(mainActivity.filesDir, toString())

            mainActivity.updateVisualElements()

            val numDeleted = WeatherFetcher.deleteOldFiles(
                mainActivity.filesDir, toString(), weatherStates
            )

            if (numDeleted > 0) {
                Toast.makeText(
                    mainActivity, "Deleted $numDeleted old weather states.", Toast.LENGTH_SHORT
                ).show()

                mainActivity.updateVisualElements()
            }

        } catch (e: IOException) {
            Toast.makeText(mainActivity, e.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            Toast.makeText(mainActivity, e.message, Toast.LENGTH_LONG).show()
        } catch (e: JSONException) {
            Toast.makeText(mainActivity, e.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            Toast.makeText(mainActivity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Tries to get new weather data from the weather API, and shows appropriate messages to the
     * user if errors happen or if there is no new data. If there is new data, update the screen
     * with it and save it.
     */
    private suspend fun callTheAPI(apiKey: String) {
        try {

            val newWeather = WeatherFetcher.getFromNet(toString(), apiKey)

            lastCall = Clock.systemUTC().instant()

            if (weatherStates.isEmpty() || newWeather != weatherStates[0]) {
                weatherStates.add(0, newWeather)

                mainActivity.updateVisualElements()

                WeatherFetcher.saveToFile(mainActivity.filesDir, toString(), newWeather)

                Toast.makeText(mainActivity, "Weather updated.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mainActivity, "New weather data is not available yet.",
                    Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException) {
            Toast.makeText(mainActivity, e.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            Toast.makeText(mainActivity, e.message, Toast.LENGTH_LONG).show()
        } catch (e: JSONException) {
            Toast.makeText(mainActivity, e.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            Toast.makeText(mainActivity, e.message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Returns the duration since the weather API was last called, or null if it has not been called
     * yet.
     */
    private fun getTimeSinceLastCall(): Duration? =
        lastCall?.epochSecond?.let {
            Duration.ofSeconds(Clock.systemUTC().instant().epochSecond - it)
        }

    /**
     * Returns the duration until calling the weather API again will be allowed. This is to try to
     * avoid calling the API too frequently.
     */
    private fun getTimeUntilNextCall(): Duration {
        return if (weatherStates.isNotEmpty()) {
            val timeUntilRecalculated = weatherStates[0].getTimeUntilRecalculated()
            val timeSinceLastCall = getTimeSinceLastCall()

            if (timeUntilRecalculated > Duration.ZERO) {
                timeUntilRecalculated
            } else if (timeSinceLastCall != null && timeSinceLastCall > Duration.ZERO) {
                Settings.MIN_CALL_RATE - timeSinceLastCall
            } else { // enough time has passed
                Duration.ZERO
            }
        } else { // no weather data
            Duration.ZERO
        }
    }

    /**
     * Returns the city, state (if not blank), and country, separated by a comma.
     */
    override fun toString() =
        if (state.isBlank())
            "$city,$country"
        else
            "$city,$state,$country"

    /**
     * Returns the city, state (if not blank), and country, separated by a comma and a space.
     */
    fun toPrettyString() =
        if (state.isBlank())
            "$city, $country"
        else
            "$city, $state, $country"

    companion object {
        /**
         * Returns the location specified in the global settings.
         */
        fun get(context: MainActivity): Location {
            val jsonObject = settings.getJSONObject("location")

            return Location(
                jsonObject.getString("city"),
                jsonObject.getString("state"),
                jsonObject.getString("country"),
                context
            )
        }
    }

}