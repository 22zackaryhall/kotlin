package tz.co.asoft

import kotlin.browser.window

var throttleFunctionId = 0

inline fun throttle(time: Long = 100L, crossinline run: () -> Unit) {
    window.clearInterval(throttleFunctionId)
    throttleFunctionId = window.setTimeout({
        run()
    }, time.toInt())
}