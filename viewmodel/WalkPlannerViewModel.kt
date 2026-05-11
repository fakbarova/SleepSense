package com.circadianx.sleepsense.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.network.LatLngDto
import com.circadianx.sleepsense.data.network.RouteRepository
import com.circadianx.sleepsense.data.network.RouteSuggestion
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class WalkPlannerUiState(
    val origin: LatLngDto? = null,
    val destination: LatLngDto? = null,
    val isLoading: Boolean = false,
    val suggestion: RouteSuggestion? = null,
    val error: String? = null
)

@HiltViewModel
class WalkPlannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(WalkPlannerUiState())
    val state: StateFlow<WalkPlannerUiState> = _state.asStateFlow()

    fun loadOriginFromLocation() {
        viewModelScope.launch {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) {
                _state.update { it.copy(error = "Location permission is required to plan a walk.") }
                return@launch
            }

            runCatching {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val loc = client.lastLocation.await()
                if (loc == null) error("Unable to read location. Turn on GPS and try again.")
                LatLngDto(lat = loc.latitude, lng = loc.longitude)
            }.onSuccess { origin ->
                _state.update { it.copy(origin = origin, error = null) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Unable to load location") }
            }
        }
    }

    fun setDestination(lat: Double, lng: Double) {
        _state.update { it.copy(destination = LatLngDto(lat, lng), suggestion = null, error = null) }
    }

    fun planWalk(stepsRemaining: Int) {
        val origin = _state.value.origin ?: return
        val destination = _state.value.destination ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = routeRepository.suggestRoute(origin, destination, stepsRemaining)
            result.fold(
                onSuccess = { suggestion -> _state.update { it.copy(isLoading = false, suggestion = suggestion) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Route planning failed") } }
            )
        }
    }
}

