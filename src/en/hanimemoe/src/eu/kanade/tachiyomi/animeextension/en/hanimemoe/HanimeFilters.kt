package eu.kanade.tachiyomi.animeextension.en.hanimemoe

import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList

object HanimeFilters {

    class SortFilter : AnimeFilter.Select<String>(
        "Sort By",
        arrayOf(
            "Recent uploads",
            "Newest releases",
            "Most views",
            "Most likes",
            "Trending",
            "A – Z",
        ),
    ) {
        fun toParam(): String = when (state) {
            0 -> "recent"
            1 -> "released"
            2 -> "views"
            3 -> "likes"
            4 -> "trending"
            5 -> "alpha"
            else -> "recent"
        }
    }

    class CensorshipFilter : AnimeFilter.Select<String>(
        "Censorship",
        arrayOf("All", "Censored", "Uncensored"),
    ) {
        fun toParam(): String? = when (state) {
            1 -> "1"
            2 -> "0"
            else -> null
        }
    }

    class TagCheckBox(name: String, val slug: String) : AnimeFilter.CheckBox(name)

    class TagFilter : AnimeFilter.Group<TagCheckBox>(
        "Tags",
        tagsList.map { TagCheckBox(it.first, it.second) },
    )

    private val tagsList = listOf(
        Pair("2000-Year-Old Dragon Girl", "2000-year-old-dragon-girl"),
        Pair("3D", "3d"),
        Pair("Ahegao", "ahegao"),
        Pair("Anal", "anal"),
        Pair("BBW", "bbw"),
        Pair("BDSM", "bdsm"),
        Pair("Big Boobs", "big-boobs"),
        Pair("Blow Job", "blow-job"),
        Pair("Bondage", "bondage"),
        Pair("Boob Job", "boob-job"),
        Pair("Bukakke", "bukakke"),
        Pair("Busty", "busty"),
        Pair("Censored", "censored"),
        Pair("Cheating", "cheating"),
        Pair("Chikan", "chikan"),
        Pair("Cosplay", "cosplay"),
        Pair("Creampie", "creampie"),
        Pair("Dark Skin", "dark-skin"),
        Pair("Defloration", "defloration"),
        Pair("Demon", "demon"),
        Pair("Dilf", "dilf"),
        Pair("Double Penetration", "double-penetration"),
        Pair("Ecchi", "ecchi"),
        Pair("Elf", "elf"),
        Pair("Facial", "facial"),
        Pair("Fantasy", "fantasy"),
        Pair("Femdom", "femdom"),
        Pair("Foot Job", "foot-job"),
        Pair("Futanari", "futanari"),
        Pair("Gangbang", "gangbang"),
        Pair("Glasses", "glasses"),
        Pair("Hand Job", "hand-job"),
        Pair("Harem", "harem"),
        Pair("HD", "hd"),
        Pair("Incest", "incest"),
        Pair("Inflation", "inflation"),
        Pair("Kemonomimi", "kemonomimi"),
        Pair("Lactation", "lactation"),
        Pair("Loli", "loli"),
        Pair("Maid", "maid"),
        Pair("Masturbation", "masturbation"),
        Pair("Milf", "milf"),
        Pair("Mind Break", "mind-break"),
        Pair("Mind Control", "mind-control"),
        Pair("Monster", "monster"),
        Pair("Monster Girl", "monster-girl"),
        Pair("Nekomimi", "nekomimi"),
        Pair("Netorare", "netorare"),
        Pair("Nurse", "nurse"),
        Pair("Older Man Younger Woman", "older-man-younger-woman"),
        Pair("Older Woman Younger Man", "older-woman-younger-man"),
        Pair("Oral", "oral"),
        Pair("Orgy", "orgy"),
        Pair("Petite", "petite"),
        Pair("Plot", "plot"),
        Pair("POV", "pov"),
        Pair("Pregnant", "pregnant"),
        Pair("Public Sex", "public-sex"),
        Pair("Rape", "rape"),
        Pair("Rimjob", "rimjob"),
        Pair("Scat", "scat"),
        Pair("School Girl", "school-girl"),
        Pair("Sex Toys", "sex-toys"),
        Pair("Shota", "shota"),
        Pair("Small Boobs", "small-boobs"),
        Pair("Stockings", "stockings"),
        Pair("Succubus", "succubus"),
        Pair("Swimsuit", "swimsuit"),
        Pair("Teacher", "teacher"),
        Pair("Tentacles", "tentacles"),
        Pair("Threesome", "threesome"),
        Pair("Uncensored", "uncensored"),
        Pair("Vanilla", "vanilla"),
        Pair("Virgin", "virgin"),
        Pair("X-Ray", "x-ray"),
        Pair("Yaoi", "yaoi"),
        Pair("Yuri", "yuri"),
    )

    fun getFilterList(): AnimeFilterList = AnimeFilterList(
        SortFilter(),
        CensorshipFilter(),
        TagFilter(),
    )
}
