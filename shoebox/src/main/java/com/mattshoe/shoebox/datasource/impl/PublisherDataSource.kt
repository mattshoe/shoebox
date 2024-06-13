package com.mattshoe.shoebox.datasource.impl

import com.mattshoe.shoebox.datasource.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow

internal class PublisherDataSource<T: Any>(
    dispatcher: CoroutineDispatcher
): MemoryCachedDataSource<T>(dispatcher) {
    override val _data = MutableSharedFlow<DataResult<T>>(replay = 0)
}