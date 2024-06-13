package com.mattshoe.shoebox.data.source.serialize

import com.google.gson.Gson
import kotlin.reflect.KClass

internal class GsonSerializer<T: Any>(
    private val clazz: KClass<T>
): Serializer<T> {
    private val gson = Gson()

    override fun serialize(data: T): ByteArray {
        return gson.toJson(data).encodeToByteArray()
    }

    override fun deserialize(data: ByteArray): T {
        return gson.fromJson(data.decodeToString(), clazz.java)
    }

}