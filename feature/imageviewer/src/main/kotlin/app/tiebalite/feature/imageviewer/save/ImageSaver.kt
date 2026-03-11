package app.tiebalite.feature.imageviewer.save

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import app.tiebalite.core.network.client.NetworkClientFactory
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

internal suspend fun saveImageToGallery(
    context: Context,
    imageUrl: String,
): ImageSaveResult =
    withContext(Dispatchers.IO) {
        runCatching {
            val fileName = buildDisplayName(imageUrl)
            val mimeType = inferMimeType(fileName)
            val savedPath =
                openDiskCacheSnapshot(context, imageUrl)?.use { snapshot ->
                    File(snapshot.data.toString()).inputStream().use { inputStream ->
                        writeImageToGallery(
                            context = context,
                            fileName = fileName,
                            mimeType = mimeType,
                            inputStream = inputStream,
                        )
                    }
                } ?: downloadAndWriteImage(
                    context = context,
                    imageUrl = imageUrl,
                    fallbackFileName = fileName,
                )
            ImageSaveResult.Success(savedPath = savedPath)
        }.getOrElse { throwable ->
            ImageSaveResult.Failure(throwable = throwable)
        }
    }

private suspend fun openDiskCacheSnapshot(
    context: Context,
    imageUrl: String,
): coil3.disk.DiskCache.Snapshot? {
    val diskCacheKey =
        resolveDiskCacheKey(
            context = context,
            imageUrl = imageUrl,
        ) ?: return null
    return context.imageLoader.diskCache?.openSnapshot(diskCacheKey)
}

private suspend fun resolveDiskCacheKey(
    context: Context,
    imageUrl: String,
): String? {
    val result =
        context.imageLoader.execute(
            ImageRequest
                .Builder(context)
                .data(imageUrl)
                .networkCachePolicy(CachePolicy.DISABLED)
                .build(),
        )
    return (result as? SuccessResult)?.diskCacheKey
}

private fun downloadAndWriteImage(
    context: Context,
    imageUrl: String,
    fallbackFileName: String,
): String {
    val client = NetworkClientFactory.createOkHttpClient()
    val request =
        Request
            .Builder()
            .url(imageUrl)
            .build()
    client.newCall(request).execute().use { response ->
        check(response.isSuccessful) { "Download failed with HTTP ${response.code}" }
        val body = checkNotNull(response.body) { "Empty response body" }
        val responseMimeType = body.contentType()?.toString()
        val fileName = rebuildDisplayName(fallbackFileName, responseMimeType)
        return body.byteStream().use { inputStream ->
            writeImageToGallery(
                context = context,
                fileName = fileName,
                mimeType = responseMimeType ?: inferMimeType(fileName),
                inputStream = inputStream,
            )
        }
    }
}

private fun writeImageToGallery(
    context: Context,
    fileName: String,
    mimeType: String,
    inputStream: InputStream,
): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        writeImageToMediaStore(
            context = context,
            fileName = fileName,
            mimeType = mimeType,
            inputStream = inputStream,
        )
    } else {
        writeImageToExternalPictures(
            context = context,
            fileName = fileName,
            inputStream = inputStream,
        )
    }

private fun writeImageToMediaStore(
    context: Context,
    fileName: String,
    mimeType: String,
    inputStream: InputStream,
): String {
    val resolver = context.contentResolver
    val relativePath = "${Environment.DIRECTORY_PICTURES}/tieba_lite"
    val values =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    val uri =
        checkNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values),
        ) { "Failed to create media store record" }

    try {
        resolver.openOutputStream(uri)?.use { outputStream ->
            inputStream.copyTo(outputStream)
        } ?: error("Failed to open media store output stream")
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    } catch (throwable: Throwable) {
        resolver.delete(uri, null, null)
        throw throwable
    }
    return relativePath
}

private fun writeImageToExternalPictures(
    context: Context,
    fileName: String,
    inputStream: InputStream,
): String {
    val picturesDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val appDir = File(picturesDir, "tieba_lite")
    if (!appDir.exists()) {
        check(appDir.mkdirs()) { "Failed to create picture directory" }
    }
    val targetFile = File(appDir, fileName)
    FileOutputStream(targetFile).use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    MediaScannerConnection.scanFile(
        context,
        arrayOf(targetFile.absolutePath),
        null,
        null,
    )
    return targetFile.absolutePath
}

private fun buildDisplayName(imageUrl: String): String {
    val guessed = URLUtil.guessFileName(imageUrl, null, null)
    if (guessed.contains('.')) {
        return guessed
    }
    return "tieba_${System.currentTimeMillis()}.jpg"
}

private fun rebuildDisplayName(
    fileName: String,
    mimeType: String?,
): String {
    val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
    if (extension.isNullOrBlank()) {
        return fileName
    }
    val baseName = fileName.substringBeforeLast('.', fileName)
    return "$baseName.${extension.lowercase(Locale.US)}"
}

private fun inferMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
    return when (extension) {
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        else -> "image/jpeg"
    }
}

internal sealed interface ImageSaveResult {
    data class Success(
        val savedPath: String,
    ) : ImageSaveResult

    data class Failure(
        val throwable: Throwable,
    ) : ImageSaveResult
}
