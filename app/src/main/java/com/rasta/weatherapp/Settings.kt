package com.rasta.weatherapp

import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.format.DateTimeFormatter

/** The global settings variable. */
var settings = Settings.getDefault()

/**
 * Class for managing settings I/O.
 *
 * @author Andrew Rast
 */
class Settings {
    companion object {

        private const val FILE_NAME = "settings.json"
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val MIN_RECALCULATE_RATE: Duration = Duration.ofMinutes(15)
        val MIN_CALL_RATE: Duration = Duration.ofMinutes(1)

        /**
         * Returns a JSONObject containing the default values of the changeable settings.
         */
        fun getDefault(): JSONObject {
            val defaultSettings = JSONObject()

            defaultSettings.put("api_key", "")
            defaultSettings.put("units", "metric")
            defaultSettings.put("location",
                JSONObject()
                    .put("city", "London")
                    .put("state", "")
                    .put("country", "UK")
            )
            defaultSettings.put("delete_after_choice", 2)

            return defaultSettings
        }

        /**
         * Read the settings file, if it exists. Otherwise get the default settings.
         */
        fun load(filesDir: File) {
            val file = File(filesDir, FILE_NAME)

            settings = if (file.exists()) {
                try {
                    val json = file.readText()

                    JSONObject(json)
                } catch (e: IOException) {
                    getDefault()
                }
            } else {
                getDefault()
            }
        }

        /**
         * Save the settings to the settings file.
         */
        fun save(filesDir: File) {
            val file = File(filesDir, FILE_NAME)

            file.writeText(settings.toString())
        }

    }
}