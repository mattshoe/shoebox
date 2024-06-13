package com.mattshoe.shoebox.datasource.builder

import com.mattshoe.shoebox.datasource.DataSource
import com.mattshoe.shoebox.datasource.impl.PublisherDataSource
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KClass

class PublisherDataSourceBuilder<T: Any>(
    clazz: KClass<T>
): DataSourceBuilder<T>(
    clazz
) {
    override fun build(): DataSource<T> {
        return PublisherDataSource(dispatcher ?: Dispatchers.IO)
    }
}