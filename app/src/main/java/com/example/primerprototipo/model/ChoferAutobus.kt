package com.example.primerprototipo.model

data class AsignacionChofer(
    val choferId: String = "",
    val autobusId: String = "",
    val fechaAsignacion: Long = System.currentTimeMillis()
)

//Test
