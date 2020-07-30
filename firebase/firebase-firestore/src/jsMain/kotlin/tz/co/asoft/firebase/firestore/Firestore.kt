package tz.co.asoft.firebase.firestore

import tz.co.asoft.FirebaseApp

@JsModule("firebase/firestore/memory")
private external val firestoreLib: dynamic

actual external class FirebaseFirestore {
    val app: FirebaseApp

    fun collection(path: String): CollectionReference

    @JsName("doc")
    fun document(path: String): DocumentReference

    fun batch(): WriteBatch
}

actual val FirebaseFirestore.app: FirebaseApp
    get() = app

actual fun FirebaseFirestore.collection(path: String): CollectionReference {
    return collection(path)
}

actual fun FirebaseFirestore.document(path: String): DocumentReference {
    return document(path)
}

actual fun FirebaseApp.firestore(): FirebaseFirestore {
    if (firestoreLib.isImported != true) {
        firestoreLib.isImported = true
    }
    return unsafeCast<dynamic>().firestore()
}

actual fun FirebaseFirestore.batch(): WriteBatch = batch()