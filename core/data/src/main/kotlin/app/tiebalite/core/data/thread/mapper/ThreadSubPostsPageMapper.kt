package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadSubPostsPage
import app.tiebalite.core.network.source.tbclient.thread.PbFloorRaw

internal class ThreadSubPostsPageMapper(
    private val replyPostMapper: ThreadReplyPostMapper = ThreadReplyPostMapper(),
    private val subPostMapper: ThreadSubPostMapper = ThreadSubPostMapper(),
) {
    fun map(raw: PbFloorRaw): ThreadSubPostsPage {
        val data = raw.response.data
        val page = data.page
        val postLite = data.post.takeIf { post -> post.id > 0L }
        val post =
            postLite?.let { post ->
                replyPostMapper.map(
                    post = post,
                    author = post.author,
                )
            }
        val subPosts =
            data.subpostListList.map { subPost ->
                subPostMapper.map(
                    subPost = subPost,
                    author = subPost.author,
                )
            }

        val currentPage = page.currentPage.takeIf { value -> value > 0 } ?: 1
        val totalPage = page.totalPage.takeIf { value -> value > 0 } ?: currentPage

        return ThreadSubPostsPage(
            threadId =
                data.thread.id
                    .takeIf { id -> id > 0L }
                    ?: postLite?.tid
                    ?: 0L,
            forumId = data.forum.id,
            forumName = data.forum.name.takeIf { value -> value.isNotBlank() },
            threadAuthorId =
                data.thread.authorId
                    .takeIf { id -> id > 0L }
                    ?: data.thread.author.id.takeIf { id -> id > 0L },
            post = post,
            currentPage = currentPage,
            hasMore = currentPage < totalPage,
            totalCount = page.totalCount.takeIf { count -> count >= 0 } ?: subPosts.size,
            subPosts = subPosts,
        )
    }
}
