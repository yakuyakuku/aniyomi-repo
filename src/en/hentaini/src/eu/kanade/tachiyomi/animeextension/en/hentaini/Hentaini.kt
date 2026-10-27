package eu.kanade.tachiyomi.animeextension.en.hentaini

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
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class Hentaini :
    AnimeHttpSource(),
    ConfigurableAnimeSource {

    override val name = "Hentaini"

    override val baseUrl = "https://hentaini.com"

    private val apiBaseUrl = "https://admin.hentaini.com/api"
    private val uploadsBaseUrl = "https://admin.hentaini.com/uploads"

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
        .add("Accept", "application/json, text/plain, */*")
        .add("Accept-Language", "en-US,en;q=0.9")
        .add("Referer", "$baseUrl/")
        .add("Origin", baseUrl)

    // ============================== Popular ===============================

    override fun popularAnimeRequest(page: Int): Request {
        return GET(
            "$apiBaseUrl/series?sort[0]=visits:desc&pagination[page]=$page&pagination[pageSize]=24&populate=*",
            headers,
        )
    }

    override fun popularAnimeParse(response: Response): AnimesPage {
        val parsed = json.decodeFromString<StrapiSeriesListResponse>(response.body.string())
        val animeList = parsed.data.map { serie ->
            SAnime.create().apply {
                title = serie.title
                thumbnail_url = fixImageUrl(serie.images)
                url = "/api/series?filters[url][\$eq]=${serie.url}&populate=*"
            }
        }
        val hasNextPage = parsed.meta.pagination.page < parsed.meta.pagination.pageCount
        return AnimesPage(animeList, hasNextPage)
    }

    // =============================== Latest ===============================

    override fun latestUpdatesRequest(page: Int): Request {
        return GET(
            "$apiBaseUrl/series?sort[0]=publishedAt:desc&pagination[page]=$page&pagination[pageSize]=24&populate=*",
            headers,
        )
    }

    override fun latestUpdatesParse(response: Response): AnimesPage = popularAnimeParse(response)

    // =============================== Search ===============================

    override fun searchAnimeRequest(page: Int, query: String, filters: AnimeFilterList): Request {
        val urlBuilder = "$apiBaseUrl/series".toHttpUrl().newBuilder()
            .addQueryParameter("pagination[page]", page.toString())
            .addQueryParameter("pagination[pageSize]", "24")
            .addQueryParameter("populate", "*")

        if (query.isNotBlank()) {
            urlBuilder.addQueryParameter("filters[title][\$containsi]", query)
        }

        var sortApplied = false

        filters.forEach { filter ->
            when (filter) {
                is HentainiFilters.SortFilter -> {
                    val param = filter.toParam()
                    val parts = param.split("=")
                    if (parts.size == 2) {
                        urlBuilder.addQueryParameter(parts[0], parts[1])
                        sortApplied = true
                    }
                }
                is HentainiFilters.CensorshipFilter -> {
                    filter.toParam()?.let {
                        urlBuilder.addQueryParameter("filters[censorship][\$eq]", it)
                    }
                }
                is HentainiFilters.GenreFilter -> {
                    filter.state.filter { it.state }.forEach { genreBox ->
                        urlBuilder.addQueryParameter("filters[genreList][name][\$eq]", genreBox.value)
                    }
                }
                else -> {}
            }
        }

        if (!sortApplied) {
            urlBuilder.addQueryParameter("sort[0]", "visits:desc")
        }

        return GET(urlBuilder.build().toString(), headers)
    }

    override fun searchAnimeParse(response: Response): AnimesPage = popularAnimeParse(response)

    override fun getFilterList(): AnimeFilterList = HentainiFilters.getFilterList()

    // =========================== Anime Details ============================

    override fun animeDetailsRequest(anime: SAnime): Request {
        val targetUrl = when {
            anime.url.startsWith("http://") || anime.url.startsWith("https://") -> anime.url
            anime.url.startsWith("/api/") -> "https://admin.hentaini.com${anime.url}"
            anime.url.startsWith("/h/") -> {
                val slug = anime.url.substringAfter("/h/").substringBefore("/")
                "$apiBaseUrl/series?filters[url][\$eq]=$slug&populate=*"
            }
            else -> "https://admin.hentaini.com${anime.url}"
        }
        return GET(targetUrl, headers)
    }

    override fun animeDetailsParse(response: Response): SAnime {
        val parsed = json.decodeFromString<StrapiSeriesListResponse>(response.body.string())
        val serie = parsed.data.firstOrNull() ?: throw Exception("Series not found")

        val brandName = serie.studio?.name ?: serie.producer?.name

        return SAnime.create().apply {
            title = serie.title
            thumbnail_url = fixImageUrl(serie.images)
            author = brandName
            artist = brandName
            val genresList = serie.genreList
            genre = genresList.joinToString { it.name }
            status = when (serie.status?.name?.lowercase()) {
                "airing" -> SAnime.ONGOING
                "finalized", "finished airing", "completed" -> SAnime.COMPLETED
                else -> SAnime.UNKNOWN
            }
            description = buildDescription(serie)
            initialized = true
        }
    }

    private fun buildDescription(serie: StrapiSeries): String {
        val descParts = mutableListOf<String>()

        // 1. Synopsis
        val synopsis = serie.synopsis?.trim()
        if (!synopsis.isNullOrBlank()) {
            descParts.add(synopsis)
        }

        // 2. Metadata lines
        val metaLines = mutableListOf<String>()

        val studioName = serie.studio?.name
        if (!studioName.isNullOrBlank()) {
            metaLines.add("Studio: $studioName")
        }

        val producerName = serie.producer?.name
        if (!producerName.isNullOrBlank()) {
            metaLines.add("Producer: $producerName")
        }

        val statusName = serie.status?.name
        if (!statusName.isNullOrBlank()) {
            metaLines.add("Status: $statusName")
        }

        val censorship = serie.censorship
        if (censorship != null) {
            metaLines.add("Censorship: ${if (censorship) "Censored" else "Uncensored"}")
        }

        val visits = serie.visits
        if (visits != null && visits > 0) {
            metaLines.add("Views: ${formatNumber(visits)}")
        }

        val epCount = serie.episodes.size
        if (epCount > 0) {
            metaLines.add("Episodes: $epCount")
        }

        if (metaLines.isNotEmpty()) {
            descParts.add(metaLines.joinToString("\n"))
        }

        // 3. Alternate English Title
        val altTitle = serie.titleEnglish?.trim()
        if (!altTitle.isNullOrBlank() && altTitle != serie.title) {
            descParts.add("Alternative Title:\n$altTitle")
        }

        return descParts.joinToString("\n\n")
    }

    // ============================== Episodes ==============================

    override fun episodeListRequest(anime: SAnime): Request = animeDetailsRequest(anime)

    override fun episodeListParse(response: Response): List<SEpisode> {
        val parsed = json.decodeFromString<StrapiSeriesListResponse>(response.body.string())
        val serie = parsed.data.firstOrNull() ?: throw Exception("Series not found")
        val episodes = serie.episodes
        val now = System.currentTimeMillis()

        val episodeList = episodes.mapIndexed { index, ep ->
            val epNum = ep.episodeNumber ?: (index + 1).toFloat()
            val epNumStr = formatEpisodeNumber(epNum)
            val uploadDate = parseDate(ep.publishedAt ?: ep.createdAt)
            val hasPlayers = !ep.players.isNullOrBlank() && ep.players != "[]"
            val isFuture = !hasPlayers || (uploadDate > now)

            SEpisode.create().apply {
                url = "/api/episodes/${ep.id}"
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

        // Sort strictly newest to oldest (highest episode number / newest date at top)
        return episodeList.sortedWith(
            compareByDescending<SEpisode> { it.episode_number }
                .thenByDescending { it.date_upload },
        )
    }

    // ============================ Video Links =============================

    override fun videoListRequest(episode: SEpisode): Request {
        val targetUrl = when {
            episode.url.startsWith("http://") || episode.url.startsWith("https://") -> episode.url
            episode.url.startsWith("/api/") -> "https://admin.hentaini.com${episode.url}"
            else -> "$apiBaseUrl${episode.url}"
        }
        return GET(targetUrl, headers)
    }

    override fun videoListParse(response: Response): List<Video> {
        val parsed = json.decodeFromString<StrapiSingleEpisodeResponse>(response.body.string())
        val episode = parsed.data ?: return emptyList()

        if (episode.players.isNullOrBlank() || episode.players == "[]") {
            val epNumStr = formatEpisodeNumber(episode.episodeNumber ?: 1f)
            throw Exception("Episode $epNumStr is upcoming and has not been released yet.")
        }

        val players = runCatching {
            json.decodeFromString<List<PlayerEntry>>(episode.players)
        }.getOrDefault(emptyList())

        val videoList = mutableListOf<Video>()

        for (player in players) {
            val playerUrl = player.url
            if (playerUrl.isBlank()) continue

            when {
                player.name.equals("BR", ignoreCase = true) || playerUrl.contains("burstcloud.co") -> {
                    extractBurstCloud(playerUrl)?.let(videoList::add)
                }
                player.name.equals("Yourupload", ignoreCase = true) || player.name.equals("Y", ignoreCase = true) || playerUrl.contains("yourupload.com") -> {
                    extractYourUpload(playerUrl)?.let(videoList::add)
                }
                player.name.equals("HLS", ignoreCase = true) || playerUrl.contains(".m3u8") -> {
                    val streamHeaders = Headers.Builder()
                        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                        .add("Referer", "$baseUrl/")
                        .add("Origin", baseUrl)
                        .build()

                    try {
                        val hlsVideos = playlistUtils.extractFromHls(
                            playlistUrl = playerUrl,
                            referer = "$baseUrl/",
                            masterHeaders = streamHeaders,
                            videoHeaders = streamHeaders,
                            videoNameGen = { quality -> "Hentaini - $quality (HLS)" },
                        )
                        if (hlsVideos.isNotEmpty()) {
                            videoList.addAll(hlsVideos)
                        } else {
                            videoList.add(
                                Video(
                                    playerUrl,
                                    "Hentaini - HLS (Stream)",
                                    playerUrl,
                                    headers = streamHeaders,
                                ),
                            )
                        }
                    } catch (e: Exception) {
                        videoList.add(
                            Video(
                                playerUrl,
                                "Hentaini - HLS (Stream)",
                                playerUrl,
                                headers = streamHeaders,
                            ),
                        )
                    }
                }
            }
        }

        val prefQuality = preferences.getString(PREF_QUALITY_KEY, PREF_QUALITY_DEFAULT).orEmpty()
        return videoList.sortedWith(
            compareByDescending<Video> { !it.quality.contains("(HLS)") } // Prefer direct MP4 for downloading & seeking
                .thenByDescending { it.quality.contains(prefQuality, ignoreCase = true) },
        )
    }

    private fun extractBurstCloud(url: String): Video? {
        return runCatching {
            val embedHeaders = Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .add("Referer", "https://www.burstcloud.co/")
                .build()

            val resp = client.newCall(GET(url, embedHeaders)).execute()
            val html = resp.body.string()
            val fileId = DATA_FILE_ID_REGEX.find(html)?.groupValues?.get(1) ?: return null

            val formBody = okhttp3.FormBody.Builder()
                .add("fileId", fileId)
                .build()

            val reqHeaders = Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .add("Referer", url)
                .add("X-Requested-With", "XMLHttpRequest")
                .build()

            val playRequest = okhttp3.Request.Builder()
                .url("https://www.burstcloud.co/file/play-request/")
                .headers(reqHeaders)
                .post(formBody)
                .build()

            val playResp = client.newCall(playRequest).execute()
            val playBody = playResp.body.string()
            val dto = json.decodeFromString<BurstCloudDto>(playBody)
            val cdnUrl = dto.purchase.cdnUrl
            if (cdnUrl.isBlank()) return null

            val directHeaders = Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .add("Referer", "https://www.burstcloud.co/")
                .build()

            Video(cdnUrl, "Hentaini - BurstCloud (MP4)", cdnUrl, headers = directHeaders)
        }.getOrNull()
    }

    private fun extractYourUpload(url: String): Video? {
        return runCatching {
            val reqHeaders = Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .add("Referer", "https://www.yourupload.com/")
                .build()

            val resp = client.newCall(GET(url, reqHeaders)).execute()
            val html = resp.body.string()
            val videoUrl = YOURUPLOAD_REGEX.find(html)?.groupValues?.get(1) ?: return null

            Video(videoUrl, "Hentaini - YourUpload (MP4)", videoUrl, headers = reqHeaders)
        }.getOrNull()
    }

    override fun videoUrlParse(response: Response): String = response.request.url.toString()

    // ============================== Helpers ===============================

    private fun fixImageUrl(images: List<StrapiImage>): String {
        val cover = images.firstOrNull { it.imageType?.name == "cover" } ?: images.firstOrNull()
        val path = cover?.path ?: return ""
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("/") -> "https://admin.hentaini.com$path"
            else -> "$uploadsBaseUrl/$path"
        }
    }

    private fun formatEpisodeNumber(num: Float): String {
        val intVal = num.toInt()
        return if (num == intVal.toFloat()) intVal.toString() else num.toString()
    }

    private fun formatNumber(num: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(num)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return 0L
        return runCatching {
            val clean = dateStr.substringBefore("+").substringBefore("Z").substringBefore(".")
            dateFormat.parse(clean)?.time ?: 0L
        }.getOrDefault(0L)
    }

    // ============================ Preferences =============================

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        ListPreference(screen.context).apply {
            key = PREF_QUALITY_KEY
            title = "Preferred quality"
            entries = arrayOf("MP4", "BurstCloud", "YourUpload", "HLS")
            entryValues = arrayOf("MP4", "BurstCloud", "YourUpload", "HLS")
            setDefaultValue(PREF_QUALITY_DEFAULT)
            summary = "%s"
            setOnPreferenceChangeListener { _, newValue ->
                preferences.edit().putString(key, newValue as String).commit()
            }
        }.also(screen::addPreference)
    }

    companion object {
        private val DATA_FILE_ID_REGEX = Regex("""data-file-id=["']([^"']+)["']""")
        private val YOURUPLOAD_REGEX = Regex("""file:\s*["'](https?://[^"']+)["']""")

        private const val PREF_QUALITY_KEY = "preferred_quality"
        private const val PREF_QUALITY_DEFAULT = "MP4"
    }
}
