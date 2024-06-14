package com.mattshoe.shoebox.data.repo

import kotlin.reflect.KClass

fun <TParams: Any, TData: Any> transientRepository(
    clazz: KClass<TData>,
    fetchData: suspend (TParams) -> TData
): SingleSourceLiveRepository<TParams, TData> {
    return SingleSourceLiveRepositoryImpl(clazz, fetchData)
}

fun <TParams: Any, TData: Any> singleSourceLiveRepository(
    clazz: KClass<TData>,
    fetchData: suspend (TParams) -> TData
): SingleSourceLiveRepository<TParams, TData> {
    return SingleSourceLiveRepositoryImpl(clazz, fetchData)
}

fun <TParams: Any, TData: Any> multiSourceLiveRepository(
    clazz: KClass<TData>,
    fetchData: suspend (TParams) -> TData
): MultiSourceLiveRepository<TParams, TData> {
    return MultiSourceLiveRepositoryImpl(clazz, fetchData)
}