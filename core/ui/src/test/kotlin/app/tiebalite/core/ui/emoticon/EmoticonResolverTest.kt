package app.tiebalite.core.ui.emoticon

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EmoticonResolverTest {
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
