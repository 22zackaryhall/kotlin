@file:JvmName("AndroidDocumentSnapshot")

package tz.co.asoft.firebase.firestore.snapshot

import com.google.gson.Gson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

actual typealias DocumentSnapshot = com.google.firebase.firestore.DocumentSnapshot

actual fun DocumentSnapshot.toJson(): String? = data()?.let { Gson().toJson(it) }

actual fun DocumentSnapshot.get(fieldPath: String): Any? = get(fieldPath)
actual fun DocumentSnapshot.data(): Map<String, Any>? = data
actual val DocumentSnapshot.id: String
    get() = id