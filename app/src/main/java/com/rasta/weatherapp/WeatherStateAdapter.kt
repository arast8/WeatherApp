package com.rasta.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * This is the adapter that connects a Location's list of WeatherStates to the RecyclerView in
 * MainActivity.
 *
 * @author Andrew Rast
 */
class WeatherStateAdapter(private val location: Location, private val mainActivity: MainActivity) :
    RecyclerView.Adapter<WeatherStateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tempText: TextView = view.findViewById(R.id.recycle_txt_temp)
        val timeText: TextView = view.findViewById(R.id.recycle_txt_time)
        val layout: ConstraintLayout = view.findViewById(R.id.layout_weather_state)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_weather_state, parent, false)

        return ViewHolder(view)
    }

    /**
     * Gets data from the proper WeatherState, but also sets the click listener that will make the
     * data show "on the big screen".
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weatherState = location.weatherStates[position]

        holder.tempText.text = weatherState.getTemp() ?: mainActivity.getString(R.string.unknown)
        holder.timeText.text = weatherState.getFormattedTime("\n")

        holder.layout.setOnClickListener { location.selected = position }
    }

    override fun getItemCount() = location.weatherStates.size

}