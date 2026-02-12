package app.tiebalite.core.data.recommend.repository

enum class RecommendLoadType(
    val wireValue: Int,
) {
    Refresh(1),
    LoadMore(2),
}
