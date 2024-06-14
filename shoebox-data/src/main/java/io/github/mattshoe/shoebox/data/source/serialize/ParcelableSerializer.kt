package io.github.mattshoe.shoebox.data.source.serialize

import android.os.Parcel
import android.os.Parcelable
import kotlin.reflect.KClass

class ParcelableSerializer<T: Parcelable>(
    private val clazz: KClass<T>
) : Serializer<T> {
    override fun serialize(data: T): ByteArray {
        val parcel = Parcel.obtain()
        try {
            data.writeToParcel(parcel, 0)
            return parcel.marshall()
        } finally {
            parcel.recycle()
        }
    }

    override fun deserialize(data: ByteArray): T {
        val parcel = Parcel.obtain()
        try {
            parcel.unmarshall(data, 0, data.size)
            parcel.setDataPosition(0)
            return clazz.java.getDeclaredConstructor(Parcel::class.java).newInstance(parcel)
        } finally {
            parcel.recycle()
        }
    }
}