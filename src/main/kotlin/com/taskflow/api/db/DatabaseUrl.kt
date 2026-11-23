package com.taskflow.api.db

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class JdbcSettings(val url: String, val user: String?, val password: String?)

object DatabaseUrl {

    fun parse(raw: String): JdbcSettings {
        val value = raw.trim()
        val withoutJdbcPrefix = value.removePrefix("jdbc:")
        val schemeSeparator = withoutJdbcPrefix.indexOf("://")
        if (schemeSeparator < 0) {
            throw IllegalArgumentException("Unsupported database URL: $value")
        }

        var remainder = withoutJdbcPrefix.substring(schemeSeparator + 3)
        var user: String? = null
        var password: String? = null

        val lastAt = remainder.lastIndexOf('@')
        val firstSlash = remainder.indexOf('/')
        if (lastAt > 0 && (firstSlash < 0 || lastAt < firstSlash)) {
            val userInfo = remainder.substring(0, lastAt)
            val colon = userInfo.indexOf(':')
            user = decode(if (colon >= 0) userInfo.substring(0, colon) else userInfo)
            if (colon >= 0) {
                password = decode(userInfo.substring(colon + 1))
            }
            remainder = remainder.substring(lastAt + 1)
        }

        val queryIndex = remainder.indexOf('?')
        val location = if (queryIndex >= 0) remainder.substring(0, queryIndex) else remainder
        val query = if (queryIndex >= 0) remainder.substring(queryIndex + 1) else ""

        val params = query.split("&").filter { it.isNotBlank() && !it.startsWith("stringtype=") }
        val queryString = (params + STRING_TYPE_PARAM).joinToString("&")

        return JdbcSettings(url = "jdbc:postgresql://$location?$queryString", user = user, password = password)
    }

    private const val STRING_TYPE_PARAM = "stringtype=unspecified"

    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
}