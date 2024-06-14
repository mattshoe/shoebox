package com.mattshoe.app.shoebox

import io.github.mattshoe.shoebox.data.repo.MultiSourceLiveRepository
import io.github.mattshoe.shoebox.data.repo.SingleSourceLiveRepository
import io.github.mattshoe.shoebox.data.repo.TransientRepository
import io.github.mattshoe.shoebox.data.repo.multiSourceLiveRepository
import io.github.mattshoe.shoebox.data.repo.singleSourceLiveRepository
import io.github.mattshoe.shoebox.data.repo.transientRepository

interface Service {
    suspend fun foo(bar: Int): String
}

class TransientRepo(service: Service): TransientRepository<Int, String>
by transientRepository(
    service::foo
)

class SingleSourceRepoImpl(service: Service): SingleSourceLiveRepository<Int, String>
by singleSourceLiveRepository(
    String::class,
    service::foo
)

class MultiSourceRepo(service: Service): MultiSourceLiveRepository<Int, String>
by multiSourceLiveRepository(
    String::class,
    service::foo
)
