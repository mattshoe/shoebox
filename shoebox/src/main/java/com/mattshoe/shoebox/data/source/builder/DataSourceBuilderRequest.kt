package com.mattshoe.shoebox.data.source.builder

import android.content.Context
import kotlin.reflect.KClass

class DataSourceBuilderRequest {
    fun <T: Any> prefsCache(context: Context, clazz: KClass<T>) =
        SharedPrefsDataSourceBuilder(context, clazz)
    fun <T: Any> memoryCache(clazz: KClass<T>) =
        MemoryCacheDataSourceBuilder(clazz)
    fun <T: Any> publisher(clazz: KClass<T>) =
        PublisherDataSourceBuilder(clazz)
}