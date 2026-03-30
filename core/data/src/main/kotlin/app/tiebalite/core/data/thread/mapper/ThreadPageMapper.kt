package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.thread.ThreadPage
import app.tiebalite.core.network.proto.thread.ThreadPostLite
import app.tiebalite.core.network.source.tbclient.thread.PbPageRaw

class ThreadPageMapper {
    private val authorResolver = ThreadAuthorResolver()
    private val firstFloorPostMapper = ThreadFirstFloorPostMapper()
    private val replyPostMapper = ThreadReplyPostMapper()

    fun map(raw: PbPageRaw): ThreadPage {
        val data = raw.response.data
        val thread = data.thread
        val threadId = thread.id
        val userMap = authorResolver.buildUserMap(data.userListList)
        val fallbackThreadTitle = thread.title.ifBlank { "(无标题)" }
        val containsFirstFloorPost = data.postListList.any { post -> post.floor == 1 }
        val firstFloorPostLite = resolveFirstFloorPost(raw = raw)
        val threadAgreeCount = thread.agreeNum.takeIf { value -> value > 0 }?.toLong()
        val nextPagePostId = resolveNextPagePostId(thread.pids)
        val firstFloorPost =
            firstFloorPostLite?.let { post ->
                firstFloorPostMapper.map(
                    post = post,
                    author = authorResolver.resolve(post = post, userMap = userMap),
                    fallbackThreadTitle = fallbackThreadTitle,
                    threadAgreeCount = threadAgreeCount,
                )
            }
        val posts =
            buildReplyPostList(
                raw = raw,
            ).map { post ->
                replyPostMapper.map(
                    post = post,
                    author = authorResolver.resolve(post = post, userMap = userMap),
                    userMap = userMap,
                )
            }

        val page =
            ThreadPage(
            threadId = threadId,
            forumId = data.forum.id.takeIf { it > 0 },
            forumName = data.forum.name.ifBlank { thread.fname }.ifBlank { null },
            forumAvatarUrl = normalizeUrl(data.forum.avatar),
            firstFloorPost = firstFloorPost,
            currentPage = data.page.currentPage.takeIf { it > 0 } ?: 1,
            totalPage = data.page.totalPage.takeIf { it > 0 } ?: 1,
            nextPagePostId = nextPagePostId,
            containsFirstFloorPost = containsFirstFloorPost,
            hasMore = data.page.hasMore != 0,
            hasPrevious = data.page.hasPrev != 0,
            posts = posts,
        )
        return page
    }

    private fun resolveFirstFloorPost(raw: PbPageRaw): ThreadPostLite? {
        return raw.response.data.postListList.firstOrNull { post -> post.floor == 1 }
    }

    private fun buildReplyPostList(
        raw: PbPageRaw,
    ): List<ThreadPostLite> =
        raw.response.data.postListList.filter { post -> post.floor != 1 }

    private fun resolveNextPagePostId(
        pids: String,
    ): Long =
        pids
            .split(',')
            .asSequence()
            .map { value -> value.trim() }
            .firstOrNull { value -> value.isNotEmpty() }
            ?.toLongOrNull()
            ?: 0L
}
