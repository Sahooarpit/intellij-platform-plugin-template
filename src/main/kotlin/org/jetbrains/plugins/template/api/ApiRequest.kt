package org.jetbrains.plugins.template.api

data class ApiRequest(
    var name: String = "New Request",
    var url: String = "https://api.example.com",
    var method: String = "GET",
    // Store as Map for easy access
    var headers: MutableMap<String, String> = mutableMapOf(),
    var queryParams: MutableMap<String, String> = mutableMapOf(),
    var body: String = ""
)