package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.FavoriteVideoEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.YouPlayerDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: YouPlayerDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, YouPlayerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("YouPlayer", appName)
    }

    @Test
    fun testFavoriteDaoOperations() = runBlocking {
        val dao = database.favoriteDao()

        val sample = FavoriteVideoEntity(
            id = "test_vid_1",
            title = "Test Video",
            channelTitle = "Test Channel",
            thumbnailUrl = "https://example.com/thumb.jpg",
            duration = "4:20",
            viewsCount = "10K views"
        )

        dao.insert(sample)
        val isFav = dao.isFavorite("test_vid_1").first()
        assertTrue(isFav)

        val allFavs = dao.getAllFavorites().first()
        assertEquals(1, allFavs.size)
        assertEquals("Test Video", allFavs[0].title)

        dao.deleteById("test_vid_1")
        val isFavAfterDelete = dao.isFavorite("test_vid_1").first()
        assertFalse(isFavAfterDelete)
    }

    @Test
    fun testHistoryDaoOperations() = runBlocking {
        val dao = database.watchHistoryDao()

        val item = WatchHistoryEntity(
            id = "hist_vid_1",
            title = "History Video",
            channelTitle = "Channel",
            thumbnailUrl = "https://example.com/h.jpg",
            duration = "10:00",
            viewsCount = "1M views",
            progressSeconds = 120
        )

        dao.insert(item)
        val historyList = dao.getHistory().first()
        assertEquals(1, historyList.size)
        assertEquals(120, historyList[0].progressSeconds)

        dao.clearAll()
        val clearedList = dao.getHistory().first()
        assertTrue(clearedList.isEmpty())
    }

    @Test
    fun testLoggerBuffer() {
        com.example.util.YouPlayerLogger.clear()
        assertEquals(0, com.example.util.YouPlayerLogger.logsFlow.value.size)

        com.example.util.YouPlayerLogger.i("TestTag", "Sample Info Message")
        com.example.util.YouPlayerLogger.e("TestTag", "Sample Error Message")

        val currentLogs = com.example.util.YouPlayerLogger.logsFlow.value
        assertEquals(2, currentLogs.size)
        assertEquals(com.example.util.LogLevel.ERROR, currentLogs[0].level)
        assertEquals(com.example.util.LogLevel.INFO, currentLogs[1].level)
    }
}
