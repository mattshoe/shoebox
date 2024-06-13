package com.mattshoe.shoebox.datasource.serialize

import android.os.Parcelable
import java.io.Serializable
import kotlin.reflect.KClass

interface SerializerFactory {
    fun <T: Any> forClass(clazz: KClass<T>): Serializer<T>
}

class SerializerFactoryImpl: SerializerFactory {
    override fun <T : Any> forClass(clazz: KClass<T>): Serializer<T> {
        return when (clazz) {
            Parcelable::class -> ParcelableSerializer(clazz as KClass<Parcelable>)
            Serializable::class -> SerializableSerializer()
            Byte::class -> ByteSerializer()
            Short::class -> ShortSerializer()
            Int::class -> IntSerializer()
            Long::class -> LongSerializer()
            Float::class -> FloatSerializer()
            Double::class -> DoubleSerializer()
            Char::class -> CharSerializer()
            Boolean::class -> BooleanSerializer()
            String::class -> StringSerializer()
            else -> GsonSerializer(clazz)
        } as Serializer<T>
    }
}