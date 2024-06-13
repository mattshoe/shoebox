package com.mattshoe.shoebox.datasource.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mattshoe.shoebox.datasource.DataResult
import com.mattshoe.shoebox.datasource.DataSource
import com.mattshoe.shoebox.datasource.serialize.Serializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException


@OptIn(ExperimentalCoroutinesApi::class)
internal open class DataStoreDataSource<T: Any>(
    private val dispatcher: CoroutineDispatcher,
    private val context: Context,
    private val clazz: Class<T>,
    private val key: String?,
    private val serializer: Serializer<T>
) : DataSource<T> {
    private val dataMutex = Mutex()
    protected open val _data = MutableSharedFlow<DataResult<T>>(replay = 1)
    private lateinit var dataRetrievalAction: suspend () -> T
    private val prefsKey = stringPreferencesKey("${context.packageName}.shoebox.prefs:${key ?: clazz.canonicalName}")
    private val Context.dataStore by preferencesDataStore(name = "${context.packageName}.shoebox.prefs:${key ?: clazz.canonicalName}")

    override val data: Flow<DataResult<T>> = _data

    override suspend fun initialize(dataRetrieval: suspend () -> T) = withContext(dispatcher) {
        fetchData(forceFetch = false, dataRetrieval)
    }

    override suspend fun refresh() = withContext(dispatcher) {
        require(this@DataStoreDataSource::dataRetrievalAction.isInitialized) {
            "Refresh was invoked before the data source was initialized."
        }
        fetchData(forceFetch = true, null)
    }

    override suspend fun invalidate() {
        context.dataStore.edit { it.remove(prefsKey) }
        _data.resetReplayCache()
        _data.emit(DataResult.Invalidated())
    }

    private suspend fun fetchData(forceFetch: Boolean, dataRetrieval: (suspend () -> T)?) {
        if (dataMutex.tryLock()) {
            var cancellationException: CancellationException? = null
            dataRetrieval?.let {
                this@DataStoreDataSource.dataRetrievalAction = it
            }

            if (canFetchData(forceFetch)) {
                try {
                    _data.emit(DataResult.Success(doFetchData()))
                } catch (e: CancellationException) {
                    cancellationException = e
                } catch (e: Throwable) {
                    _data.emit(DataResult.Error(e))
                }
            }
            dataMutex.unlock()
            cancellationException?.let {
                throw it
            }
        }
    }

    private suspend fun canFetchData(forceFetch: Boolean): Boolean {
        val prefs = context.dataStore.data.first()
        val hasDataBeenFetchedAlready = prefs[prefsKey] != null
        return (forceFetch || !hasDataBeenFetchedAlready)
    }

    private suspend fun doFetchData(): T {
        val prefs = context.dataStore.data
            .catch { exception ->
                // Handle the exception if any error occurs while reading data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.first()

        return if (prefs[prefsKey] != null) {
            serializer.deserialize(prefs[prefsKey]!!.encodeToByteArray())
        } else {
            dataRetrievalAction.invoke().also { data ->
                context.dataStore.edit { settings ->
                    settings[prefsKey] = serializer.serialize(data).decodeToString()
                }
            }
        }
    }
}