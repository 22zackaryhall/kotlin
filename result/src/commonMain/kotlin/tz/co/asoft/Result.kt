package tz.co.asoft

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

sealed class Result<out T> {
    @Serializable
    class Success<out T>(val data: T) : Result<T>()

    @Serializable
    class Failure<out T>(val msg: String) : Result<T>()

    companion object {
        val RJson = Json(
            configuration = JsonConfiguration(
                ignoreUnknownKeys = true,
                prettyPrint = true
            )
        )

        fun <T> stringify(serializer: KSerializer<T>, res: Result<T>) = when (res) {
            is Success -> RJson.stringify(Success.serializer(serializer), res)
            is Failure -> RJson.stringify(Failure.serializer(serializer), res)
        }

        fun <T> parse(serializer: KSerializer<T>, json: String) = try {
            RJson.parse(Success.serializer(serializer), json)
        } catch (c1: Throwable) {
            try {
                RJson.parse(Failure.serializer(serializer), json)
            } catch (c: Throwable) {
                Failure<T>(c.message ?: c.cause?.message ?: "Failed to serialize json")
            }
        }
    }
}

fun <T> Throwable.asFailure() = Result.Failure<T>(message ?: cause?.message ?: "Unknown Error")