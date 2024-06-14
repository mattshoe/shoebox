package io.github.mattshoe.shoebox.data.source.builder

import android.content.Context
import io.github.mattshoe.shoebox.data.source.DataSource
import io.github.mattshoe.shoebox.data.source.impl.SharedPrefsDataSource
import io.github.mattshoe.shoebox.data.source.serialize.Serializer
import io.github.mattshoe.shoebox.data.source.serialize.SerializerFactory
import io.github.mattshoe.shoebox.data.source.serialize.SerializerFactoryImpl
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

class SharedPrefsDataSourceBuilder<T: Any>(
    private val context: Context,
    clazz: KClass<T>
): DataSourceBuilder<T>(
    clazz
) {
    private val serializerFactory: SerializerFactory = SerializerFactoryImpl()
    private var serializer: Serializer<T>? = null
    private var key: String? = null

    fun key(key: String): SharedPrefsDataSourceBuilder<T> {
        this.key = key
        return this
    }

    fun serializer(serializer: Serializer<T>): SharedPrefsDataSourceBuilder<T> {
        this.serializer = serializer
        return this
    }

    override fun build(): DataSource<T> {
        return SharedPrefsDataSource(
            dispatcher ?: Dispatchers.IO,
            context,
            clazz.java,
            key,
            serializer ?: serializerFactory.forClass(clazz)
        )
    }
}