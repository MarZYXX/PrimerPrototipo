package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primerprototipo.model.Autobus
import com.example.primerprototipo.model.HorariosRuta
import com.example.primerprototipo.model.Parada
import com.example.primerprototipo.model.RutasMisantla
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.repository.DirectionsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class ChoferViewModel : ViewModel() {

    private val _accionServicio = MutableLiveData<AccionServicio>()
    val accionServicio: LiveData<AccionServicio> = _accionServicio

    private val _rutaDibujable = MutableLiveData<List<LatLng>?>()
    val rutaDibujable: LiveData<List<LatLng>?> = _rutaDibujable

    private val _autobus = MutableLiveData<Autobus>()
    val autobus: LiveData<Autobus> = _autobus

    private val _pasajerosAbordo = MutableLiveData<Int>()
    val pasajerosAbordo: LiveData<Int> = _pasajerosAbordo

    private val _proximaParada = MutableLiveData<String>()
    val proximaParada: LiveData<String> = _proximaParada

    private val _tiempoSalida = MutableLiveData<String>()
    val tiempoSalida: LiveData<String> = _tiempoSalida

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _capacidadDisponible = MutableLiveData<String>()
    val capacidadDisponible: LiveData<String> = _capacidadDisponible

    private val _nombreRuta = MutableLiveData<String>()
    val nombreRuta: LiveData<String> = _nombreRuta

    private val _terminalDestino = MutableLiveData<String>()
    val terminalDestino: LiveData<String> = _terminalDestino

    private val _terminalSeleccionada = MutableLiveData<Terminal>()
    val terminalSeleccionada: LiveData<Terminal> = _terminalSeleccionada

    private var paradaActualIndex = 0
    private var paradasRuta: List<Parada> = emptyList()

    fun inicializarAutobus(nombreChofer: String) {
        _mensaje.value = "Bienvenido, $nombreChofer. Selecciona tu terminal de salida"
        _nombreRuta.value = "Ruta no configurada"
        _proximaParada.value = "Selecciona terminal"
        _pasajerosAbordo.value = 0
        _capacidadDisponible.value = "${Autobus.CAPACIDAD_MAXIMA} asientos disponibles"
        _tiempoSalida.value = "Pendiente"
    }

    fun configurarTerminalSalida(terminal: Terminal) {
        _terminalSeleccionada.value = terminal
        paradasRuta = RutasMisantla.obtenerParadasPorTerminal(terminal)
        paradaActualIndex = 0

        viewModelScope.launch {
            val result = DirectionsRepository.getDirections(paradasRuta)
            result.onSuccess { ruta ->
                _rutaDibujable.postValue(ruta)
            }.onFailure { error ->
                _mensaje.postValue("Error al trazar la ruta: ${error.message}")
            }
        }

        val nombreRuta = RutasMisantla.obtenerNombreRuta(terminal)
        val terminalDestino = RutasMisantla.obtenerTerminalDestino(terminal)

        val nuevoAutobus = Autobus(
            id = "BUS-${System.currentTimeMillis()}",
            numeroUnidad = "007",
            ruta = nombreRuta,
            pasajerosAbordo = 0,
            proximaParada = paradasRuta.first().nombre,
            tiempoSalida = "Pendiente",
            latitud = paradasRuta.first().latitud,
            longitud = paradasRuta.first().longitud,
            enRuta = false
        )

        _autobus.value = nuevoAutobus
        _nombreRuta.value = nombreRuta
        _terminalDestino.value = "Destino: $terminalDestino"
        actualizarDatos()
    }

    fun agregarPasajero() {
        val bus = _autobus.value ?: return
        if (bus.agregarPasajero()) {
            _autobus.value = bus
            actualizarDatos()
            _mensaje.value = "Pasajero agregado. Total: ${bus.pasajerosAbordo}"
        } else {
            _mensaje.value = "Autobús lleno. Capacidad máxima: ${Autobus.CAPACIDAD_MAXIMA}"
        }
    }

    fun quitarPasajero() {
        val bus = _autobus.value ?: return
        if (bus.quitarPasajero()) {
            _autobus.value = bus
            actualizarDatos()
            _mensaje.value = "Pasajero bajó. Total: ${bus.pasajerosAbordo}"
        } else {
            _mensaje.value = "No hay pasajeros a bordo"
        }
    }

    fun avanzarSiguienteParada() {
        val bus = _autobus.value ?: return
        if (paradaActualIndex < paradasRuta.size - 1) {
            paradaActualIndex++
            val siguienteParada = paradasRuta[paradaActualIndex]
            bus.proximaParada = siguienteParada.nombre
            bus.latitud = siguienteParada.latitud
            bus.longitud = siguienteParada.longitud
            _autobus.value = bus
            actualizarDatos()
            _mensaje.value = "Avanzando a: ${siguienteParada.nombre}"
        } else {
            _mensaje.value = "Has llegado a la última parada"
        }
    }

    fun retrocederParada() {
        if (paradaActualIndex > 0) {
            paradaActualIndex--
            val bus = _autobus.value ?: return
            val paradaAnterior = paradasRuta[paradaActualIndex]
            bus.proximaParada = paradaAnterior.nombre
            bus.latitud = paradaAnterior.latitud
            bus.longitud = paradaAnterior.longitud
            _autobus.value = bus
            actualizarDatos()
            _mensaje.value = "Retrocediendo a: ${paradaAnterior.nombre}"
        } else {
            _mensaje.value = "Ya estás en la primera parada"
        }
    }

    fun establecerTiempoSalida(tiempo: String) {
        if (tiempo.isEmpty()) {
            _mensaje.value = "Ingrese un tiempo de salida válido"
            return
        }
        val bus = _autobus.value ?: return
        bus.tiempoSalida = tiempo
        bus.enRuta = true
        _autobus.value = bus
        actualizarDatos()
        _mensaje.value = "Salida programada: $tiempo"
        _accionServicio.value = AccionServicio.INICIAR
    }

    fun finalizarRuta() {
        val bus = _autobus.value ?: return
        bus.enRuta = false
        _autobus.value = bus
        actualizarDatos()
        _accionServicio.value = AccionServicio.DETENER
        _mensaje.value = "Ruta finalizada."
    }

    fun obtenerHorariosDisponibles(): List<String> {
        return HorariosRuta.generarHorarios()
    }

    private fun actualizarDatos() {
        val bus = _autobus.value ?: return
        _pasajerosAbordo.value = bus.pasajerosAbordo
        _proximaParada.value = bus.proximaParada
        _tiempoSalida.value = if (bus.tiempoSalida.isEmpty()) "Pendiente" else bus.tiempoSalida
        val disponible = bus.obtenerCapacidadDisponible()
        _capacidadDisponible.value = "$disponible asientos disponibles"
    }

    sealed class AccionServicio {
        object INICIAR : AccionServicio()
        object DETENER : AccionServicio()
    }
}
