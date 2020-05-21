package tz.co.asoft.logging

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tz.co.asoft.persist.repo.IRepo
import tz.co.asoft.persist.tools.Cause

actual open class Logger actual constructor(
    protected actual val source: String,
    protected actual val repo: IRepo<Log>?
) {
    actual var tag = ""

    private val origin get() = if (tag.isEmpty()) source else "$source/$tag"

    actual fun d(msg: String) {
        val log = Log(Log.Level.DEBUG.name, msg, origin)
        println(log.toString())
        log.send()
    }

    actual fun e(msg: String, c: Cause?) {
        val log = Log(Log.Level.ERROR.name, msg, origin)
        println(log.toString())
        c?.printStackTrace()
        log.send()
    }

    actual fun e(c: Cause?) {
        val log = Log(Log.Level.ERROR.name, c?.message ?: "No Message", origin)
        println(log.toString())
        c?.printStackTrace()
        log.send()
    }

    actual fun f(msg: String, c: Cause?) {
        val log = Log(Log.Level.FAILURE.name, msg, origin)
        println(log.toString())
        c?.printStackTrace()
        log.send()
    }

    actual fun f(c: Cause?) {
        val log = Log(Log.Level.FAILURE.name, c?.message ?: "No Message", origin)
        println(log.toString())
        c?.printStackTrace()
        log.send()
    }

    actual fun w(msg: String) {
        val log = Log(Log.Level.WARNING.name, msg, origin)
        println(log.toString())
        log.send()
    }

    actual fun i(msg: String) {
        val log = Log(Log.Level.INFO.name, msg, origin)
        println(log.toString())
        log.send()
    }

    actual fun obj(vararg o: Any?) {
        for (it in o) {
            val log = Log(Log.Level.INFO.name, it.toString(), origin)
            println(log.toString())
        }
    }

    actual fun obj(o: Any?) {
        val log = Log(Log.Level.INFO.name, o.toString(), origin)
        println(log.toString())
    }

    private fun Log.send() = GlobalScope.launch {
        repo?.create(this@send)
    }
}
