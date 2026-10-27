package eu.kanade.tachiyomi.animeextension.en.hanimemoe

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.lib.playlistutils.PlaylistUtils
import eu.kanade.tachiyomi.network.GET
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.parser.Parser
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HanimeMoe :
    AnimeHttpSource(),
    ConfigurableAnimeSource {

    override val name = "Hanime.moe"

    override val baseUrl = "https://hanime.moe"

    override val lang = "en"

    override val supportsLatest = true

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val playlistUtils by lazy { PlaylistUtils(client, headers) }

    private val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", Application.MODE_PRIVATE)
    }

    override fun headersBuilder(): Headers.Builder = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
        .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        .add("Accept-Language", "en-US,en;q=0.9")
        .add("Referer", "$baseUrl/")

    // ============================== Popular ===============================

    override fun popularAnimeRequest(page: Int): Request {
        return GET("$baseUrl/browse/videos?sort=views&page=$page", headers)
    }

    override fun popularAnimeParse(response: Response): AnimesPage {
        val page = extractInertiaData(response.body.string())
        val animeList = page.props.videos.map { item ->
            SAnime.create().apply {
                title = item.title
                thumbnail_url = fixUrl(item.poster ?: item.thumbnail)
                setUrlWithoutDomain("/videos/${item.slug}")
            }
        }
        val hasNextPage = page.props.meta.currentPage < page.props.meta.lastPage
        return AnimesPage(animeList, hasNextPage)
    }

    // =============================== Latest ===============================

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/browse/videos?sort=recent&page=$page", headers)
    }

    override fun latestUpdatesParse(response: Response): AnimesPage = popularAnimeParse(response)

    // =============================== Search ===============================

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        val urlBuilder = if (query.isNotBlank()) {
            "$baseUrl/search".toHttpUrl().newBuilder()
                .addQueryParameter("q", query)
        } else {
            "$baseUrl/browse/videos".toHttpUrl().newBuilder()
        }

        urlBuilder.addQueryParameter("page", page.toString())

        filters.forEach { filter ->
            when (filter) {
                is HanimeFilters.SortFilter -> {
                    urlBuilder.addQueryParameter("sort", filter.toParam())
                }
                is HanimeFilters.CensorshipFilter -> {
                    filter.toParam()?.let { urlBuilder.addQueryParameter("censored", it) }
                }
                is HanimeFilters.TagFilter -> {
                    filter.state.forEach { tagBox ->
                        if (tagBox.state) {
                            urlBuilder.addQueryParameter("tags[]", tagBox.slug)
                        }
                    }
                }
                else -> {}
            }
        }

        return GET(urlBuilder.build().toString(), headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage = popularAnimeParse(response)

    override fun getFilterList(): AnimeFilterList = HanimeFilters.getFilterList()

    // =========================== Anime Details ============================

    override fun animeDetailsParse(response: Response): SAnime {
        val data = extractInertiaData(response.body.string())
        val series = data.props.series
        val video = data.props.video ?: throw Exception("Failed to find video data on page")

        val brandName = series?.brand?.name ?: video.brand?.name

        return SAnime.create().apply {
            title = series?.title ?: video.title
            thumbnail_url = fixUrl(series?.cover ?: video.poster ?: video.thumbnail)
            author = brandName
            artist = brandName
            genre = series?.tags?.joinToString { it.name }
            status = when (series?.status?.lowercase()) {
                "completed" -> SAnime.COMPLETED
                "ongoing" -> SAnime.ONGOING
                else -> SAnime.UNKNOWN
            }
            description = buildDescription(series, video)
            initialized = true
        }
    }

    private fun buildDescription(series: SeriesDetails?, video: VideoDetails): String {
        val descParts = mutableListOf<String>()

        // 1. Synopsis / Plot
        val synopsis = (series?.synopsis ?: video.blurb ?: video.synopsis)?.trim()
        if (!synopsis.isNullOrBlank()) {
            descParts.add(synopsis)
        }

        // 2. Formatted Information & Metadata
        val metaLines = mutableListOf<String>()

        val brandName = series?.brand?.name ?: video.brand?.name
        if (!brandName.isNullOrBlank()) {
            metaLines.add("Studio / Brand: $brandName")
        }

        val year = series?.year
        if (year != null && year > 0) {
            metaLines.add("Release Year: $year")
        }

        val censored = series?.isCensored ?: video.isCensored
        if (censored != null) {
            metaLines.add("Censorship: ${if (censored) "Censored" else "Uncensored"}")
        }

        val rank = video.rank ?: series?.rank
        if (rank != null && rank > 0) {
            metaLines.add("Rank: #${formatNumber(rank)}")
        }

        val views = video.viewsHuman ?: series?.viewsHuman ?: video.views?.let { formatNumber(it) }
        if (!views.isNullOrBlank()) {
            metaLines.add("Views: $views")
        }

        val likes = video.likesCount
        val dislikes = video.dislikesCount
        if (likes != null && likes >= 0) {
            val totalVotes = likes + (dislikes ?: 0)
            val ratingStr = if (totalVotes > 0) {
                val pct = (likes * 100) / totalVotes
                val dislikeText = if (dislikes != null && dislikes > 0) " / 👎 ${formatNumber(dislikes)}" else ""
                "Rating: $pct% (👍 ${formatNumber(likes)}$dislikeText)"
            } else {
                "Likes: ${formatNumber(likes)}"
            }
            metaLines.add(ratingStr)
        }

        val downloads = video.downloadsCount
        if (downloads != null && downloads > 0) {
            metaLines.add("Downloads: ${formatNumber(downloads)}")
        }

        if (metaLines.isNotEmpty()) {
            descParts.add(metaLines.joinToString("\n"))
        }

        // 3. Alternate Names
        val altTitles = series?.altTitles?.filter {
            it.isNotBlank() && it != series.title && !it.startsWith("**")
        }
        if (!altTitles.isNullOrEmpty()) {
            descParts.add("Alternative Titles:\n" + altTitles.joinToString(", "))
        }

        return descParts.joinToString("\n\n")
    }

    // ============================== Episodes ==============================

    override fun episodeListParse(response: Response): List<SEpisode> {
        val data = extractInertiaData(response.body.string())
        val siblings = data.props.siblings
        val video = data.props.video ?: throw Exception("Failed to find video data on page")
        val now = System.currentTimeMillis()

        val episodeList = if (siblings.isNotEmpty()) {
            siblings.mapIndexed { index, item ->
                val epNum = item.number?.toFloat() ?: extractEpisodeNumber(item.title, item.slug, index + 1)
                val epNumStr = formatEpisodeNumber(epNum)
                val uploadDate = parseDate(item.publishedAt ?: item.releasedAt)
                val isFuture = !item.playable || (uploadDate > now)

                SEpisode.create().apply {
                    url = "/videos/${item.slug}"
                    episode_number = epNum
                    date_upload = uploadDate
                    if (isFuture) {
                        name = "Episode $epNumStr (Upcoming)"
                        scanlator = "Upcoming"
                    } else {
                        name = "Episode $epNumStr"
                        scanlator = null
                    }
                }
            }
        } else {
            val epNum = video.number?.toFloat() ?: extractEpisodeNumber(video.title, video.slug, 1)
            val epNumStr = formatEpisodeNumber(epNum)
            val uploadDate = parseDate(video.publishedAt ?: video.releasedAt)
            val isFuture = !video.playable || (uploadDate > now)

            listOf(
                SEpisode.create().apply {
                    url = "/videos/${video.slug}"
                    episode_number = epNum
                    date_upload = uploadDate
                    if (isFuture) {
                        name = "Episode $epNumStr (Upcoming)"
                        scanlator = "Upcoming"
                    } else {
                        name = "Episode $epNumStr"
                        scanlator = null
                    }
                },
            )
        }

        // Sort strictly newest to oldest (highest episode number / newest date at top)
        return episodeList.sortedWith(
            compareByDescending<SEpisode> { it.episode_number }
                .thenByDescending { it.date_upload },
        )
    }

    // ============================ Video Links =============================

    override fun videoListParse(response: Response): List<Video> {
        val data = extractInertiaData(response.body.string())
        val video = data.props.video ?: return emptyList()

        if (!video.playable) {
            val epNum = video.number?.toFloat() ?: extractEpisodeNumber(video.title, video.slug, 1)
            throw Exception("Episode ${formatEpisodeNumber(epNum)} is upcoming and has not been released yet.")
        }

        val sources = video.sources
        if (sources.isEmpty()) {
            val uploadDate = parseDate(video.publishedAt ?: video.releasedAt)
            if (uploadDate > System.currentTimeMillis()) {
                val epNum = video.number?.toFloat() ?: extractEpisodeNumber(video.title, video.slug, 1)
                throw Exception("Episode ${formatEpisodeNumber(epNum)} is upcoming and has not been released yet.")
            }
            return emptyList()
        }

        val videoList = mutableListOf<Video>()

        for (src in sources) {
            val srcUrl = src.url
            if (srcUrl.isBlank()) continue

            if (srcUrl.contains("vimbed.com", ignoreCase = true) || src.type.equals("iframe", ignoreCase = true)) {
                try {
                    val vimbedHeaders = headers.newBuilder()
                        .set("Referer", "$baseUrl/")
                        .build()

                    val vimbedResp = client.newCall(GET(srcUrl, vimbedHeaders)).execute()
                    val vimbedBody = vimbedResp.body.string()

                    val vimbedPage = extractVimbedData(vimbedBody)
                    val streamUrl = vimbedPage?.props?.video?.streamUrl

                    if (!streamUrl.isNullOrBlank()) {
                        val streamVideoHeaders = headers.newBuilder()
                            .set("Referer", srcUrl)
                            .set("Origin", "https://demo.vimbed.com")
                            .build()

                        val hlsVideos = playlistUtils.extractFromHls(
                            playlistUrl = streamUrl,
                            referer = srcUrl,
                            masterHeaders = streamVideoHeaders,
                            videoHeaders = streamVideoHeaders,
                            videoNameGen = { quality -> "Hanime - $quality" },
                        )

                        if (hlsVideos.isNotEmpty()) {
                            videoList.addAll(hlsVideos)
                        } else {
                            videoList.add(
                                Video(
                                    streamUrl,
                                    "Hanime - Auto",
                                    streamUrl,
                                    headers = streamVideoHeaders,
                                ),
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to continue other sources
                }
            } else if (srcUrl.endsWith(".m3u8")) {
                val hlsVideos = playlistUtils.extractFromHls(
                    playlistUrl = srcUrl,
                    referer = "$baseUrl/",
                    masterHeaders = headers,
                    videoHeaders = headers,
                    videoNameGen = { quality -> "Hanime - $quality" },
                )
                videoList.addAll(hlsVideos)
            } else if (srcUrl.endsWith(".mp4")) {
                val quality = src.quality ?: "Default"
                videoList.add(Video(srcUrl, "Hanime - $quality", srcUrl, headers = headers))
            }
        }

        val prefQuality = preferences.getString(PREF_QUALITY_KEY, PREF_QUALITY_DEFAULT)
        return videoList.sortedWith(
            compareByDescending { it.quality.contains(prefQuality.orEmpty()) },
        )
    }

    override fun videoUrlParse(response: Response): String = throw UnsupportedOperationException("Not used")

    // ============================== Helpers ===============================

    private fun extractEpisodeNumber(title: String, slug: String, fallback: Int): Float {
        EPISODE_NUM_TITLE_REGEX.find(title)?.groupValues?.get(1)?.toFloatOrNull()?.let { return it }
        EPISODE_NUM_SLUG_REGEX.find(slug)?.groupValues?.get(1)?.toFloatOrNull()?.let { return it }
        return fallback.toFloat()
    }

    private fun formatEpisodeNumber(num: Float): String {
        val intVal = num.toInt()
        return if (num == intVal.toFloat()) intVal.toString() else num.toString()
    }

    private fun formatNumber(num: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(num)
    }

    private fun extractInertiaData(html: String): InertiaPage {
        val match = DATA_PAGE_REGEX.find(html)
            ?: throw Exception("Could not find data-page attribute in HTML")
        val unescapedJson = Parser.unescapeEntities(match.groupValues[1], false)
        return json.decodeFromString(unescapedJson)
    }

    private fun extractVimbedData(html: String): VimbedInertia? {
        val match = DATA_PAGE_REGEX.find(html) ?: return null
        val unescapedJson = Parser.unescapeEntities(match.groupValues[1], false)
        return runCatching { json.decodeFromString<VimbedInertia>(unescapedJson) }.getOrNull()
    }

    private fun fixUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$baseUrl$url"
            else -> "$baseUrl/$url"
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return 0L
        return runCatching {
            val clean = dateStr.substringBefore("+").substringBefore("Z")
            dateFormat.parse(clean)?.time ?: 0L
        }.getOrDefault(0L)
    }

    // ============================ Preferences =============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        ListPreference(screen.context).apply {
            key = PREF_QUALITY_KEY
            title = "Preferred quality"
            entries = arrayOf("1080p", "720p", "480p")
            entryValues = arrayOf("1080p", "720p", "480p")
            setDefaultValue(PREF_QUALITY_DEFAULT)
            summary = "%s"
            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().putString(key, newValue as String).commit()
            }
        }.also(screen::addPreference)
    }

    companion object {
        private val DATA_PAGE_REGEX = Regex("""data-page\s*=\s*["']([^"']+)["']""")

        private val EPISODE_NUM_TITLE_REGEX = Regex("""(?:Episode|Ep\.?|#)?\s*(\d+)(?:\s*\(.*?\))?\s*$""", RegexOption.IGNORE_CASE)
        private val EPISODE_NUM_SLUG_REGEX = Regex("""-(\d+)$""")

        private const val PREF_QUALITY_KEY = "preferred_quality"
        private const val PREF_QUALITY_DEFAULT = "1080p"
    }
}
