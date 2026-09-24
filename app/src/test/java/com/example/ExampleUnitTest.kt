package com.example

import com.example.data.remote.CuratedVideoCatalog
import com.example.data.remote.PrivacyApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFormatSeconds() {
        assertEquals("LIVE", PrivacyApiClient.formatSeconds(0))
        assertEquals("0:45", PrivacyApiClient.formatSeconds(45))
        assertEquals("3:33", PrivacyApiClient.formatSeconds(213))
        assertEquals("1:05:20", PrivacyApiClient.formatSeconds(3920))
    }

    @Test
    fun testFormatViews() {
        assertEquals("1.5B views", PrivacyApiClient.formatViews(1_500_000_000L))
        assertEquals("24.0M views", PrivacyApiClient.formatViews(24_000_000L))
        assertEquals("85.5K views", PrivacyApiClient.formatViews(85_500L))
        assertEquals("450 views", PrivacyApiClient.formatViews(450L))
    }

    @Test
    fun testCuratedCatalogTrending() {
        val all = CuratedVideoCatalog.getTrendingVideos("All")
        assertTrue(all.isNotEmpty())

        val music = CuratedVideoCatalog.getTrendingVideos("Music")
        assertTrue(music.any { it.category.equals("Music", ignoreCase = true) })

        val tech = CuratedVideoCatalog.getTrendingVideos("Tech")
        assertTrue(tech.any { it.category.equals("Tech", ignoreCase = true) })
    }

    @Test
    fun testCuratedCatalogSearch() {
        val results = CuratedVideoCatalog.searchCurated("4K")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.title.contains("4K", ignoreCase = true) })
    }

    @Test
    fun testCuratedCatalogRelated() {
        val firstId = CuratedVideoCatalog.ALL_VIDEOS.first().id
        val related = CuratedVideoCatalog.getRelatedVideos(firstId)
        assertTrue(related.isNotEmpty())
        assertFalse(related.any { it.id == firstId })
    }
}
