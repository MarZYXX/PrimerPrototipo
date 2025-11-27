package com.example.primerprototipo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.primerprototipo.model.Autobus
import com.example.primerprototipo.model.Parada
import com.example.primerprototipo.model.RutasMisantla
import com.example.primerprototipo.model.Terminal

class ChoferViewModel : ViewModel() {

    // ============================================
    // LiveData Observables
    // ============================================
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

    // ============================================
    // Variables Internas
    // ============================================
    private var paradaActualIndex = 0
    private var paradasRuta: List<Parada> = emptyList()
    private var terminalActual: Terminal = Terminal.MISANTLA

    // ============================================
    // Inicialización del Autobús
    // ============================================
    fun inicializarAutobus(nombreChofer: String) {
        // Por defecto inicia sin terminal seleccionada
        _mensaje.value = "Bienvenido, $nombreChofer. Selecciona tu terminal de salida"
        _nombreRuta.value = "Ruta no configurada"
        _proximaParada.value = "Selecciona terminal"
        _pasajerosAbordo.value = 0
        _capacidadDisponible.value = "${Autobus.CAPACIDAD_MAXIMA} asientos disponibles"
        _tiempoSalida.value = "Pendiente"
    }

    // ============================================
    // Configuración de Terminal de Salida
    // ============================================
    fun configurarTerminalSalida(terminal: Terminal) {
        terminalActual = terminal
        _terminalSeleccionada.value = terminal

        // Obtener paradas según la terminal seleccionada
        paradasRuta = RutasMisantla.obtenerParadasPorTerminal(terminal)
        paradaActualIndex = 0

        // Crear el autobús con la ruta configurada
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
        _mensaje.value = "Ruta configurada: ${terminal.nombreCompleto} → $terminalDestino"
    }

    // ============================================
    // Gestión de Pasajeros
    // ============================================
    fun agregarPasajero() {
        val bus = _autobus.value ?: return

        if (bus.agregarPasajero()) {
            _autobus.value = bus // Trigger update
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

    // ============================================
    // Gestión de Paradas
    // ============================================
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



    fun iniciarTransmisionGPS(context: Context) {
        val autobus = _autobus.value ?: return
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            putExtra("autobus_id", autobus.id)
            putExtra("ruta", autobus.ruta)
            putExtra("chofer_id", usuarioActual?.correo ?: "unknown")
        }
        context.startService(intent)
    }

    fun detenerTransmisionGPS(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java)
        context.stopService(intent)
    }

    // ============================================
    // Gestión de Tiempo
    // ============================================
    fun establecerTiempoSalida(tiempo: String, context: Context) {
        if (tiempo.isEmpty()) {
            _mensaje.value = "Ingrese un tiempo de salida válido"
            return
        }

        val bus = _autobus.value ?: return
        bus.tiempoSalida = tiempo
        bus.enRuta = true

        _autobus.value = bus
        actualizarDatos()
        _mensaje.value = "Salida programada: $tiempo - Iniciando transmisión GPS"

        // Iniciar GPS cuando se establece el horario
        iniciarTransmisionGPS(context)
    }

    fun obtenerHorariosDisponibles(): List<String> {
        // Horarios de ejemplo
        return listOf(
            "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM",
            "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM",
            "04:00 PM", "05:00 PM", "06:00 PM"
        )
    }

    // ============================================
    // Cambiar Ruta
    // ============================================
    fun cambiarRuta(nuevaRuta: String) {
        val bus = _autobus.value ?: return

        bus.ruta = nuevaRuta
        paradasRuta = RutasMisantla.obtenerParadasPorRuta(nuevaRuta)
        paradaActualIndex = 0

        val primeraParada = paradasRuta.firstOrNull()
        if (primeraParada != null) {
            bus.proximaParada = primeraParada.nombre
            bus.latitud = primeraParada.latitud
            bus.longitud = primeraParada.longitud
        }

        _autobus.value = bus
        actualizarDatos()
        _mensaje.value = "Ruta cambiada a: $nuevaRuta"
    }

    // ============================================
    // Obtener Terminales Disponibles
    // ============================================
    fun obtenerTerminalesDisponibles(): Array<Terminal> {
        return Terminal.values()
    }

    // ============================================
    // Verificar si la ruta está configurada
    // ============================================
    fun esRutaConfigurada(): Boolean {
        return paradasRuta.isNotEmpty()
    }

    // ============================================
    // Actualización de UI
    // ============================================
    private fun actualizarDatos() {
        val bus = _autobus.value ?: return

        _pasajerosAbordo.value = bus.pasajerosAbordo
        _proximaParada.value = bus.proximaParada
        _tiempoSalida.value = if (bus.tiempoSalida.isEmpty()) "Pendiente" else bus.tiempoSalida

        val disponible = bus.obtenerCapacidadDisponible()
        _capacidadDisponible.value = "$disponible asientos disponibles"
    }

    // ============================================
    // Información del Estado
    // ============================================
    fun obtenerInfoCompleta(): String {
        val bus = _autobus.value ?: return "No hay datos del autobús"

        return """
            Unidad: ${bus.numeroUnidad}
            Ruta: ${bus.ruta}
            Pasajeros: ${bus.pasajerosAbordo}/${Autobus.CAPACIDAD_MAXIMA}
            Próxima parada: ${bus.proximaParada}
            Salida: ${bus.tiempoSalida}
            Estado: ${if (bus.enRuta) "En ruta" else "Detenido"}
        """.trimIndent()
    }
}