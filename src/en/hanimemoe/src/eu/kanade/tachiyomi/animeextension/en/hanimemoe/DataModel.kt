package eu.kanade.tachiyomi.animeextension.en.hanimemoe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InertiaPage(
    val component: String? = null,
    val props: PageProps = PageProps(),
    val url: String? = null,
)

@Serializable
data class PageProps(
    val videos: List<VideoItem> = emptyList(),
    val video: VideoDetails? = null,
    val series: SeriesDetails? = null,
    val siblings: List<VideoItem> = emptyList(),
    val meta: PageMeta = PageMeta(),
)

@Serializable
data class PageMeta(
    @SerialName("current_page")
    val currentPage: Int = 1,
    @SerialName("last_page")
    val lastPage: Int = 1,
    val total: Int = 0,
    @SerialName("per_page")
    val perPage: Int = 24,
)

@Serializable
data class VideoItem(
    val id: Long? = null,
    val slug: String = "",
    val title: String = "",
    val poster: String? = null,
    val thumbnail: String? = null,
    val duration: Long? = null,
    val views: Long? = null,
    @SerialName("views_human")
    val viewsHuman: String? = null,
    val brand: BrandItem? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("released_at")
    val releasedAt: String? = null,
    val playable: Boolean = true,
    val rank: Long? = null,
    val number: Long? = null,
)

@Serializable
data class BrandItem(
    val name: String = "",
    val slug: String = "",
    val logo: String? = null,
)

@Serializable
data class VideoDetails(
    val id: Long? = null,
    val slug: String = "",
    val title: String = "",
    val poster: String? = null,
    val thumbnail: String? = null,
    val duration: Long? = null,
    val views: Long? = null,
    @SerialName("views_human")
    val viewsHuman: String? = null,
    @SerialName("likes_count")
    val likesCount: Long? = null,
    @SerialName("dislikes_count")
    val dislikesCount: Long? = null,
    @SerialName("downloads_count")
    val downloadsCount: Long? = null,
    val rank: Long? = null,
    val brand: BrandItem? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("released_at")
    val releasedAt: String? = null,
    val playable: Boolean = true,
    @SerialName("is_censored")
    val isCensored: Boolean? = null,
    val number: Long? = null,
    val blurb: String? = null,
    val synopsis: String? = null,
    val sources: List<VideoSource> = emptyList(),
)

@Serializable
data class VideoSource(
    val id: Long? = null,
    val label: String? = null,
    val type: String? = null,
    val url: String = "",
    val quality: String? = null,
    val language: String? = null,
)

@Serializable
data class SeriesDetails(
    val id: Long? = null,
    val slug: String? = null,
    val title: String? = null,
    @SerialName("alt_titles")
    val altTitles: List<String> = emptyList(),
    val cover: String? = null,
    val banner: String? = null,
    val synopsis: String? = null,
    val status: String? = null,
    val year: Int? = null,
    @SerialName("is_censored")
    val isCensored: Boolean? = null,
    val brand: BrandItem? = null,
    val tags: List<TagItem> = emptyList(),
    @SerialName("videos_count")
    val videosCount: Int? = null,
    val rank: Long? = null,
    val views: Long? = null,
    @SerialName("views_human")
    val viewsHuman: String? = null,
)

@Serializable
data class TagItem(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class VimbedInertia(
    val props: VimbedProps = VimbedProps(),
)

@Serializable
data class VimbedProps(
    val video: VimbedVideo? = null,
)

@Serializable
data class VimbedVideo(
    val title: String? = null,
    val key: String? = null,
    @SerialName("stream_url")
    val streamUrl: String? = null,
    @SerialName("poster_url")
    val posterUrl: String? = null,
    @SerialName("download_url")
    val downloadUrl: String? = null,
)
