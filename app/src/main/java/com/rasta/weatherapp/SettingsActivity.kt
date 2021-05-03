package com.rasta.weatherapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject

/**
 * The user can change the default settings in this Activity.
 *
 * @author Andrew Rast
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var unitsRadioGroup: RadioGroup
    private lateinit var celsiusButton: RadioButton
    private lateinit var fahrenheitButton: RadioButton
    private lateinit var kelvinButton: RadioButton
    private lateinit var cityInput: EditText
    private lateinit var stateInput: EditText
    private lateinit var countryInput: EditText
    private lateinit var deleteAfterSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = "Settings"

        // visual elements
        apiKeyInput = findViewById(R.id.input_api_key)
        unitsRadioGroup = findViewById(R.id.radio_units)
        celsiusButton = findViewById(R.id.button_celsius)
        fahrenheitButton = findViewById(R.id.button_fahrenheit)
        kelvinButton = findViewById(R.id.button_kelvin)
        cityInput = findViewById(R.id.input_city)
        stateInput = findViewById(R.id.input_state)
        countryInput = findViewById(R.id.input_country)
        deleteAfterSpinner = findViewById(R.id.sp_delete_after)
        deleteAfterSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.delete_after_choices,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val locationJSONObject = settings.getJSONObject("location")

        // load the settings into the visual elements
        apiKeyInput.setText(settings.getString("api_key"))
        unitsRadioGroup.check(
            when (settings.getString("units")) {
                "imperial" -> R.id.button_fahrenheit
                "standard" -> R.id.button_kelvin
                else -> R.id.button_celsius
            }
        )
        cityInput.setText(locationJSONObject.getString("city"))
        stateInput.setText(locationJSONObject.getString("state"))
        countryInput.setText(locationJSONObject.getString("country"))
        deleteAfterSpinner.setSelection(settings.getInt("delete_after_choice"))
    }

    /**
     * Get updated settings from the visual elements, save to file, and pass the old settings back
     * to MainActivity.
     */
    fun save(v: View) {
        val oldSettings: String = settings.toString()

        settings.put("api_key", apiKeyInput.text)
        settings.put(
            "units",
            when (unitsRadioGroup.checkedRadioButtonId) {
                R.id.button_celsius -> "metric"
                R.id.button_fahrenheit -> "imperial"
                R.id.button_kelvin -> "standard"
                else -> ""
            }
        )
        settings.put(
            "location",
            JSONObject()
                .put("city", cityInput.text)
                .put("state", stateInput.text)
                .put("country", countryInput.text)
        )
        settings.put("delete_after_choice", deleteAfterSpinner.selectedItemPosition)

        Settings.save(filesDir)

        val data = Intent(this, MainActivity::class.java)
        data.putExtra("$packageName.oldSettings", oldSettings)
        setResult(RESULT_OK, data)

        finish()
    }

}