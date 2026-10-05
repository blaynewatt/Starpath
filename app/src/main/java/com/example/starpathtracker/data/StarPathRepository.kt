package com.example.starpathtracker.data

import android.content.Context
import com.example.starpathtracker.data.model.StarPathDuty
import com.example.starpathtracker.data.model.StarPathEntry
import com.example.starpathtracker.data.model.StarPathList
import com.example.starpathtracker.data.model.StarPathPreset
import com.example.starpathtracker.data.scraper.StarPathScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class StarPathRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val prefs = context.getSharedPreferences("star_path_tracker_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LAST_URL = "key_last_active_url"
        const val KEY_ENTRIES_CACHE = "key_cached_star_paths"

        val DEFAULT_ENTRIES = listOf(
            StarPathEntry("Astronomer's Journey", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Astronomer%27s_Journey_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Haunted Holiday", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Haunted_Holiday_Star_Path_Duties"),
            StarPathEntry("Lovely Monsters", "https://www.ign.com/wikis/disney-dreamlight-valley/Lovely_Monsters_Star_Path_Duties"),
            StarPathEntry("A Day At Disney", "https://www.ign.com/wikis/disney-dreamlight-valley/All_A_Day_At_Disney_Star_Path_Duties"),
            StarPathEntry("Majesty and Magnolias", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Majesty_and_Magnolias_Star_Path_Duties"),
            StarPathEntry("Dapper Delights", "https://www.ign.com/wikis/disney-dreamlight-valley/Dapper_Delights_Star_Path_Duties"),
            StarPathEntry("Night Show", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Night_Show_Star_Path_Duties"),
            StarPathEntry("Frost and Fairies", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Frost_and_Fairies_Star_Path_Duties"),
            StarPathEntry("Oasis Retreat", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Oasis_Retreat_Star_Path_Duties"),
            StarPathEntry("Garden of Whimsy", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Garden_of_Whimsy_Star_Path_Duties"),
            StarPathEntry("Adventures in Never Land", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Adventures_in_Never_Land_Star_Path_Duties"),
            StarPathEntry("Retro Roadtrip", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Retro_Roadtrip_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Witchful Thinking", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Witchful_Thinking_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Winter Warmth", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Winter_Warmth_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Paw-fect Romance", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Paw-fect_Romance_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Elements of Nature", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Elements_of_Nature_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Godly Glamor", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Godly_Glamor_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Pop City", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Pop_City_Star_Path_Duties_and_Routine_Duties"),
            StarPathEntry("Haunting Elegance", "https://www.ign.com/wikis/disney-dreamlight-valley/All_Haunting_Elegance_Star_Path_Duties_and_Routine_Duties")
        )

        val DEFAULT_URL = DEFAULT_ENTRIES.last().url

        val PRESETS = listOf(
            StarPathPreset(
                id = "haunting_elegance",
                title = "Haunting Elegance Star Path",
                url = DEFAULT_URL,
                description = "Season of Scares update (Oogie Boogie, Jack Skellington)",
                assetFileName = "haunting_elegance.json"
            ),
            StarPathPreset(
                id = "godly_glamor",
                title = "Godly Glamor Star Path",
                url = "https://www.ign.com/wikis/disney-dreamlight-valley/All_Godly_Glamor_Star_Path_Duties_and_Routine_Duties",
                description = "Olympus Fashion & Mediterranean Specialties",
                assetFileName = "godly_glamor.json"
            ),
            StarPathPreset(
                id = "pop_city",
                title = "Pop City Star Path",
                url = "https://www.ign.com/wikis/disney-dreamlight-valley/All_Pop_City_Star_Path_Duties_and_Routine_Duties",
                description = "Candy Rush & Vanellope Neon City",
                assetFileName = "pop_city.json"
            )
        )
    }

    private val _starPathEntries = MutableStateFlow<List<StarPathEntry>>(DEFAULT_ENTRIES)
    val starPathEntries: StateFlow<List<StarPathEntry>> = _starPathEntries.asStateFlow()

    private val _currentStarPath = MutableStateFlow<StarPathList?>(null)
    val currentStarPath: StateFlow<StarPathList?> = _currentStarPath.asStateFlow()

    fun getActiveUrl(): String {
        return prefs.getString(KEY_LAST_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun getListIdForUrl(url: String): String {
        val preset = PRESETS.find { it.url.equals(url.trim(), ignoreCase = true) }
        return preset?.id ?: StarPathScraper.generateListId(url.trim())
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        // Load cached index if present
        loadCachedEntries()

        val lastUrl = getActiveUrl()
        loadStarPath(lastUrl, forceScrape = false)

        // Asynchronously refresh the list of Star Paths from IGN hub
        refreshStarPathIndex()
    }

    private fun loadCachedEntries() {
        val cached = prefs.getString(KEY_ENTRIES_CACHE, null)
        if (!cached.isNullOrEmpty()) {
            try {
                val parsed = json.decodeFromString<List<StarPathEntry>>(cached)
                if (parsed.isNotEmpty()) {
                    _starPathEntries.value = parsed
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun refreshStarPathIndex(): Result<List<StarPathEntry>> = withContext(Dispatchers.IO) {
        val result = StarPathScraper.scrapeStarPathIndex()
        if (result.isSuccess) {
            val list = result.getOrThrow()
            if (list.isNotEmpty()) {
                _starPathEntries.value = list
                prefs.edit().putString(KEY_ENTRIES_CACHE, json.encodeToString(list)).apply()
            }
        }
        result
    }

    suspend fun loadStarPath(url: String, forceScrape: Boolean = false): Result<StarPathList> =
        withContext(Dispatchers.IO) {
            val listId = getListIdForUrl(url)
            val cacheFile = File(context.filesDir, "starpath_$listId.json")

            // Remember checked duties and titles for this list id
            val checkedIds = getCheckedDutyIds(listId)
            val checkedTitles = getCheckedDutyTitles(listId)

            // If the current in-memory list matches this list, retain any currently completed items
            val inMemoryCompletedTitles = if (_currentStarPath.value?.let { getListIdForUrl(it.sourceUrl) } == listId) {
                _currentStarPath.value?.duties?.filter { it.isCompleted }?.map { it.title.trim().lowercase(java.util.Locale.ROOT) }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
            val inMemoryCompletedIds = if (_currentStarPath.value?.let { getListIdForUrl(it.sourceUrl) } == listId) {
                _currentStarPath.value?.duties?.filter { it.isCompleted }?.map { it.id }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }

            val allCheckedIds = checkedIds + inMemoryCompletedIds
            val allCheckedTitles = checkedTitles + inMemoryCompletedTitles

            fun applyChecked(duties: List<StarPathDuty>): List<StarPathDuty> {
                return duties.map { duty ->
                    val normTitle = duty.title.trim().lowercase(java.util.Locale.ROOT)
                    val isCompleted = allCheckedIds.contains(duty.id) ||
                            allCheckedTitles.contains(normTitle) ||
                            allCheckedTitles.contains(duty.title.trim())
                    duty.copy(isCompleted = isCompleted)
                }
            }

            var loadedList: StarPathList? = null

            // 1. If not forcing scrape, check cached local file
            if (!forceScrape && cacheFile.exists()) {
                try {
                    val content = cacheFile.readText()
                    val parsed = json.decodeFromString<StarPathList>(content)
                    loadedList = parsed.copy(
                        id = listId,
                        duties = applyChecked(parsed.duties)
                    )
                } catch (_: Exception) {
                    // Cache corrupt or unreadable, fall through
                }
            }

            // 2. If still null and matches a preset asset, load from bundled assets
            if (loadedList == null && !forceScrape) {
                val preset = PRESETS.find { it.id.equals(listId, ignoreCase = true) }
                if (preset != null && preset.assetFileName.isNotEmpty()) {
                    try {
                        val assetContent = context.assets.open(preset.assetFileName).bufferedReader().use { it.readText() }
                        val parsed = json.decodeFromString<StarPathList>(assetContent)
                        loadedList = parsed.copy(
                            id = listId,
                            duties = applyChecked(parsed.duties)
                        )
                        saveListToFile(loadedList, cacheFile)
                    } catch (_: Exception) {
                        // Fall through to scraping
                    }
                }
            }

            // 3. If still null or forceScrape is requested, scrape from the web
            if (loadedList == null || forceScrape) {
                val scrapeResult = StarPathScraper.scrapeUrl(url)
                if (scrapeResult.isSuccess) {
                    val scraped = scrapeResult.getOrThrow()
                    loadedList = scraped.copy(
                        id = listId,
                        duties = applyChecked(scraped.duties)
                    )
                    saveListToFile(loadedList, cacheFile)
                } else if (loadedList == null) {
                    return@withContext Result.failure(
                        scrapeResult.exceptionOrNull() ?: Exception("Failed to load Star Path data")
                    )
                }
            }

            // Always update persistent checked status with the merged list
            saveCheckedStatus(listId, loadedList.duties.filter { d -> d.isCompleted })

            prefs.edit().putString(KEY_LAST_URL, url).apply()
            _currentStarPath.value = loadedList
            Result.success(loadedList)
        }

    suspend fun toggleDutyCompleted(dutyId: String) = withContext(Dispatchers.IO) {
        val current = _currentStarPath.value ?: return@withContext
        val listId = getListIdForUrl(current.sourceUrl)
        val updatedDuties = current.duties.map { duty ->
            if (duty.id == dutyId) {
                duty.copy(isCompleted = !duty.isCompleted)
            } else {
                duty
            }
        }
        val updatedList = current.copy(id = listId, duties = updatedDuties)
        _currentStarPath.value = updatedList

        saveCheckedStatus(listId, updatedDuties.filter { it.isCompleted })

        val cacheFile = File(context.filesDir, "starpath_$listId.json")
        saveListToFile(updatedList, cacheFile)
    }

    suspend fun resetAllChecked() = withContext(Dispatchers.IO) {
        val current = _currentStarPath.value ?: return@withContext
        val listId = getListIdForUrl(current.sourceUrl)
        val updatedDuties = current.duties.map { it.copy(isCompleted = false) }
        val updatedList = current.copy(id = listId, duties = updatedDuties)
        _currentStarPath.value = updatedList

        prefs.edit()
            .remove("checked_duties_$listId")
            .remove("checked_titles_$listId")
            .apply()

        val cacheFile = File(context.filesDir, "starpath_$listId.json")
        saveListToFile(updatedList, cacheFile)
    }

    suspend fun markAllChecked() = withContext(Dispatchers.IO) {
        val current = _currentStarPath.value ?: return@withContext
        val listId = getListIdForUrl(current.sourceUrl)
        val updatedDuties = current.duties.map { it.copy(isCompleted = true) }
        val updatedList = current.copy(id = listId, duties = updatedDuties)
        _currentStarPath.value = updatedList

        saveCheckedStatus(listId, updatedDuties)

        val cacheFile = File(context.filesDir, "starpath_$listId.json")
        saveListToFile(updatedList, cacheFile)
    }

    private fun getCheckedDutyIds(listId: String): Set<String> {
        return prefs.getStringSet("checked_duties_$listId", emptySet()) ?: emptySet()
    }

    private fun getCheckedDutyTitles(listId: String): Set<String> {
        return prefs.getStringSet("checked_titles_$listId", emptySet()) ?: emptySet()
    }

    private fun saveCheckedStatus(listId: String, completedDuties: List<StarPathDuty>) {
        val ids = completedDuties.map { it.id }.toSet()
        val titles = completedDuties.map { it.title.trim().lowercase(java.util.Locale.ROOT) }.toSet()
        prefs.edit()
            .putStringSet("checked_duties_$listId", ids)
            .putStringSet("checked_titles_$listId", titles)
            .apply()
    }

    private fun saveListToFile(list: StarPathList, file: File) {
        try {
            val content = json.encodeToString(list)
            file.writeText(content)
        } catch (_: Exception) {}
    }
}
