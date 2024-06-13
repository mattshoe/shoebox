package com.mattshoe.shoebox.data.source.builder

import com.mattshoe.shoebox.data.source.DataSource
import com.mattshoe.shoebox.data.source.impl.MemoryCachedDataSource
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

class MemoryCacheDataSourceBuilder<T: Any>(
    clazz: KClass<T>
): DataSourceBuilder<T>(
    clazz
) {
    override fun build(): DataSource<T> {
        return MemoryCachedDataSource(dispatcher ?: Dispatchers.IO)
    }
}