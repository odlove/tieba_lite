package app.tiebalite.core.ui.emoticon

import androidx.annotation.DrawableRes
import app.tiebalite.core.ui.R

sealed interface EmoticonAsset {
    data class LocalRes(
        @param:DrawableRes val resId: Int,
    ) : EmoticonAsset

    data class Remote(
        val url: String,
    ) : EmoticonAsset

    data class FallbackText(
        val text: String,
    ) : EmoticonAsset
}

fun interface EmoticonResolver {
    fun resolve(
        id: String?,
        name: String,
    ): EmoticonAsset
}

object DefaultEmoticonResolver : EmoticonResolver {
    override fun resolve(
        id: String?,
        name: String,
    ): EmoticonAsset {
        val normalizedId =
            id
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::normalizeEmoticonId)
        if (normalizedId != null) {
            val localRes = localResById[normalizedId]
            if (localRes != null) {
                return EmoticonAsset.LocalRes(localRes)
            }
            return EmoticonAsset.Remote(url = buildEmoticonUrl(normalizedId))
        }

        val normalizedName = name.trim()
        val fallbackId = fallbackIdByName[normalizedName]
        if (fallbackId != null) {
            val localRes = localResById[fallbackId]
            if (localRes != null) {
                return EmoticonAsset.LocalRes(localRes)
            }
            return EmoticonAsset.Remote(url = buildEmoticonUrl(fallbackId))
        }

        return EmoticonAsset.FallbackText(text = fallbackText(name))
    }

    private fun buildEmoticonUrl(id: String): String = "$EmoticonBaseUrl/$id.png"

    private fun normalizeEmoticonId(rawId: String): String = if (rawId == "image_emoticon") "image_emoticon1" else rawId

    private fun fallbackText(name: String): String = "#(${name.ifBlank { "表情" }})"

    private const val EmoticonBaseUrl = "https://static.tieba.baidu.com/tb/editor/images/client"

    private val localResById =
        mapOf(
                "image_emoticon1" to R.drawable.image_emoticon1,
                "image_emoticon2" to R.drawable.image_emoticon2,
                "image_emoticon3" to R.drawable.image_emoticon3,
                "image_emoticon4" to R.drawable.image_emoticon4,
                "image_emoticon5" to R.drawable.image_emoticon5,
                "image_emoticon6" to R.drawable.image_emoticon6,
                "image_emoticon7" to R.drawable.image_emoticon7,
                "image_emoticon8" to R.drawable.image_emoticon8,
                "image_emoticon9" to R.drawable.image_emoticon9,
                "image_emoticon10" to R.drawable.image_emoticon10,
                "image_emoticon11" to R.drawable.image_emoticon11,
                "image_emoticon12" to R.drawable.image_emoticon12,
                "image_emoticon13" to R.drawable.image_emoticon13,
                "image_emoticon14" to R.drawable.image_emoticon14,
                "image_emoticon15" to R.drawable.image_emoticon15,
                "image_emoticon16" to R.drawable.image_emoticon16,
                "image_emoticon17" to R.drawable.image_emoticon17,
                "image_emoticon18" to R.drawable.image_emoticon18,
                "image_emoticon19" to R.drawable.image_emoticon19,
                "image_emoticon20" to R.drawable.image_emoticon20,
                "image_emoticon21" to R.drawable.image_emoticon21,
                "image_emoticon22" to R.drawable.image_emoticon22,
                "image_emoticon23" to R.drawable.image_emoticon23,
                "image_emoticon24" to R.drawable.image_emoticon24,
                "image_emoticon25" to R.drawable.image_emoticon25,
                "image_emoticon26" to R.drawable.image_emoticon26,
                "image_emoticon27" to R.drawable.image_emoticon27,
                "image_emoticon28" to R.drawable.image_emoticon28,
                "image_emoticon29" to R.drawable.image_emoticon29,
                "image_emoticon30" to R.drawable.image_emoticon30,
                "image_emoticon31" to R.drawable.image_emoticon31,
                "image_emoticon32" to R.drawable.image_emoticon32,
                "image_emoticon33" to R.drawable.image_emoticon33,
                "image_emoticon34" to R.drawable.image_emoticon34,
                "image_emoticon35" to R.drawable.image_emoticon35,
                "image_emoticon36" to R.drawable.image_emoticon36,
                "image_emoticon37" to R.drawable.image_emoticon37,
                "image_emoticon38" to R.drawable.image_emoticon38,
                "image_emoticon39" to R.drawable.image_emoticon39,
                "image_emoticon40" to R.drawable.image_emoticon40,
                "image_emoticon41" to R.drawable.image_emoticon41,
                "image_emoticon42" to R.drawable.image_emoticon42,
                "image_emoticon43" to R.drawable.image_emoticon43,
                "image_emoticon44" to R.drawable.image_emoticon44,
                "image_emoticon45" to R.drawable.image_emoticon45,
                "image_emoticon46" to R.drawable.image_emoticon46,
                "image_emoticon47" to R.drawable.image_emoticon47,
                "image_emoticon48" to R.drawable.image_emoticon48,
                "image_emoticon49" to R.drawable.image_emoticon49,
                "image_emoticon50" to R.drawable.image_emoticon50,
                "image_emoticon89" to R.drawable.image_emoticon89,
            )

    private val fallbackIdByName =
        mapOf(
                "滑稽" to "image_emoticon25",
                "呵呵" to "image_emoticon1",
                "哈哈" to "image_emoticon2",
                "啊" to "image_emoticon4",
                "开心" to "image_emoticon7",
                "酷" to "image_emoticon5",
                "汗" to "image_emoticon8",
                "怒" to "image_emoticon6",
                "鄙视" to "image_emoticon11",
                "不高兴" to "image_emoticon12",
                "泪" to "image_emoticon9",
                "吐舌" to "image_emoticon3",
                "黑线" to "image_emoticon10",
                "乖" to "image_emoticon28",
                "呼~" to "image_emoticon21",
                "花心" to "image_emoticon20",
                "惊哭" to "image_emoticon30",
                "惊讶" to "image_emoticon32",
                "狂汗" to "image_emoticon27",
                "冷" to "image_emoticon23",
                "勉强" to "image_emoticon26",
                "喷" to "image_emoticon33",
                "噗" to "image_emoticon89",
                "钱" to "image_emoticon14",
                "生气" to "image_emoticon31",
                "睡觉" to "image_emoticon29",
                "太开心" to "image_emoticon24",
                "吐" to "image_emoticon17",
                "委屈" to "image_emoticon19",
                "笑眼" to "image_emoticon22",
                "咦" to "image_emoticon18",
                "阴险" to "image_emoticon16",
                "疑问" to "image_emoticon15",
                "真棒" to "image_emoticon13",
                "爱心" to "image_emoticon34",
                "心碎" to "image_emoticon35",
                "玫瑰" to "image_emoticon36",
                "礼物" to "image_emoticon37",
                "彩虹" to "image_emoticon38",
                "星星月亮" to "image_emoticon39",
                "太阳" to "image_emoticon40",
                "钱币" to "image_emoticon41",
                "灯泡" to "image_emoticon42",
                "茶杯" to "image_emoticon43",
                "蛋糕" to "image_emoticon44",
                "音乐" to "image_emoticon45",
                "haha" to "image_emoticon46",
                "胜利" to "image_emoticon47",
                "大拇指" to "image_emoticon48",
                "弱" to "image_emoticon49",
                "OK" to "image_emoticon50",
            )
}
