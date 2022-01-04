package com.lagradost.cloudstream3.providersjav

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup

class JavTubeWatch : MainAPI() {
    override val name: String get() = "JavTube"
    override val mainUrl: String get() = "https://javtube.watch"
    override val supportedTypes: Set<TvType> get() = setOf(TvType.JAV)
    override val hasDownloadSupport: Boolean get() = false
    override val hasMainPage: Boolean get() = true
    override val hasQuickSearch: Boolean get() = false

    override fun getMainPage(): HomePageResponse {
        val document = app.get(mainUrl).document
        val all = ArrayList<HomePageList>()

        // Fetch row title
        val title = "Latest videos"
        // Fetch list of items and map
        val inner = document.selectFirst("div.videos-list")?.select("article")
        //Log.i(this.name, "Inner => $inner")
        if (!inner.isNullOrEmpty()) {
            val elements: List<SearchResponse> = inner.mapNotNull {

                //Log.i(this.name, "Inner content => $innerArticle")
                val aa = it.select("a")?.last()
                val link = fixUrlNull(aa?.attr("href")) ?: return@mapNotNull null

                val imgArticle = aa?.select("img")
                val name = imgArticle?.attr("alt") ?: ""
                var image = imgArticle?.attr("data-src")
                if (image.isNullOrEmpty()) {
                    image = imgArticle?.attr("src")
                }

                MovieSearchResponse(
                    name = name,
                    url = link,
                    this.name,
                    TvType.JAV,
                    image,
                    year = null,
                    id = null,
                )
            }

            all.add(
                HomePageList(
                    title, elements
                )
            )
        }
        return HomePageResponse(all)
    }

    override fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/$query"
        val document = app.get(url).document
            .select("article#post")

        return document?.mapNotNull {
            val innerA = it?.selectFirst("a") ?: return@mapNotNull null
            val linkUrl = innerA.attr("href") ?: return@mapNotNull null
            val title = innerA.select("header.entry-header")?.text() ?: ""
            val imgLink = innerA.select("img")
            var image = imgLink?.attr("data-src")
            if (image.isNullOrEmpty()) {
                image = imgLink?.attr("src")
            }
            val year = null

            MovieSearchResponse(
                title,
                linkUrl,
                this.name,
                TvType.JAV,
                image,
                year
            )
        } ?: listOf()
    }

    override fun load(url: String): LoadResponse {
        val document = app.get(url).document
        //Log.i(this.name, "Result => ${body}")

        // Video details
        val content = document.selectFirst("article#post")
            ?.select("div.video-player")

        val title = content?.select("[meta itemprop=\"name\"]")?.attr("content") ?: ""
        val descript =content?.select("[meta itemprop=\"description\"]")?.attr("content")
        Log.i(this.name, "Result => (descript) $descript")
        val year = null

        // Poster Image
        val poster = content?.select("[meta itemprop=\"thumbnailUrl\"]")?.attr("content")
        Log.i(this.name, "Result => (poster) $poster")

        //TODO: Fetch links
        // Video stream
        val streamUrl: String =  try {
            val streamdataStart = content?.toString()?.indexOf("var torotube_Public = {") ?: 0
            val streamdata = content?.toString()?.substring(streamdataStart) ?: ""
            //Log.i(this.name, "Result => (streamdata) ${streamdata}")
            streamdata.substring(0, streamdata.indexOf("};"))
        } catch (e: Exception) {
            Log.i(this.name, "Result => Exception (load) $e")
            ""
        }
        return MovieLoadResponse(title, url, this.name, TvType.JAV, streamUrl, poster, year, descript, null, null)
    }

    //TODO: LoadLinks
    override fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (data == "about:blank") return false
        if (data.isEmpty()) return false

        var streamdata = data.substring(data.indexOf("player"))
        streamdata = streamdata.substring(streamdata.indexOf("["))
        streamdata = streamdata.substring(1, streamdata.indexOf("]"))
            .replace("\\\"", "\"")
            .replace("\",\"", "")
            .replace("\\", "")
        //Log.i(this.name, "Result => (streamdata) ${streamdata}")

        // Get all src from iframes
        val streambody = Jsoup.parse(streamdata)?.select("iframe")
            ?.filter { s -> s.hasAttr("src") }
            ?.map { a -> a?.attr("src") ?: "" }
        //Log.i(this.name, "Result => (streambody) ${streambody.toString()}")
        if (!streambody.isNullOrEmpty()) {
            streambody.forEach { link ->
                Log.i(this.name, "Result => (link) $link")
                loadExtractor(link, link, callback)
            }
            return true
        }
        return false
    }
}