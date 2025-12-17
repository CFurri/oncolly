package com.teknos.oncolly.singletons

import com.teknos.oncolly.entity.Activity
import com.teknos.oncolly.entity.CreateActivityRequest
import com.teknos.oncolly.network.ApiService

/**
 * Singleton repository to centralize activity calls and keep a small in-memory cache.
 */
object ActivitySingleton {
    private val api: ApiService = SingletonApp.getInstance().api
    private val cache: MutableList<Activity> = mutableListOf()

    private fun bearer(): String =
        SingletonApp.getInstance().userToken?.let { "Bearer $it" }
            ?: error("Missing auth token")

    suspend fun fetchMyActivities(): Result<List<Activity>> = runCatching {
        val response = api.getMyActivities(bearer())
        if (response.isSuccessful) {
            val body = response.body().orEmpty()
            cache.clear()
            cache.addAll(body)
            body
        } else {
            error("${response.code()}")
        }
    }

    fun cached(): List<Activity> = cache.toList()

    suspend fun createActivity(request: CreateActivityRequest): Result<Unit> = runCatching {
        val response = api.createActivity(bearer(), request)
        if (response.isSuccessful) {
            Unit
        } else {
            val errorBody = response.errorBody()?.string()
            error(errorBody?.ifBlank { "HTTP ${response.code()}" } ?: "HTTP ${response.code()}")
        }
    }

    suspend fun deleteActivity(id: String): Result<Unit> = runCatching {
        val response = api.deleteActivity(bearer(), id)
        if (response.isSuccessful) {
            removeLocal(id)
            Unit
        } else {
            error("${response.code()}")
        }
    }

    fun addLocal(activity: Activity) {
        cache.removeAll { it.id == activity.id }
        cache.add(activity)
        cache.sortByDescending { it.occurredAt }
    }

    fun removeLocal(id: String) {
        cache.removeAll { it.id == id }
    }
}
