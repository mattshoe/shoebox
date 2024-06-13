package com.mattshoe.shoebox.datasource

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.mattshoe.shoebox.datasource.impl.MemoryCachedDataSource
import com.mattshoe.shoebox.datasource.serialize.Serializer
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryCachedDataSourceTest {
    private lateinit var subject: MemoryCachedDataSource<String>
    private val unconfinedTestDispatcher = UnconfinedTestDispatcher()
    private val standardTestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        subject = MemoryCachedDataSource(unconfinedTestDispatcher)
    }

    @Test
    fun `WHEN dataRetrieval succeeds THEN success is emitted`() = runTest {

        val dataSource: DataSource<String> = DataSource.Builder()
            .prefsCache(mockk(relaxed = true), String::class)
            .key("derp")
            .build()


        subject.data.test {
            val expectedValue = "yew dun did it now"

            subject.initialize { expectedValue }

            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success(expectedValue))
        }
    }

    @Test
    fun `WHEN dataRetrieval fails THEN error is emitted`() = runTest {
        subject.data.test {
            val expectedValue = RuntimeException("oops")

            subject.initialize { throw expectedValue }

            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Error<String>(expectedValue))
        }
    }

    @Test
    fun `WHEN initialize is invoked multiple times sequentially THEN only the first invocation is honored`() = runTest {
        subject.data.test {
            val expectedValue = "first"

            subject.initialize { expectedValue }
            subject.initialize { "second" }
            subject.initialize { "third" }

            advanceUntilIdle()

            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success(expectedValue))
        }
    }

    @Test
    fun `WHEN initialize is invoked multiple times concurrently THEN only one operation is made`() = runTest(standardTestDispatcher) {
        subject = MemoryCachedDataSource(standardTestDispatcher)

        subject.data.test {
            val expectedValue = "first"

            launch {
                subject.initialize {
                    delay(500)
                    expectedValue
                }
            }
            launch {
                delay(100)
                subject.initialize {
                    "second"
                }
            }
            launch {
                delay(100)
                subject.initialize {
                    "third"
                }
            }

            advanceUntilIdle()

            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success(expectedValue))
        }
    }

    @Test
    fun `WHEN refresh is invoked concurrently THEN only one operation is performed`() = runTest(standardTestDispatcher) {
        var counter = 0
        subject = MemoryCachedDataSource(standardTestDispatcher)

        subject.data.test {
            subject.initialize {
                delay(500)
                counter.toString().also {
                    counter++
                }
            }
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("0"))

            launch {
                subject.refresh()
            }
            launch {
                subject.refresh()
            }
            launch {
                subject.refresh()
            }

            advanceUntilIdle()

            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("1"))
        }
    }

    @Test
    fun `WHEN refresh is invoked THEN new item is emitted`() = runTest {
        var counter = 0
        subject.data.test {
            subject.initialize {
                counter.toString().also {
                    counter++
                }
            }
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("0"))

            subject.refresh()
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("1"))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `WHEN refresh is invoked before initialize THEN exception is thrown`() = runTest {
        subject.refresh()
    }

}