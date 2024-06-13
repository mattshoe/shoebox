package com.mattshoe.test.shoebox

import com.mattshoe.shoebox.data.repo.LiveRepository
import com.mattshoe.shoebox.data.repo.simpleLiveRepository

interface Service {
    suspend fun foo(bar: Int): String
}

class Repo(service: Service):
    LiveRepository<Int, String> by simpleLiveRepository(
        String::class,
        service::foo
    )
