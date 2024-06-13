package com.mattshoe.shoebox.data.repo

import com.mattshoe.shoebox.data.DataResult
import com.mattshoe.shoebox.data.source.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

fun <TParams: Any, TData: Any> simpleLiveRepository(
    clazz: KClass<TData>,
    fetchData: suspend (TParams) -> TData
): LiveRepository<TParams, TData> {
    return SimpleSimpleLiveRepository(clazz, fetchData)
}

internal class SimpleSimpleLiveRepository<TParams: Any, TData: Any>(
    override val clazz: KClass<TData>,
    private val fetchData: suspend (TParams) -> TData,
): LiveRepository<TParams, TData> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataSource by lazy {
        DataSource.Builder()
            .memoryCache(clazz)
            .build()
    }
    override val data: Flow<DataResult<TData>> = dataSource.data

    override suspend fun fetch(data: TParams) {
        coroutineScope.launch {
            dataSource.initialize {
                fetchData(data)
            }
        }.join()
    }

    override suspend fun refresh() {
        coroutineScope.launch {
            dataSource.refresh()
        }.join()
    }

    override suspend fun clear() {
        dataSource.invalidate()
    }

    override fun close() {
        coroutineScope.cancel()
    }
}