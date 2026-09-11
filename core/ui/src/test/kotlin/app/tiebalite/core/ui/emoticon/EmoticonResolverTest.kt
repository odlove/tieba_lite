package app.tiebalite.core.ui.emoticon

import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class EmoticonResolverTest {
    @Test
    fun resolveUsesTextForMissingResources() {
        for (id in listOf(null, " ", "image_emoticon999999")) {
            assertEquals(
                EmoticonAsset.FallbackText("#(新表情)"),
                DefaultEmoticonResolver.resolve(id = id, name = " #(新表情) "),
            )
            assertEquals(
                EmoticonAsset.FallbackText("#(表情)"),
                DefaultEmoticonResolver.resolve(id = id, name = " "),
            )
        }
    }

    @Test
    fun resolveFallsBackToNameWhenIdIsMissingAndWarnsOnce() {
        val id = "image_emoticon999998"
        val expected = DefaultEmoticonResolver.resolve(id = "image_emoticon9", name = "泪")
        repeat(2) {
            assertEquals(expected, DefaultEmoticonResolver.resolve(id = id, name = "#(泪)"))
        }
        val warning = ShadowLog.getLogsForTag("EmoticonResolver").filter { it.msg.contains(id) }.single()
        assertEquals(Log.WARN, warning.type)
        assertTrue(warning.msg.contains("name=泪"))
    }

    @Test
    fun resolveWarnsOnceWhenOnlyTextIsAvailable() {
        val id = "image_emoticon999997"
        repeat(2) {
            assertEquals(
                EmoticonAsset.FallbackText("#(新表情)"),
                DefaultEmoticonResolver.resolve(id = id, name = "新表情"),
            )
        }
        assertEquals(1, ShadowLog.getLogsForTag("EmoticonResolver").count { it.msg.contains(id) })
    }

    @Test
    fun resolveUsesLocalResourceForKnownId() {
        val asset = DefaultEmoticonResolver.resolve(id = "image_emoticon9", name = "泪")

        assertTrue(asset is EmoticonAsset.LocalRes)
    }

    @Test
    fun resolveNormalizesBareFirstEmoticonId() {
        val asset = DefaultEmoticonResolver.resolve(id = "image_emoticon", name = "呵呵")

        assertTrue(asset is EmoticonAsset.LocalRes)
    }

    @Test
    fun resolveNormalizesSharpWrappedName() {
        val asset = DefaultEmoticonResolver.resolve(id = null, name = "#(泪)")

        assertTrue(asset is EmoticonAsset.LocalRes)
    }
}
