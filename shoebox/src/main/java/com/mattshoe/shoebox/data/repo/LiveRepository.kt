package com.mattshoe.shoebox.data.repo

import com.mattshoe.shoebox.data.DataResult
import kotlinx.coroutines.flow.Flow
import java.io.Closeable
import kotlin.reflect.KClass

interface LiveRepository<TParams: Any, TData: Any>: Closeable {
    val clazz: KClass<TData>
    val data: Flow<DataResult<TData>>

    suspend fun fetch(data: TParams)
    suspend fun refresh()
    suspend fun clear()
    override fun close()
}