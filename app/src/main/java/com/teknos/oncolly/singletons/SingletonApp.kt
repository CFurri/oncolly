package com.teknos.oncolly.singletons

import com.teknos.oncolly.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class SingletonApp private constructor() {
    // --- AQUI GUARDEM LES NOSTRES 'COSES' GLOBALS ---

    // La connexió a Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Recorda: IP Emulador
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // Per cridar el servidor
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Les dades de sessió (Usuari actual)
    var userId: Int? = null
    var userRole: String? = null // "DOCTOR" o "PACIENT"
    var userToken: String? = null

    // Funcions per gestionar la sessió
    fun ferLogin(id: Int, role: String, token: String) {
        this.userId = id
        this.userRole = role
        this.userToken = token
    }

    fun tancarSessio() {
        this.userId = null
        this.userRole = null
        this.userToken = null
    }

    companion object {
        @Volatile
        private var instance: SingletonApp? = null

        fun getInstance(): SingletonApp {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SingletonApp()
                    }
                }
            }
            return instance!!
        }
    }
}
