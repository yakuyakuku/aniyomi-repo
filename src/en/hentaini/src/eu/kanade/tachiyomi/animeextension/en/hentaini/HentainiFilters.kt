package eu.kanade.tachiyomi.animeextension.en.hentaini

import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList

object HentainiFilters {

    class SortFilter : AnimeFilter.Select<String>(
        "Sort By",
        arrayOf(
            "Most views",
            "Newest added",
            "Recently updated",
            "A – Z",
            "Z – A",
        ),
    ) {
        fun toParam(): String = when (state) {
            0 -> "sort[0]=visits:desc"
            1 -> "sort[0]=publishedAt:desc"
            2 -> "sort[0]=updatedAt:desc"
            3 -> "sort[0]=title:asc"
            4 -> "sort[0]=title:desc"
            else -> "sort[0]=visits:desc"
        }
    }

    class CensorshipFilter : AnimeFilter.Select<String>(
        "Censorship",
        arrayOf("All", "Censored", "Uncensored"),
    ) {
        fun toParam(): String? = when (state) {
            1 -> "true"
            2 -> "false"
            else -> null
        }
    }

    class GenreCheckBox(name: String, val value: String) : AnimeFilter.CheckBox(name)

    class GenreFilter : AnimeFilter.Group<GenreCheckBox>(
        "Genres",
        genresList.map { GenreCheckBox(it.first, it.second) },
    )

    private val genresList = listOf(
        Pair("Ahegao", "Ahegao"),
        Pair("Anal", "Anal"),
        Pair("BBW", "BBW"),
        Pair("Big Breast", "Big Breast"),
        Pair("Blackmail", "Blackmail"),
        Pair("Bondage", "Bondage"),
        Pair("Chikan", "Chikan"),
        Pair("Cosplay", "Cosplay"),
        Pair("Futanari", "Futanari"),
        Pair("Garyu", "Garyu"),
        Pair("Harem", "Harem"),
        Pair("Incest", "Incest"),
        Pair("Maid", "Maid"),
        Pair("Milf", "Milf"),
        Pair("Netorare", "Netorare"),
        Pair("Nurse", "Nurse"),
        Pair("Orgy", "Orgy"),
        Pair("Oyakodon", "Oyakodon"),
        Pair("Paizuri", "Paizuri"),
        Pair("Rape", "Rape"),
        Pair("Scholar", "Scholar"),
        Pair("Shotacon", "Shotacon"),
        Pair("Small Breasts", "Small Breasts"),
        Pair("Succubus", "Succubus"),
        Pair("Tentacles", "Tentacles"),
        Pair("Vanilla", "Vanilla"),
        Pair("Virgin", "Virgin"),
        Pair("Yaoi", "Yaoi"),
        Pair("Yuri", "Yuri"),
    )

    fun getFilterList(): AnimeFilterList = AnimeFilterList(
        SortFilter(),
        CensorshipFilter(),
        GenreFilter(),
    )
}
