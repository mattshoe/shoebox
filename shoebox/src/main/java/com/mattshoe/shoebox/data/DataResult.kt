package com.mattshoe.shoebox.data

/**
 * ***A type that encapsulates the result of a single data retrieval operation.***
 *
 * Can be either [Success] or [Error].
 */
sealed interface DataResult<T: Any> {
    /**
     * Encapsulates the data of a successful data retrieval operation.
     */
    data class Success<T: Any>(val data: T): DataResult<T>

    /**
     * Encapsulates an error encountered by a data retrieval operation.
     */
    data class Error<T: Any>(val error: Throwable):
        DataResult<T>

    /**
     * Representation of an invalidated cache. Meaning any previous data
     * is no longer usable.
     */
    class Invalidated<T: Any>: DataResult<T>
}