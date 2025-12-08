package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primerprototipo.model.Autobus
import com.example.primerprototipo.model.Chofer
import com.example.primerprototipo.model.HorariosRuta
import com.example.primerprototipo.model.Parada
import com.example.primerprototipo.model.RutasMisantla
import com.example.primerprototipo.model.Terminal
import com.example.primerprototipo.repository.ChoferRepository
import com.example.primerprototipo.repository.DirectionsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class ChoferViewModel : ViewModel() {

    private val _chofer = MutableLiveData<Chofer?>()
    val chofer: LiveData<Chofer?> = _chofer

    private val _estadoCarga = MutableLiveData<EstadoCarga>()
    val estadoCarga: LiveData<EstadoCarga> = _estadoCarga

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

    // Funciones para cargar datos
    fun cargarDatosChofer(choferId: String) {
        _estadoCarga.value = EstadoCarga.CARGANDO
        viewModelScope.launch {
            val resultado = ChoferRepository.getChofer(choferId)
            resultado.onSuccess { choferData ->
                if (choferData != null) {
                    _chofer.postValue(choferData)
                    _estadoCarga.postValue(EstadoCarga.EXITO)
                    // Una vez cargado, inicializamos el panel con su nombre
                    inicializarAutobus(choferData.nombre)
                } else {
                    _estadoCarga.postValue(EstadoCarga.ERROR("No se encontraron datos para el chofer."))
                }
            }.onFailure { error ->
                _estadoCarga.postValue(EstadoCarga.ERROR(error.message ?: "Error desconocido"))
            }
        }
    }

    fun inicializarAutobus(nombreChofer: String) {
        _mensaje.value = "Bienvenido, $nombreChofer. Selecciona tu terminal de salida"
        _nombreRuta.value = "Ruta no configurada"
        _proximaParada.value = "Selecciona terminal"
        _pasajerosAbordo.value = 0
        _capacidadDisponible.value = "${Autobus.CAPACIDAD_MAXIMA} asientos disponibles"
        _tiempoSalida.value = "Pendiente"
    }

    // Funciones para configurar la ruta
    fun configurarTerminalSalida(terminal: Terminal) {
        _terminalSeleccionada.value = terminal
        _mensaje.value = "Calculando ruta..."

        viewModelScope.launch {
           // RutaRepository.getRuta(terminal)
            val origen = RutasMisantla.obtenerTerminalOrigen(terminal)
            val destino = RutasMisantla.obtenerTerminalDestino(terminal)

            val result = DirectionsRepository.getDirections(origen, destino)

            result.onSuccess { ruta -> // ruta es de tipo model.Ruta
                paradasRuta = ruta.paradas
                paradaActualIndex = 0

                if (paradasRuta.isNotEmpty()) {
                    _rutaDibujable.postValue(ruta.polyline)

                    val nombreRuta = RutasMisantla.obtenerNombreRuta(terminal)

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

                    _autobus.postValue(nuevoAutobus)
                    _nombreRuta.postValue(nombreRuta)
                    // CAMBIO: Se muestra el destino con el nombre de la ciudad, no la dirección completa.
                    _terminalDestino.postValue("Destino: ${RutasMisantla.obtenerTerminalDestino(terminal).split(",")[0]}")
                    actualizarDatos() // Se llama aquí para asegurar que la UI se actualice
                    _mensaje.postValue("Ruta configurada. Listo para iniciar.")

                } else {
                    _mensaje.postValue("Error: No se encontraron paradas en la ruta calculada.")
                }

            }.onFailure { error ->
                _mensaje.postValue("Error al trazar la ruta: ${error.message}")
            }
        }
    }

    // Funciones para controlar el autobús
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

    // Funciones para controlar el autobús
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

    // Clases y objetos internos
    sealed class AccionServicio {
        object INICIAR : AccionServicio()
        object DETENER : AccionServicio()
    }

    sealed class EstadoCarga {
        object CARGANDO : EstadoCarga()
        object EXITO : EstadoCarga()
        data class ERROR(val mensaje: String) : EstadoCarga()
    }
}
