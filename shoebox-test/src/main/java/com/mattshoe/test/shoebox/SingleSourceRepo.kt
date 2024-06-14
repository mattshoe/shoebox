package com.mattshoe.test.shoebox

import com.mattshoe.shoebox.data.repo.MultiSourceLiveRepository
import com.mattshoe.shoebox.data.repo.MultiSourceLiveRepositoryImpl
import com.mattshoe.shoebox.data.repo.SingleSourceLiveRepository
import com.mattshoe.shoebox.data.repo.multiSourceLiveRepository
import com.mattshoe.shoebox.data.repo.singleSourceLiveRepository

interface Service {
    suspend fun foo(bar: Int): String
}

class SingleSourceRepo(service: Service):
    SingleSourceLiveRepository<Int, String> by singleSourceLiveRepository(
        String::class,
        service::foo
    )

class MultiSourceRepo(service: Service):
    MultiSourceLiveRepository<Int, String> by multiSourceLiveRepository(
        String::class,
        service::foo
    )
