package com.mattshoe.shoebox.datasource.impl

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.mattshoe.shoebox.datasource.DataResult
import com.mattshoe.shoebox.datasource.DataSource
import com.mattshoe.shoebox.datasource.serialize.Serializer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * This implementation of [DataSource] only caches data in-memory rather than on-disk.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal open class SharedPrefsDataSource<T: Any>(
    private val dispatcher: CoroutineDispatcher,
    private val context: Context,
    private val clazz: Class<T>,
    private val key: String?,
    private val serializer: Serializer<T>
): DataSource<T> {
    private val dataMutex = Mutex()
    protected open  val _data = MutableSharedFlow<DataResult<T>>(replay = 1)
    private lateinit var dataRetrievalAction: suspend () -> T
    private val prefs = context.getSharedPreferences("$context.packageName.shoebox.prefs", MODE_PRIVATE)
    private val prefsKey = "${context.packageName}.shoebox.prefs:${key ?: clazz.canonicalName}"

    override val data: Flow<DataResult<T>> = _data

    override suspend fun initialize(dataRetrieval: suspend () -> T) = withContext(dispatcher) {
        fetchData(forceFetch = false, dataRetrieval)
    }

    override suspend fun refresh() = withContext(dispatcher) {
        require(this@SharedPrefsDataSource::dataRetrievalAction.isInitialized) {
            "Refresh was invoked before the data source was initialized."
        }
        fetchData(forceFetch = true, null)
    }

    override suspend fun invalidate() {
        prefs.edit().remove(prefsKey).apply()
        _data.resetReplayCache()
        _data.emit(DataResult.Invalidated())
    }

    private suspend fun fetchData(forceFetch: Boolean, dataRetrieval: (suspend () -> T)?) {
        if (dataMutex.tryLock()) {
            try {
                dataRetrieval?.let {
                    this@SharedPrefsDataSource.dataRetrievalAction = it
                }

                if (canFetchData(forceFetch)) {
                    try {
                        _data.emit(
                            DataResult.Success(
                                doFetchData()
                            )
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        _data.emit(DataResult.Error(e))
                    }
                }
            } finally {
                dataMutex.unlock()
            }
        }
    }

    private fun canFetchData(forceFetch: Boolean): Boolean {
        val hasDataBeenFetchedAlready = prefs.contains(prefsKey)
        return (forceFetch || !hasDataBeenFetchedAlready)
    }

    private suspend fun doFetchData(): T {
        return if (prefs.contains(prefsKey)) {
            serializer.deserialize(
                prefs.getString(
                    prefsKey,
                    ""
                )?.encodeToByteArray()!!
            )
        } else {
            dataRetrievalAction.invoke().also {
                prefs.edit().putString(
                    prefsKey,
                    serializer.serialize(it).decodeToString()
                ).apply()
            }
        }
    }

}

