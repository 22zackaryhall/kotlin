package tz.co.asoft

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.KSerializer

suspend fun <T> PipelineContext<Unit, ApplicationCall>.send(
    serializer: KSerializer<T>,
    res: Result<T>
) = call.respondText(Result.stringify(serializer, res), ContentType.Application.Json)