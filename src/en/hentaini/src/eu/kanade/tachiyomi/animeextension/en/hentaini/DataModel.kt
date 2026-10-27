package eu.kanade.tachiyomi.animeextension.en.hentaini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StrapiSeriesListResponse(
    val data: List<StrapiSeries> = emptyList(),
    val meta: StrapiMeta = StrapiMeta(),
)

@Serializable
data class StrapiMeta(
    val pagination: StrapiPagination = StrapiPagination(),
)

@Serializable
data class StrapiPagination(
    val page: Int = 1,
    val pageSize: Int = 24,
    val pageCount: Int = 1,
    val total: Int = 0,
)

@Serializable
data class StrapiSeries(
    val id: Long? = null,
    val title: String = "",
    @SerialName("title_english")
    val titleEnglish: String? = null,
    val synopsis: String? = null,
    val censorship: Boolean? = null,
    @SerialName("next_episode")
    val nextEpisode: String? = null,
    val visits: Long? = null,
    val url: String = "",
    val status: StrapiNamedObject? = null,
    val studio: StrapiNamedObject? = null,
    val producer: StrapiNamedObject? = null,
    val images: List<StrapiImage> = emptyList(),
    val episodes: List<StrapiEpisode> = emptyList(),
    val genres: String? = null,
    @SerialName("genreList")
    val genreList: List<StrapiNamedObject> = emptyList(),
    val publishedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class StrapiNamedObject(
    val id: Long? = null,
    val name: String = "",
)

@Serializable
data class StrapiImage(
    val id: Long? = null,
    val path: String = "",
    val placeholder: String? = null,
    @SerialName("image_type")
    val imageType: StrapiNamedObject? = null,
)

@Serializable
data class StrapiEpisode(
    val id: Long? = null,
    @SerialName("episode_number")
    val episodeNumber: Float? = null,
    val visible: Boolean = true,
    val players: String? = null,
    val publishedAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class StrapiSingleEpisodeResponse(
    val data: StrapiEpisode? = null,
)

@Serializable
data class PlayerEntry(
    val name: String = "",
    val url: String = "",
    val code: String? = null,
)

@Serializable
data class BurstCloudDto(
    val purchase: BurstCloudPurchase = BurstCloudPurchase(),
)

@Serializable
data class BurstCloudPurchase(
    val cdnUrl: String = "",
)
