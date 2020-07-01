package tz.co.asoft.auth

import kotlinx.serialization.Serializable

@Serializable
data class App(
    override var id: Long? = null,
    override var uid: String = "",
    override val name: String,
    override var deleted: Boolean = false,
    val permits: List<String> = listOf(),
    val apiKey: String? = null
) : Claimer