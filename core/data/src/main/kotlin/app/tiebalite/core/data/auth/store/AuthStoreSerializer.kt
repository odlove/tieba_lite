package app.tiebalite.core.data.auth.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import app.tiebalite.core.proto.auth.AuthStoreProto
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

internal object AuthStoreSerializer : Serializer<AuthStoreProto> {
    override val defaultValue: AuthStoreProto = AuthStoreProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AuthStoreProto =
        try {
            AuthStoreProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read auth store proto.", exception)
        }

    override suspend fun writeTo(
        t: AuthStoreProto,
        output: OutputStream,
    ) {
        t.writeTo(output)
    }
}
