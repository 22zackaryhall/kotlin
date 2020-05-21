package tz.co.asoft.auth.dao

import kotlinx.serialization.json.Json
import tz.co.asoft.auth.User
import tz.co.asoft.storage.IStorage

open class UsersLocalDao(private val db: IStorage) : IUsersLocalDao {
    private val serializer = User.serializer()

    override var data: MutableMap<String, User>? = mutableMapOf()

    override suspend fun create(t: User): User {
        db.set("device_user", Json.stringify(serializer, t))
        return super.create(t)
    }

    override suspend fun load(): User? {
        val json = db.get("device_user") ?: return null
        return Json.parse(serializer, json)
    }

    override suspend fun delete() {
        db.remove("device_user")
    }
}