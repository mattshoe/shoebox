package com.mattshoe.shoebox.data.source.impl

import com.mattshoe.shoebox.data.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow

internal class PublisherDataSource<T: Any>(
    dispatcher: CoroutineDispatcher
): MemoryCachedDataSource<T>(dispatcher) {
    override val _data = MutableSharedFlow<DataResult<T>>(replay = 0)
}