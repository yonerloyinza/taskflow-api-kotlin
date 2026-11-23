package com.taskflow.api.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseUrlTest {

    @Test
    fun `parses postgres url with credentials`() {
        val settings = DatabaseUrl.parse("postgresql://user:pass@host:5432/insforge?sslmode=require")

        assertEquals("jdbc:postgresql://host:5432/insforge?sslmode=require&stringtype=unspecified", settings.url)
        assertEquals("user", settings.user)
        assertEquals("pass", settings.password)
    }

    @Test
    fun `keeps existing jdbc prefix`() {
        val settings = DatabaseUrl.parse("jdbc:postgresql://host:6543/db")

        assertEquals("jdbc:postgresql://host:6543/db?stringtype=unspecified", settings.url)
        assertNull(settings.user)
        assertNull(settings.password)
    }

    @Test
    fun `decodes percent encoded credentials`() {
        val settings = DatabaseUrl.parse("postgresql://user%40mail:p%40ss@host:5432/db")

        assertEquals("user@mail", settings.user)
        assertEquals("p@ss", settings.password)
    }

    @Test
    fun `replaces an existing stringtype parameter`() {
        val settings = DatabaseUrl.parse("postgresql://host/db?stringtype=varchar")

        assertEquals("jdbc:postgresql://host/db?stringtype=unspecified", settings.url)
    }

    @Test
    fun `parses url without credentials`() {
        val settings = DatabaseUrl.parse("postgresql://host:5432/db?sslmode=require")

        assertEquals("jdbc:postgresql://host:5432/db?sslmode=require&stringtype=unspecified", settings.url)
        assertNull(settings.user)
    }
}
