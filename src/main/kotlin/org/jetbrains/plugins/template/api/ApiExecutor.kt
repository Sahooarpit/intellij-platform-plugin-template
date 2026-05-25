package org.jetbrains.plugins.template.api

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

object ApiExecutor {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    fun execute(url: String, method: String, headersText: String, paramsText: String, body: String): String {
        return try {
            // 1. Build URL with Query Parameters
            val fullUrl = if (paramsText.isBlank()) url else {
                val separator = if (url.contains("?")) "&" else "?"
                val encodedParams = paramsText.lines()
                    .filter { it.contains("=") }
                    .joinToString("&") { line ->
                        val parts = line.split("=", limit = 2)
                        val key = URLEncoder.encode(parts[0].trim(), StandardCharsets.UTF_8)
                        val value = URLEncoder.encode(parts[1].trim(), StandardCharsets.UTF_8)
                        "$key=$value"
                    }
                url + separator + encodedParams
            }

            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .method(method, if (method == "GET" || method == "DELETE")
                    HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body))

            // 2. Parse and Add Headers
            headersText.lines().filter { it.contains(":") }.forEach {
                val parts = it.split(":", limit = 2)
                requestBuilder.header(parts[0].trim(), parts[1].trim())
            }

            val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
            "Status: ${response.statusCode()}\nURL: $fullUrl\n\n${response.body()}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}