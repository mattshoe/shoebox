package com.mattshoe.shoebox.datasource.serialize

interface Serializer<T: Any> {
    fun serialize(data: T): ByteArray
    fun deserialize(data: ByteArray): T
}