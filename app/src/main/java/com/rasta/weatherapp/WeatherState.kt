package com.rasta.weatherapp

import org.json.JSONObject
import java.time.*
import kotlin.math.roundToInt

/**
 * Represents the weather at a certain location, and can be queried for various attributes.
 *
 * @author Andrew Rast
 */
class WeatherState(val json: String): Comparable<WeatherState> {

    val time: ZonedDateTime
    val condition: String?
    val temp: Double?
    val feelsLike: Double?
    val humidity: Int?
    val cloudCover: Int?
    val visibility: Int?

    init {
        // the data that the API response should have
        val jsonObject = JSONObject(json)

        val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
        val main = jsonObject.getJSONObject("main")

        val dt = jsonObject.getLong("dt")
        val timezone:ZoneOffset = ZoneOffset.ofTotalSeconds(jsonObject.getInt("timezone"))
        val instant = Instant.ofEpochSecond(dt)
        time = ZonedDateTime.ofInstant(instant, timezone)

        // the data that the API response may or may not have
        condition = weather.optString("description").takeUnless { it.isBlank() }
        temp = main.optDouble("temp").takeUnless { it.isNaN() }
        feelsLike = main.optDouble("feels_like").takeUnless { it.isNaN() }
        humidity = main.optInt("humidity").takeUnless { it == 0 }

        val cloudsObject = jsonObject.optJSONObject("clouds")
        cloudCover = cloudsObject?.optInt("all")?.takeUnless { it == 0 }

        visibility = jsonObject.optInt("visibility").takeUnless { it == 0 }
    }

    /**
     * Returns the duration since the data in this WeatherState was measured or calculated (not
     * since it was received).
     */
    fun getTimeSinceCalculated(): Duration =
        Duration.ofSeconds(Clock.systemUTC().instant().epochSecond - time.toEpochSecond())

    /**
     * Returns the duration until new weather data may be available. It will be negative if
     * [Settings.MIN_RECALCULATE_RATE] has passed.
     */
    fun getTimeUntilRecalculated(): Duration =
        Settings.MIN_RECALCULATE_RATE - getTimeSinceCalculated()

    /**
     * Returns this WeatherState's [time], only including the date if more than one day has passed.
     */
    fun getFormattedTime(delimiter: String): String =
        Settings.TIME_FORMATTER.format(time) +
                if (getTimeSinceCalculated().toDays() >= 1)
                    delimiter + Settings.DATE_FORMATTER.format(time)
                else
                    ""

    /**
     * Converts a temperature from Kelvin to the unit in the settings, rounds it, and appends the
     * unit's symbol.
     */
    private fun Double.asTemperature(): String =
        when (settings.getString("units")) {
            "standard" -> "${this.roundToInt()} K"
            "metric" -> "${(this - 273.15).roundToInt()} °C"
            else -> "${(this * 1.8 - 459.67).roundToInt()} °F"
        }

    private fun Int.asDistance(): String =
        when (settings.getString("units")) {
            "imperial" -> {
                val feet = this * 3.28084

                if (feet > 5280.0)
                    "${(feet / 5280.0).roundToInt()} mi"
                else
                    "${feet.roundToInt()} ft"
            }
            else -> {
                val meters = this

                if (meters > 1000)
                    "${(meters / 1000.0).roundToInt()} km"
                else
                    "$meters m"
            }
        }

    fun getTemp(): String? =
        temp?.asTemperature()

    fun getFeelsLike(): String? =
        feelsLike?.asTemperature()

    fun getHumidity(): String? =
        humidity?.let { "$it%" }

    fun getCloudCover(): String? =
        cloudCover?.let { "$it%" }

    fun getVisibility(): String? =
        visibility?.asDistance()

    override fun compareTo(other: WeatherState) =
        -time.compareTo(other.time)

    override fun equals(other: Any?) =
        other is WeatherState && json == other.json

    override fun hashCode() =
        json.hashCode()

}