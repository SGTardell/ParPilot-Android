package com.stardell.parpilotai.api

import com.stardell.parpilotai.models.Course
import com.stardell.parpilotai.models.Hole
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// MARK: - API Models

data class CourseSearchResponse(val courses: List<APICourse>)
data class CourseDetailResponse(val course: APICourse)

data class APICourse(
    val id: Int,
    val club_name: String?,
    val course_name: String?,
    val location: APILocation?,
    val tees: APITees?
) {
    val formattedName: String
        get() {
            val n1 = club_name?.trim() ?: ""
            val n2 = course_name?.trim() ?: ""
            
            if (n1.isEmpty() && n2.isEmpty()) return "Unknown Course"
            if (n1.isEmpty()) return n2
            if (n2.isEmpty()) return n1
            if (n1.lowercase() == n2.lowercase()) return n1
            return "$n1 - $n2"
        }
        
    fun toLocalCourse(teeBox: APITeeBox): Course {
        val fullName = "$formattedName (${teeBox.tee_name ?: "Default Tees"})"
        
        val localHoles = mutableListOf<Hole>()
        teeBox.holes?.forEachIndexed { index, appHole ->
            localHoles.add(
                Hole(
                    number = index + 1,
                    par = appHole.par ?: 4,
                    yardage = appHole.yardage ?: 0,
                    handicap = appHole.handicap ?: 18
                )
            )
        }
        
        return Course(
            name = fullName,
            holes = localHoles,
            latitude = location?.latitude,
            longitude = location?.longitude,
            slope = teeBox.slope_rating ?: 113,
            rating = teeBox.course_rating
        )
    }
}

data class APILocation(
    val city: String?,
    val state: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class APITees(
    val female: List<APITeeBox>?,
    val male: List<APITeeBox>?
) {
    val allTees: List<APITeeBox>
        get() {
            val combined = mutableListOf<APITeeBox>()
            male?.let { males ->
                combined.addAll(males.map { it.copy(tee_name = "${it.tee_name ?: "Unknown"} (M)") })
            }
            female?.let { females ->
                combined.addAll(females.map { it.copy(tee_name = "${it.tee_name ?: "Unknown"} (W)") })
            }
            return combined
        }
}

data class APITeeBox(
    var tee_name: String?,
    val course_rating: Double?,
    val slope_rating: Int?,
    val total_yards: Int?,
    val par_total: Int?,
    val holes: List<APIHole>?
)

data class APIHole(
    val par: Int?,
    val yardage: Int?,
    val handicap: Int?
)

// MARK: - API Client Service

class GolfCourseAPIClient private constructor() {
    
    companion object {
        val shared = GolfCourseAPIClient()
    }

    private val apiKey = "45W3S5TYQMIJHN35DQNZJVEXAQ"
    private val baseURL = "https://api.golfcourseapi.com/v1"
    private val gson = Gson()

    class APIException(message: String) : Exception(message)

    private suspend inline fun <reified T> fetch(endpoint: String): T = withContext(Dispatchers.IO) {
        val url = URL(baseURL + endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", "Key $apiKey")
        
        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw APIException("HTTP Error: $responseCode")
            }
            
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(responseText, T::class.java)
        } catch (e: Exception) {
            throw APIException("Network or Decoding Error: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun searchCourses(query: String): List<APICourse> {
        if (query.isEmpty()) return emptyList()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val response: CourseSearchResponse = fetch("/search?search_query=$encodedQuery")
        return response.courses
    }

    suspend fun getCourseDetails(id: Int): APICourse {
        val response: CourseDetailResponse = fetch("/courses/$id")
        return response.course
    }
}
