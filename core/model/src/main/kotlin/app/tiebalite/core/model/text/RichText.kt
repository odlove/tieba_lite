package app.tiebalite.core.model.text

data class RichText(
    val parts: List<RichTextPart> = emptyList(),
) {
    val plainText: String
        get() = parts.joinToString(separator = "") { part -> part.plainText }

    fun isBlank(): Boolean = plainText.isBlank()

    fun isNotBlank(): Boolean = plainText.isNotBlank()

    companion object {
        fun text(value: String): RichText =
            RichText(
                parts =
                    value
                        .takeIf { it.isNotEmpty() }
                        ?.let { listOf(RichTextPart.Text(it)) }
                        ?: emptyList(),
            )
    }
}

sealed interface RichTextPart {
    val plainText: String

    data class Text(
        val text: String,
    ) : RichTextPart {
        override val plainText: String
            get() = text
    }

    data class Link(
        val text: String,
        val url: String,
    ) : RichTextPart {
        override val plainText: String
            get() = text.ifEmpty { url }
    }

    data class Mention(
        val text: String,
        val uid: Long? = null,
    ) : RichTextPart {
        override val plainText: String
            get() = text
    }

    data class Emoticon(
        val id: String? = null,
        val name: String,
    ) : RichTextPart {
        override val plainText: String
            get() = "#(${name.ifBlank { "表情" }})"
    }

    data class Unknown(
        val type: Int,
        val text: String,
        val link: String,
    ) : RichTextPart {
        override val plainText: String
            get() = text.ifEmpty { link }
    }
}
