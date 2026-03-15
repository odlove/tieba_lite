package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.network.proto.thread.AgreeLite
import com.google.protobuf.ByteString

internal object ThreadAgreeParser {
    fun parseCount(raw: ByteString?): Long {
        if (raw == null || raw.isEmpty) {
            return 0
        }
        return runCatching {
            AgreeLite.parseFrom(raw).agreeNum
        }.getOrDefault(0)
    }
}
