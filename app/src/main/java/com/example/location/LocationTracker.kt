package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float?,
    ) : LocationResult

    data object PermissionDenied : LocationResult

    data class Unavailable(val reason: String) : LocationResult
}

class LocationTracker(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.PermissionDenied
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null && !LocationManagerCompat.isLocationEnabled(locationManager)) {
            return LocationResult.Unavailable("휴대폰의 위치(GPS) 서비스가 꺼져 있습니다. 상단바 또는 설정에서 위치를 켜주세요.")
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        val priority = if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val current = withTimeoutOrNull(8_000L) {
            awaitCurrentLocation(fusedClient, priority)
        }
        if (current != null) {
            return current.toResult()
        }

        val lastFused = withTimeoutOrNull(2_000L) {
            awaitLastLocation(fusedClient)
        }
        if (lastFused != null) {
            return lastFused.toResult()
        }

        val fallbackManagerLocation = getBestLastKnownFromManager(locationManager)
        if (fallbackManagerLocation != null) {
            return fallbackManagerLocation.toResult()
        }

        return LocationResult.Unavailable(
            "GPS 위성 신호를 수신할 수 없습니다. 지하 주차장이라면 층수·구역 메모와 기둥 사진만으로도 저장할 수 있습니다.",
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitCurrentLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient,
        priority: Int,
    ): Location? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }
        try {
            client.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitLastLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient,
    ): Location? = suspendCancellableCoroutine { cont ->
        try {
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownFromManager(locationManager: LocationManager?): Location? {
        if (locationManager == null) return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        return providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
    }

    private fun Location.toResult(): LocationResult.Success = LocationResult.Success(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else null,
    )
}
