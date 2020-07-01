package tz.co.asoft.platform.env

import kotlinx.serialization.json.JsonObject
import tz.co.asoft.platform.core.Ctx
import tz.co.asoft.platform.core.require

actual fun Ctx.env() = getEnv()

fun env() = getEnv()

private fun getEnv() = platformEnvironment.toJsonObject().toMap()

private fun <T> T.toJsonObject(): JsonObject = JSON.stringify(this).toJsonObject()

private val platformEnvironment = try {
    require<Any>("platform.environment.json")
} catch (e: Throwable) {
    Unit
}