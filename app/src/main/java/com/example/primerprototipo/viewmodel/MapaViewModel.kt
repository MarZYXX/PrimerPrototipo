package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primerprototipo.model.Parada
import com.example.primerprototipo.model.RutasMisantla
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.model.UbicacionAutobus
import com.example.primerprototipo.repository.DirectionsRepository
import com.example.primerprototipo.repository.FirebaseManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapaViewModel : ViewModel() {

    private val _rutaSeleccionada = MutableLiveData<Terminal>()

    private val _rutaDibujable = MutableLiveData<List<LatLng>?>()
    val rutaDibujable: LiveData<List<LatLng>?> = _rutaDibujable

    private val _paradasDeLaRuta = MutableLiveData<List<Parada>>()
    val paradasDeLaRuta: LiveData<List<Parada>> = _paradasDeLaRuta

    val autobusesEnRuta: LiveData<List<UbicacionAutobus>> = FirebaseManager.autobusesEnRuta

    private val _mensajeError = MutableLiveData<String>()
    val mensajeError: LiveData<String> = _mensajeError

    fun seleccionarRuta(terminal: Terminal) {
        if (_rutaSeleccionada.value == terminal) return

        _rutaSeleccionada.value = terminal
        FirebaseManager.escucharAutobusesPorRuta(RutasMisantla.obtenerNombreRuta(terminal))

        viewModelScope.launch {
            // Obtiene el origen y destino de la ruta
            val origen = RutasMisantla.obtenerTerminalOrigen(terminal)
            val destino = RutasMisantla.obtenerTerminalDestino(terminal)

            // Llama al repositorio con los nombres de las ciudades.
            val result = DirectionsRepository.getDirections(origen, destino)

            result.onSuccess {
                // Al tener éxito, actualiza tanto la polilínea de la ruta como la lista de paradas.
                _rutaDibujable.postValue(it.polyline)
                _paradasDeLaRuta.postValue(it.paradas)
            }.onFailure {
                _mensajeError.postValue("No se pudo obtener la ruta: ${it.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseManager.dejarDeEscucharAutobuses()
    }
}