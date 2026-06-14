package app.tiebalite.core.data.forum.repository

import app.tiebalite.core.data.forum.remote.ForumRemoteDataSource
import app.tiebalite.core.data.forum.remote.NetworkForumLikeRemoteClient
import app.tiebalite.core.data.forum.remote.NetworkForumPageRemoteClient
import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.forum.TbClientForumNetwork

object ForumRepositoryFactory {
    fun create(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        accountProvider: () -> AuthAccount? = { null },
    ): ForumRepository {
        val frsPageNetworkSource = TbClientForumNetwork.createFrsPageNetworkSource(baseUrl = baseUrl)
        val forumLikeNetworkSource = TbClientForumNetwork.createForumLikeNetworkSource(baseUrl = baseUrl)
        return ForumRepositoryImpl(
            remoteDataSource =
                ForumRemoteDataSource(
                    frsPageClient = NetworkForumPageRemoteClient(frsPageNetworkSource),
                    forumLikeClient = NetworkForumLikeRemoteClient(forumLikeNetworkSource),
                    accountProvider = accountProvider,
                ),
        )
    }
}
