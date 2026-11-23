package com.example.coders.ui.screens

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object AddressGeocoder {

    suspend fun findCoordinates(
        context: Context,
        address: String
    ): Pair<Double, Double>? = withContext(Dispatchers.IO) {

        if (address.isBlank()) {
            return@withContext null
        }

        val geocoder = Geocoder(context, Locale.getDefault())

        @Suppress("DEPRECATION")
        val results = geocoder.getFromLocationName(
            address.trim(),
            1
        )

        val result = results?.firstOrNull()

        if (result == null) {
            null
        } else {
            result.latitude to result.longitude
        }
    }
}