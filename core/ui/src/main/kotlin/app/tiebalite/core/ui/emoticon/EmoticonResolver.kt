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

    @DrawableRes
    private fun localResId(id: String): Int? = localResById[id]

    private fun fallbackText(name: String): String = "#(${name.ifBlank { "表情" }})"

    private const val EmoticonBaseUrl = "https://static.tieba.baidu.com/tb/editor/images/client"

    private val localResById =
        R.drawable::class.java.fields
            .asSequence()
            .filter { field -> field.name.startsWith("image_emoticon") }
            .associate { field -> field.name to field.getInt(null) }

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
                "哼" to "image_emoticon61",
                "吃瓜" to "image_emoticon62",
                "扔便便" to "image_emoticon63",
                "惊恐" to "image_emoticon64",
                "哎呦" to "image_emoticon65",
                "小乖" to "image_emoticon66",
                "捂嘴笑" to "image_emoticon67",
                "你懂的" to "image_emoticon68",
                "what" to "image_emoticon69",
                "酸爽" to "image_emoticon70",
                "呀咩爹" to "image_emoticon71",
                "笑尿" to "image_emoticon72",
                "挖鼻" to "image_emoticon73",
                "犀利" to "image_emoticon74",
                "小红脸" to "image_emoticon75",
                "懒得理" to "image_emoticon76",
                "沙发" to "image_emoticon77",
                "手纸" to "image_emoticon78",
                "香蕉" to "image_emoticon79",
                "便便" to "image_emoticon80",
                "药丸" to "image_emoticon81",
                "红领巾" to "image_emoticon82",
                "蜡烛" to "image_emoticon83",
                "三道杠" to "image_emoticon84",
                "暗中观察" to "image_emoticon85",
                "喝酒" to "image_emoticon87",
                "嘿嘿嘿" to "image_emoticon88",
                "困成狗" to "image_emoticon90",
                "微微一笑" to "image_emoticon91",
                "托腮" to "image_emoticon92",
                "摊手" to "image_emoticon93",
                "柯基暗中观察" to "image_emoticon94",
                "欢呼" to "image_emoticon95",
                "炸药" to "image_emoticon96",
                "突然兴奋" to "image_emoticon97",
                "紧张" to "image_emoticon98",
                "黑头瞪眼" to "image_emoticon99",
                "黑头高兴" to "image_emoticon100",
                "不跟丑人说话" to "image_emoticon101",
                "么么哒" to "image_emoticon102",
                "亲亲才能起来" to "image_emoticon103",
                "伦家只是宝宝" to "image_emoticon104",
                "你是我的人" to "image_emoticon105",
                "假装看不见" to "image_emoticon106",
                "单身等撩" to "image_emoticon107",
                "吓到宝宝了" to "image_emoticon108",
                "哈哈哈" to "image_emoticon109",
                "嗯嗯" to "image_emoticon110",
                "好幸福" to "image_emoticon111",
                "宝宝不开心" to "image_emoticon112",
                "小姐姐别走" to "image_emoticon113",
                "小姐姐在吗" to "image_emoticon114",
                "小姐姐来啦" to "image_emoticon115",
                "小姐姐来玩呀" to "image_emoticon116",
                "我养你" to "image_emoticon117",
                "我是不会骗你的" to "image_emoticon118",
                "扎心了" to "image_emoticon119",
                "无聊" to "image_emoticon120",
                "月亮代表我的心" to "image_emoticon121",
                "来追我呀" to "image_emoticon122",
                "爱你的形状" to "image_emoticon123",
                "白眼" to "image_emoticon124",
                "奥特曼" to "image_emoticon125",
                "不听" to "image_emoticon126",
                "干饭" to "image_emoticon127",
                "望远镜" to "image_emoticon128",
                "菜狗" to "image_emoticon129",
                "老虎" to "image_emoticon130",
                "嗷呜" to "image_emoticon131",
                "烟花" to "image_emoticon132",
                "香槟" to "image_emoticon133",
                "文字啊" to "image_emoticon134",
                "文字对" to "image_emoticon135",
                "鼠1" to "image_emoticon136",
                "鼠2" to "image_emoticon137",
            )
}
