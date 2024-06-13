package com.mattshoe.shoebox.datasource.serialize

import kotlinx.serialization.KSerializer

class StringSerializer: Serializer<String> {
    override fun serialize(data: String): ByteArray {
        return data.encodeToByteArray()
    }

    override fun deserialize(data: ByteArray): String {
        return data.decodeToString()
    }
}