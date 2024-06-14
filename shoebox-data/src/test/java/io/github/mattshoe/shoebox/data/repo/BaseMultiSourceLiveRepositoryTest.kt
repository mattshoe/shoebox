package io.github.mattshoe.shoebox.data.repo

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.google.common.truth.Truth
import io.github.mattshoe.shoebox.data.DataResult
import io.github.mattshoe.shoebox.test.CoroutineTestRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration

@ExperimentalCoroutinesApi
class BaseMultiSourceLiveRepositoryTest {
    private lateinit var subject: TestMultiSourceLiveRepository

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Before
    fun setUp() {
        subject = TestMultiSourceLiveRepository(coroutineTestRule.testDispatcher)
    }

    @Test
    fun `WHEN stream is invoked THEN it should emit a value`() = runTest {
        subject.stream(42).test {
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("42"))
        }
    }

    @Test
    fun `WHEN stream is invoked multiple times THEN it should only make one service call`() = runTest(coroutineTestRule.testDispatcher) {
        Truth.assertThat(subject.stream(42).first()).isEqualTo(subject.stream(42).first())
        Truth.assertThat(subject.fetchCounter).isEqualTo(1)
    }

    @Test
    fun `WHEN stream is invoked with different values THEN it should only make one service call each`() = runTest(coroutineTestRule.testDispatcher) {
        Truth.assertThat(subject.stream(42).first()).isEqualTo(subject.stream(42).first())
        Truth.assertThat(subject.stream(43).first()).isEqualTo(subject.stream(43).first())
        Truth.assertThat(subject.fetchCounter).isEqualTo(2)
    }

    @Test
    fun `WHEN refresh is invoked THEN it should make 2 service calls`() = runTest {
        subject.stream(42).test {
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("42"))
            subject.refresh(42)
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(subject.fetchCounter).isEqualTo(2)
        }
    }

    @Test
    fun `WHEN refresh is invoked THEN it should emit data to all collectors`() = runTest(timeout = Duration.INFINITE) {
        turbineScope(timeout = Duration.INFINITE) {
            val turbine1 = subject.stream(42).testIn(backgroundScope)
            val turbine2 = subject.stream(42).testIn(backgroundScope)

            Truth.assertThat(turbine1.awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Success("42"))

            subject.refresh(42)

            Truth.assertThat(turbine1.awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Success("42"))
        }
    }

    @Test
    fun `WHEN refresh is invoked on a different param THEN it should not emit data to other collectors`() = runTest {
        turbineScope {
            val turbine1 = subject.stream(42).testIn(backgroundScope)
            val turbine2 = subject.stream(43).testIn(backgroundScope)

            Truth.assertThat(turbine1.awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Success("43"))

            subject.refresh(43)

            advanceUntilIdle()

            Truth.assertThat(subject.latestValue(42)).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Success("43"))
        }
    }

    @Test
    fun `WHEN stream is invoked multiple times THEN latestValue should match emission`() = runTest(coroutineTestRule.testDispatcher) {
        subject.stream(42).test {
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(subject.latestValue(42)).isEqualTo(DataResult.Success("42"))
        }
    }

    @Test
    fun `WHEN latestValue is invoked before stream THEN it is null`() = runTest(coroutineTestRule.testDispatcher) {
        Truth.assertThat(subject.latestValue(42)).isNull()
    }

    @Test
    fun `WHEN latestValue is invoked after invalidation THEN it is invalidated`() = runTest(coroutineTestRule.testDispatcher) {
        subject.stream(42).test {
            Truth.assertThat(awaitItem()).isEqualTo(DataResult.Success("42"))

            subject.invalidate(42)

            Truth.assertThat(awaitItem() is DataResult.Invalidated).isTrue()
            Truth.assertThat(subject.latestValue(42) is DataResult.Invalidated).isTrue()
        }
    }

    @Test
    fun `WHEN invalidateAll is invoked THEN all data sources are invalidated`() = runTest(coroutineTestRule.testDispatcher) {
        turbineScope {
            val turbine1 = subject.stream(42).testIn(backgroundScope)
            val turbine2 = subject.stream(43).testIn(backgroundScope)
            val turbine3 = subject.stream(44).testIn(backgroundScope)

            Truth.assertThat(turbine1.awaitItem()).isEqualTo(DataResult.Success("42"))
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Success("43"))
            Truth.assertThat(turbine3.awaitItem()).isEqualTo(DataResult.Success("44"))

            subject.invalidateAll()

            Truth.assertThat(turbine1.awaitItem()).isEqualTo(DataResult.Invalidated<Int>())
            Truth.assertThat(turbine2.awaitItem()).isEqualTo(DataResult.Invalidated<Int>())
            Truth.assertThat(turbine3.awaitItem()).isEqualTo(DataResult.Invalidated<Int>())
        }
    }

    // Test implementation of BaseMultiSourceLiveRepository for testing purposes
    private class TestMultiSourceLiveRepository(dispatcher: CoroutineDispatcher) : BaseMultiSourceLiveRepository<Int, String>(dispatcher) {
        override val dataType = String::class
        var fetchCounter = 0

        override suspend fun fetchData(params: Int): String {
            fetchCounter++
            return params.toString()
        }
    }
}