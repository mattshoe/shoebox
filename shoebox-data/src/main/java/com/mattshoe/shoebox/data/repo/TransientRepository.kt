package com.mattshoe.shoebox.data.repo

interface TransientRepository<TParams: Any, TData: Any> {
    suspend fun fetch(params: TParams): TData
}

open class TransientRepositoryImpl<TParams: Any, TData: Any>(
    private val fetch: suspend (TParams) -> TData
): TransientRepository<TParams, TData> {
    override suspend fun fetch(params: TParams): TData = fetch(params)
}