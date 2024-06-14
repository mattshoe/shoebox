package com.mattshoe.test.shoebox

import com.mattshoe.shoebox.data.repo.SingleSourceLiveRepository
import com.mattshoe.shoebox.data.repo.singleSourceLiveRepository

interface Service {
    suspend fun foo(bar: Int): String
}

class Repo(service: Service):
    SingleSourceLiveRepository<Int, String> by singleSourceLiveRepository(
        String::class,
        service::foo
    )
