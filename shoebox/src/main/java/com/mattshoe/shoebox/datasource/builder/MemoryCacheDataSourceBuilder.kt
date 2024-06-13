package com.mattshoe.shoebox.datasource.builder

import com.mattshoe.shoebox.datasource.DataSource
import com.mattshoe.shoebox.datasource.impl.MemoryCachedDataSource
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