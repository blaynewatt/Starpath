package net.syserr.starpathtracker.data.scraper

import net.syserr.starpathtracker.data.model.StarPathDuty
import net.syserr.starpathtracker.data.model.StarPathList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.Locale
import java.util.UUID

object StarPathScraper {

    const val STAR_PATH_HUB_URL = "https://www.ign.com/wikis/disney-dreamlight-valley/Star_Path"

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun scrapeStarPathIndex(hubUrl: String = STAR_PATH_HUB_URL): Result<List<net.syserr.starpathtracker.data.model.StarPathEntry>> =
        withContext(Dispatchers.IO) {
            try {
                val doc: Document = Jsoup.connect(hubUrl)
                    .userAgent(USER_AGENT)
                    .referrer("https://www.google.com/")
                    .timeout(20000)
                    .get()

                val links = doc.select("a[href]")
                val entries = mutableListOf<net.syserr.starpathtracker.data.model.StarPathEntry>()
                val seenUrls = mutableSetOf<String>()

                for (link in links) {
                    val rawText = link.text().trim()
                    val href = link.attr("abs:href").trim()

                    if (rawText.contains("Star Path Duties", ignoreCase = true) || href.contains("Star_Path_Duties", ignoreCase = true)) {
                        if (href.isNotEmpty() && seenUrls.add(href)) {
                            val displayTitle = if (rawText.isNotEmpty()) cleanTitle(rawText) else cleanTitle(href.substringAfterLast("/").replace("_", " "))
                            entries.add(net.syserr.starpathtracker.data.model.StarPathEntry(title = displayTitle, url = href))
                        }
                    }
                }

                if (entries.isEmpty()) {
                    Result.failure(IllegalStateException("No Star Path Duty links found on hub page"))
                } else {
                    Result.success(entries)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun scrapeUrl(url: String): Result<StarPathList> = withContext(Dispatchers.IO) {
        try {
            val doc: Document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com/")
                .timeout(20000)
                .get()

            val title = extractTitle(doc)
            val duties = parseDuties(doc, url)

            if (duties.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Could not find any Star Path duties on this page. Please ensure this is a Disney Dreamlight Valley Star Path guide.")
                )
            }

            val listId = generateListId(url)
            Result.success(
                StarPathList(
                    id = listId,
                    title = title,
                    sourceUrl = url,
                    duties = duties,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractTitle(doc: Document): String {
        // Try h1 first
        val h1 = doc.selectFirst("h1")?.text()?.trim()
        if (!h1.isNullOrEmpty()) {
            return cleanTitle(h1)
        }

        // Try title tag
        val titleTag = doc.title().trim()
        if (titleTag.isNotEmpty()) {
            return cleanTitle(titleTag)
        }

        return "Star Path Duties"
    }

    private fun cleanTitle(raw: String): String {
        return raw.replace(Regex("(?i) - Disney Dreamlight Valley.*"), "")
            .replace(Regex("(?i)Disney Dreamlight Valley:?\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun parseDuties(doc: Document, sourceUrl: String): List<StarPathDuty> {
        val duties = mutableListOf<StarPathDuty>()
        val tables = doc.select("table")

        if (tables.isNotEmpty()) {
            var dutyIndex = 1
            for (table in tables) {
                val sectionName = findSectionName(table)
                val rows = table.select("tr")
                if (rows.isEmpty()) continue

                // Check header row
                val headerCells = rows.first()?.select("th, td")?.map { it.text().lowercase(Locale.ROOT).trim() } ?: emptyList()

                var taskCol = 0
                var howCol = 1
                var reqCol = 2
                var rewardCol = 3

                var hasHeaders = false
                headerCells.forEachIndexed { idx, text ->
                    when {
                        text.contains("task") || text.contains("duty") -> {
                            taskCol = idx
                            hasHeaders = true
                        }
                        text.contains("how") || text.contains("complete") || text.contains("description") -> {
                            howCol = idx
                            hasHeaders = true
                        }
                        text.contains("req") -> {
                            reqCol = idx
                            hasHeaders = true
                        }
                        text.contains("reward") || text.contains("token") -> {
                            rewardCol = idx
                            hasHeaders = true
                        }
                    }
                }

                val dataRows = if (hasHeaders) rows.drop(1) else rows

                for (row in dataRows) {
                    val cells = row.select("td, th")
                    if (cells.size < 2) continue

                    fun getColText(col: Int): String {
                        return if (col < cells.size) cells[col].text().trim() else ""
                    }

                    val taskTitle = getColText(taskCol)
                    val howTo = getColText(howCol)
                    val requirement = getColText(reqCol)
                    val rewardStr = getColText(rewardCol)

                    if (taskTitle.isBlank() || taskTitle.equals("task", ignoreCase = true)) {
                        continue
                    }

                    val tokenReward = extractNumber(rewardStr)

                    val dutyId = "duty_${dutyIndex++}_${Math.abs(taskTitle.hashCode())}"
                    duties.add(
                        StarPathDuty(
                            id = dutyId,
                            title = taskTitle,
                            howToComplete = howTo,
                            requirement = requirement,
                            tokenReward = tokenReward,
                            section = sectionName,
                            isCompleted = false
                        )
                    )
                }
            }
        }

        // If no table duties found, attempt list parsing as a fallback
        if (duties.isEmpty()) {
            val listItems = doc.select("article li, .wiki-content li, main li")
            var idx = 1
            for (li in listItems) {
                val text = li.text().trim()
                if (text.length > 5 && (text.contains("–") || text.contains("-") || text.contains(":"))) {
                    val parts = text.split(Regex("[-–:]"), limit = 2)
                    val title = parts[0].trim()
                    val desc = if (parts.size > 1) parts[1].trim() else ""
                    duties.add(
                        StarPathDuty(
                            id = "duty_list_${idx++}",
                            title = title,
                            howToComplete = desc,
                            requirement = "",
                            tokenReward = 0,
                            section = "Duties",
                            isCompleted = false
                        )
                    )
                }
            }
        }

        return duties
    }

    private fun findSectionName(table: Element): String {
        // Look backwards among preceding siblings
        var prev = table.previousElementSibling()
        var hops = 0
        while (prev != null && hops < 10) {
            val tag = prev.tagName().lowercase(Locale.ROOT)
            if (tag == "h2" || tag == "h3" || tag == "h4") {
                val headingText = prev.text().trim()
                if (headingText.isNotEmpty()) {
                    return headingText
                }
            }
            // Check if there is an h2 or h3 inside prev container
            val innerHeading = prev.selectFirst("h2, h3, h4")
            if (innerHeading != null && innerHeading.text().trim().isNotEmpty()) {
                return innerHeading.text().trim()
            }
            prev = prev.previousElementSibling()
            hops++
        }

        // Look in parent element's preceding elements
        val parent = table.parent()
        if (parent != null) {
            var parentPrev = parent.previousElementSibling()
            var parentHops = 0
            while (parentPrev != null && parentHops < 5) {
                val heading = if (parentPrev.tagName().matches(Regex("h[234]"))) parentPrev else parentPrev.selectFirst("h2, h3, h4")
                if (heading != null && heading.text().trim().isNotEmpty()) {
                    return heading.text().trim()
                }
                parentPrev = parentPrev.previousElementSibling()
                parentHops++
            }
        }

        return "Main Duties"
    }

    private fun extractNumber(str: String): Int {
        val match = Regex("\\d+").find(str)
        return match?.value?.toIntOrNull() ?: 0
    }

    fun generateListId(url: String): String {
        val slug = url.substringAfterLast("/").lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]"), "_")
        return if (slug.isNotEmpty()) slug else UUID.randomUUID().toString()
    }
}
