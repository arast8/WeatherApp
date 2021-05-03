package com.rasta.weatherapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

/**
 * This app uses the OpenWeather API (https://openweathermap.org/) to fetch current weather data.
 * It logs and displays saved past weather data.
 *
 * @author Andrew Rast
 */
class MainActivity : AppCompatActivity() {

    private lateinit var selectedTimeText: TextView
    private lateinit var conditionText: TextView
    private lateinit var tempText: TextView
    private lateinit var feelsLikeText: TextView
    private lateinit var humidityText: TextView
    private lateinit var cloudCoverText: TextView
    private lateinit var visibilityText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationText: TextView
    private lateinit var lastUpdatedText: TextView

    private lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // visual elements
        selectedTimeText = findViewById(R.id.txt_selected_time)
        conditionText = findViewById(R.id.txt_condition)
        tempText = findViewById(R.id.txt_temp)
        feelsLikeText = findViewById(R.id.txt_feels_like)
        humidityText = findViewById(R.id.txt_humidity)
        cloudCoverText = findViewById(R.id.txt_cloud_cover)
        visibilityText = findViewById(R.id.txt_visibility)
        recyclerView = findViewById(R.id.rv_weather_states)
        recyclerView.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, true
        )
        locationText = findViewById(R.id.txt_location)
        lastUpdatedText = findViewById(R.id.txt_last_updated)

        Settings.load(filesDir)

        currentLocation = Location.get(this)
        recyclerView.adapter = currentLocation.weatherStateAdapter

        refresh(null)
    }

    fun refresh(v: View?) {
        testForNewLocation()

        currentLocation.refresh()
    }

    /**
     * Tests if [currentLocation] is the same as the location in the global settings. If not set
     * [currentLocation] to the new [Location].
     */
    private fun testForNewLocation() {
        val jsonObject = settings.getJSONObject("location")

        if (jsonObject.getString("city") != currentLocation.city ||
                jsonObject.getString("state") != currentLocation.state ||
                jsonObject.getString("country") != currentLocation.country
        ) {
            currentLocation = Location.get(this)
            recyclerView.adapter = currentLocation.weatherStateAdapter

            Toast.makeText(this, "Changed location.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Update the screen with weather values or [R.string.unknown].
     */
    fun updateVisualElements() {
        val unknown = resources.getString(R.string.unknown)
        val currentWeather = currentLocation.weatherStates.takeUnless { it.isEmpty() }?.get(0)
        val selectedWeather = currentLocation.weatherStates.takeUnless { it.isEmpty() }
                ?.get(currentLocation.selected)

        locationText.text = currentLocation.toPrettyString()
        lastUpdatedText.text = resources.getString(
                R.string.last_updated,
            currentWeather?.getFormattedTime(" ") ?: unknown
        )
        selectedTimeText.text = resources.getString(
                R.string.selected_time,
                if (selectedWeather != currentWeather)
                    selectedWeather?.getFormattedTime(" ") ?: ""
                else ""
        )
        tempText.text = resources.getString(
                R.string.temp, selectedWeather?.getTemp() ?: unknown
        )
        conditionText.text = resources.getString(
                R.string.condition, selectedWeather?.condition ?: unknown
        )
        feelsLikeText.text = resources.getString(
                R.string.feels_like, selectedWeather?.getFeelsLike() ?: unknown
        )
        humidityText.text = resources.getString(
                R.string.humidity, selectedWeather?.getHumidity() ?: unknown
        )
        cloudCoverText.text = resources.getString(
                R.string.cloud_cover, selectedWeather?.getCloudCover() ?: unknown
        )
        visibilityText.text = resources.getString(
                R.string.visibility, selectedWeather?.getVisibility() ?: unknown
        )
        currentLocation.weatherStateAdapter.notifyDataSetChanged()
    }

    /**
     * starts SettingsActivity
     */
    fun startSettingsActivity(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("$packageName.settings", settings.toString())
        startActivityForResult(intent, 1)
    }

    /**
     * starts SettingsActivity
     */
    fun startInfoActivity(v: View) {
        val intent = Intent(this, InfoActivity::class.java)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // SettingsActivity result
        if (requestCode == 1) {
            data?.getStringExtra("$packageName.oldSettings")?.let {
                val oldSettings = JSONObject(it)

                if (settings.getString("api_key")
                        != oldSettings.getString("api_key") ||
                        settings.getJSONObject("location").toString()
                        != oldSettings.getJSONObject("location").toString()
                ) { // the changes to the settings require new data
                    refresh(null)
                } else {
                    updateVisualElements()
                }
            }

        }
    }

}