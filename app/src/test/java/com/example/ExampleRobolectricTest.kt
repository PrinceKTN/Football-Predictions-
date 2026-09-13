package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.SiteAccuracyHistory
import com.example.data.repository.HistoricalAccuracyData
import com.example.data.repository.PredictionRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Betting Site Analyzer", appName)
  }

  @Test
  fun `verify analyzed sites directory has forebet and predictz`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = PredictionRepository(db.betPredictionDao())
    val sites = repo.analyzedSites
    assert(sites.any { it.id == "forebet" })
    assert(sites.any { it.id == "predictz" })
    assert(sites.any { it.id == "windrawwin" })
  }

  @Test
  fun `verify sportybet curated picks are available for today and tomorrow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val repo = PredictionRepository(db.betPredictionDao())
    val picks = repo.todayAndTomorrowSportyBetPicks
    assert(picks.isNotEmpty())
    assert(picks.any { it.scheduleDay == "Today" })
    assert(picks.any { it.scheduleDay == "Tomorrow" })
  }

  @Test
  fun `verify 30-day historical accuracy data is populated for tracked sites`() {
    val histories: List<SiteAccuracyHistory> = HistoricalAccuracyData.siteHistories
    assert(histories.isNotEmpty())
    
    // Check key tracked sites are included
    val siteIds = histories.map { it.siteId }
    assert(siteIds.contains("all_aggregate"))
    assert(siteIds.contains("forebet"))
    assert(siteIds.contains("predictz"))
    assert(siteIds.contains("windrawwin"))
    assert(siteIds.contains("vitibet"))
    assert(siteIds.contains("statarea"))
    assert(siteIds.contains("betensured"))
    assert(siteIds.contains("tipstrr"))

    // Verify each site contains exactly 30 days of data
    histories.forEach { site ->
      assertEquals(30, site.dailyPoints.size)
      assert(site.overall30DayAccuracy in 30.0..80.0)
      assert(site.dailyPoints.first().dayNumber == 1)
      assert(site.dailyPoints.last().dayNumber == 30)
      site.dailyPoints.forEach { pt ->
        assert(pt.accuracyPercentage in 0.0..100.0)
        assert(pt.betsWon <= pt.betsTotal)
        assert(pt.betsTotal > 0)
      }
    }
  }
}
