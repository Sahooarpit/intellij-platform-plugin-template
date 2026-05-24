package org.jetbrains.plugins.template.api

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object ApiExecutor {
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

    fun execute(url: String, method: String, headers: String, body: String): String {
        return try {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, if (method == "GET" || method == "DELETE")
                    HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(body))

            headers.lines().filter { it.contains(":") }.forEach {
                val parts = it.split(":", limit = 2)
                requestBuilder.header(parts[0].trim(), parts[1].trim())
            }

            val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
            "Status: ${response.statusCode()}\n\n${response.body()}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}