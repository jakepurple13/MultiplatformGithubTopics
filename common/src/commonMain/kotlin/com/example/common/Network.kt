package com.example.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Serializable
data class GithubTopics(
    val items: List<GitHubTopic>
)

@Serializable
data class GitHubTopic(
    @SerialName("html_url")
    val htmlUrl: String,
    val url: String,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val description: String? = null,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("pushed_at")
    val pushedAt: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("stargazers_count")
    val stars: Int,
    val watchers: Int,
    @SerialName("forks_count")
    val forks: Int = 0,
    val language: String = "No language",
    val owner: Owner,
    val license: License? = null,
    @SerialName("default_branch")
    val branch: String,
    val topics: List<String> = emptyList()
)

@Serializable
data class Owner(
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class License(
    val name: String,
)

@Serializable
data class GitHubRepo(
    val content: String,
    val encoding: String,
)

@Serializable
sealed class ReadMeResponse {
    class Success(val content: String) : ReadMeResponse()

    @Serializable
    class Failed(val message: String) : ReadMeResponse()

    object Loading : ReadMeResponse()
}

object Network {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient {
            install(Logging)
            install(ContentNegotiation) { json(json) }
        }
    }

    private val timePrinter = PrettyTime()
    private val format = SimpleDateFormat.getDateTimeInstance()

    suspend fun getTopics(page: Int, vararg topics: String) = runCatching {
        val url =
            "https://api.github.com/search/repositories?q=" + topics.joinToString(separator = "+") { "topic:$it" } + "+sort:updated-desc&page=$page"

        client.get(url).body<GithubTopics>().items.map {
            val date = Instant.parse(it.pushedAt).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            it.copy(pushedAt = "Updated " + timePrinter.format(Date(date)) + " on\n" + format.format(date))
        }
    }

    suspend fun getReadMe(fullName: String) = runCatching {
        val response = client.get("https://api.github.com/repos/$fullName/readme") {
            header("Accept", "application/vnd.github.raw+json")
        }.bodyAsText()

        try {
            json.decodeFromString<ReadMeResponse.Failed>(response)
        } catch (e: Exception) {
            ReadMeResponse.Success(response)
        }
    }
}