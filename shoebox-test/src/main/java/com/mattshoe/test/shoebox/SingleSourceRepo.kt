package com.mattshoe.test.shoebox

import com.mattshoe.shoebox.data.repo.MultiSourceLiveRepository
import com.mattshoe.shoebox.data.repo.SingleSourceLiveRepository
import com.mattshoe.shoebox.data.repo.TransientRepository
import com.mattshoe.shoebox.data.repo.multiSourceLiveRepository
import com.mattshoe.shoebox.data.repo.singleSourceLiveRepository
import com.mattshoe.shoebox.data.repo.transientRepository

interface Service {
    suspend fun foo(bar: Int): String
}

class TransientRepo(service: Service):
    TransientRepository<Int, String> by transientRepository(
        String::class,
        service::foo
    )


interface SingleSourceRepo {
    val foo: Int
}
class SingleSourceRepoImpl(service: Service): SingleSourceRepo, SingleSourceLiveRepository<Int, String>
by singleSourceLiveRepository(
    String::class,
    service::foo
) {
}

class MultiSourceRepo(service: Service):
    MultiSourceLiveRepository<Int, String> by multiSourceLiveRepository(
        String::class,
        service::foo
    )
