package com.stardell.parpilotai.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stardell.parpilotai.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class ScoreChip(
    val title: String,
    val relativeScore: Int
)

enum class AppTheme(val rawValue: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromString(value: String?): AppTheme {
            return values().find { it.rawValue == value } ?: SYSTEM
        }
    }
}

enum class AppStyleTheme(val rawValue: String) {
    CLASSIC_GREEN("Classic Green"),
    DARK_GOLD("Dark & Gold"),
    PRO_TOUR_BLUE("Pro Tour Blue");

    companion object {
        fun fromString(value: String?): AppStyleTheme {
            return values().find { it.rawValue == value } ?: CLASSIC_GREEN
        }
    }
}

class GolferViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("ParPilotAI_Prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // StateFlows replacing @Published
    private val _golfers = MutableStateFlow<List<Golfer>>(emptyList())
    val golfers: StateFlow<List<Golfer>> = _golfers.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _pastRounds = MutableStateFlow<List<Scorecard>>(emptyList())
    val pastRounds: StateFlow<List<Scorecard>> = _pastRounds.asStateFlow()

    private val _scoreChips = MutableStateFlow<List<ScoreChip>>(emptyList())
    val scoreChips: StateFlow<List<ScoreChip>> = _scoreChips.asStateFlow()

    private val _activeScorecard = MutableStateFlow<Scorecard?>(null)
    val activeScorecard: StateFlow<Scorecard?> = _activeScorecard.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // User Settings
    private val _themePreference = MutableStateFlow(AppTheme.SYSTEM)
    val themePreference: StateFlow<AppTheme> = _themePreference.asStateFlow()

    private val _appStyleTheme = MutableStateFlow(AppStyleTheme.CLASSIC_GREEN)
    val appStyleTheme: StateFlow<AppStyleTheme> = _appStyleTheme.asStateFlow()

    private val _hostName = MutableStateFlow("")
    val hostName: StateFlow<String> = _hostName.asStateFlow()

    private val _useSlope = MutableStateFlow(true)
    val useSlope: StateFlow<Boolean> = _useSlope.asStateFlow()

    private val _ownerHandicap = MutableStateFlow(18)
    val ownerHandicap: StateFlow<Int> = _ownerHandicap.asStateFlow()

    private val _myBagDistances = MutableStateFlow<Map<String, Int>>(emptyMap())
    val myBagDistances: StateFlow<Map<String, Int>> = _myBagDistances.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _golfers.value = loadList("ParPilotAI_Golfers")
            _courses.value = loadList("ParPilotAI_Courses")
            _pastRounds.value = loadList("ParPilotAI_PastRounds")
            
            val chips: List<ScoreChip> = loadList("ParPilotAI_ScoreChips")
            if (chips.isEmpty()) {
                _scoreChips.value = listOf(
                    ScoreChip("DOUBLE", 2),
                    ScoreChip("BOGEY", 1),
                    ScoreChip("PAR", 0),
                    ScoreChip("MAX", 5),
                    ScoreChip("QUAD", 4),
                    ScoreChip("TRIPLE", 3),
                    ScoreChip("ACE", -999),
                    ScoreChip("ALBATROSS", -3),
                    ScoreChip("EAGLE", -2),
                    ScoreChip("BIRDIE", -1)
                )
            } else {
                _scoreChips.value = chips
            }

            _themePreference.value = AppTheme.fromString(prefs.getString("AppThemePreference", "System"))
            _appStyleTheme.value = AppStyleTheme.fromString(prefs.getString("ParPilotAI_Theme", "Classic Green"))
            _hostName.value = prefs.getString("ParPilotAI_HostName", "") ?: ""
            _useSlope.value = prefs.getBoolean("ParPilotAI_UseSlope", true)
            
            val ownerHcp = prefs.getInt("ParPilotAI_OwnerHCP", 0)
            _ownerHandicap.value = if (ownerHcp == 0) 18 else ownerHcp
            
            val bagJson = prefs.getString("ParPilotAI_OwnerBag", null)
            if (bagJson != null) {
                val type = object : TypeToken<Map<String, Int>>() {}.type
                _myBagDistances.value = gson.fromJson(bagJson, type) ?: emptyMap()
            }
        }
    }

    // MARK: - Generic Persistence Helpers
    private inline fun <reified T> loadList(key: String): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<T>>() {}.type
        return try { gson.fromJson(json, type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    private fun <T> saveList(key: String, list: List<T>) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit().putString(key, gson.toJson(list)).apply()
        }
    }

    // MARK: - Actions
    fun updateThemePreference(theme: AppTheme) {
        _themePreference.value = theme
        prefs.edit().putString("AppThemePreference", theme.rawValue).apply()
    }

    fun updateAppStyleTheme(theme: AppStyleTheme) {
        _appStyleTheme.value = theme
        prefs.edit().putString("ParPilotAI_Theme", theme.rawValue).apply()
    }

    fun updateHostName(name: String) {
        _hostName.value = name
        prefs.edit().putString("ParPilotAI_HostName", name).apply()
    }

    fun updateUseSlope(use: Boolean) {
        _useSlope.value = use
        prefs.edit().putBoolean("ParPilotAI_UseSlope", use).apply()
    }

    fun updateOwnerHandicap(hcp: Int) {
        _ownerHandicap.value = hcp
        prefs.edit().putInt("ParPilotAI_OwnerHCP", hcp).apply()
    }

    fun addGolfer(golfer: Golfer) {
        val updated = _golfers.value + golfer
        _golfers.value = updated
        saveList("ParPilotAI_Golfers", updated)
    }

    fun updateGolfer(golfer: Golfer) {
        val updated = _golfers.value.map { if (it.id == golfer.id) golfer else it }
        _golfers.value = updated
        saveList("ParPilotAI_Golfers", updated)
    }

    fun deleteGolfer(golfer: Golfer) {
        val updated = _golfers.value.filter { it.id != golfer.id }
        _golfers.value = updated
        saveList("ParPilotAI_Golfers", updated)
    }

    fun addCourse(course: Course) {
        val updated = _courses.value + course
        _courses.value = updated
        saveList("ParPilotAI_Courses", updated)
    }

    fun deleteCourse(course: Course) {
        val updated = _courses.value.filter { it.id != course.id }
        _courses.value = updated
        saveList("ParPilotAI_Courses", updated)
    }

    fun startNewScorecard(course: Course, golfers: List<Golfer>) {
        _activeScorecard.value = Scorecard(
            courseId = course.id,
            courseNameSnapshot = course.name,
            golferIds = golfers.map { it.id }
        )
    }

    fun completeActiveRound() {
        val round = _activeScorecard.value ?: return
        round.date = Date()
        val updatedRounds = _pastRounds.value + round
        _pastRounds.value = updatedRounds
        saveList("ParPilotAI_PastRounds", updatedRounds)
        _activeScorecard.value = null
        _selectedTab.value = 0
    }

    fun deleteRound(scorecard: Scorecard) {
        val updated = _pastRounds.value.filter { it.id != scorecard.id }
        _pastRounds.value = updated
        saveList("ParPilotAI_PastRounds", updated)
    }

    fun updateScore(golferId: UUID, hole: Int, score: Int?) {
        val scorecard = _activeScorecard.value ?: return
        // Create a copy to trigger StateFlow emission
        val updatedMap = scorecard.scores.toMutableMap()
        val key = "${golferId}_${hole}"
        if (score != null) {
            updatedMap[key] = score
        } else {
            updatedMap.remove(key)
        }
        _activeScorecard.value = scorecard.copy(scores = updatedMap)
    }
}
