package com.rasta.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

/**
 * This Activity shows information about the app.
 *
 * @author Andrew Rast
 */
class InfoActivity : AppCompatActivity() {

    private lateinit var infoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        infoText = findViewById(R.id.txt_info)

        infoText.text = getString(R.string.txt_info, getString(R.string.version))
    }

    fun finish(v: View) {
        finish()
    }
}