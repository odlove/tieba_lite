package app.tiebalite.feature.forum

import app.tiebalite.core.data.forum.repository.ForumRepository
import app.tiebalite.core.model.forum.ForumHeader
import app.tiebalite.core.model.forum.ForumPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleLikeKeepsSignedStateWhenRefollowingSignedForum() =
        runTest(testDispatcher) {
            val repository =
                FakeForumRepository(
                    header =
                        ForumHeader(
                            forumId = 155829L,
                            forumName = "python",
                            isLiked = true,
                            isSigned = true,
                            continuousSignDays = 7,
                        ),
                )
            val viewModel = ForumViewModel(forumName = "python", repository = repository)
            advanceUntilIdle()

            viewModel.toggleForumLike()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.header?.isLiked ?: true)

            viewModel.toggleForumLike()
            advanceUntilIdle()

            val header = viewModel.uiState.value.header ?: error("header missing")
            assertTrue(header.isLiked)
            assertTrue(header.isSigned)
            assertEquals(7, header.continuousSignDays)
        }
}

private class FakeForumRepository(
    private val header: ForumHeader,
) : ForumRepository {
    override suspend fun loadForumPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
    ): Result<ForumPage> = Result.success(ForumPage(header = header))

    override suspend fun followForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun unfollowForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun signInForum(
        forumId: Long,
        forumName: String,
    ): Result<Unit> = Result.success(Unit)
}
