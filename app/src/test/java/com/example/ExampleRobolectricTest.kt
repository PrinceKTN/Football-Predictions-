package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.PredictionRepository(db.betPredictionDao())
    val sites = repo.analyzedSites
    assert(sites.any { it.id == "forebet" })
    assert(sites.any { it.id == "predictz" })
    assert(sites.any { it.id == "windrawwin" })
  }

  @Test
  fun `verify sportybet curated picks are available for today and tomorrow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.PredictionRepository(db.betPredictionDao())
    val picks = repo.todayAndTomorrowSportyBetPicks
    assert(picks.isNotEmpty())
    assert(picks.any { it.scheduleDay == "Today" })
    assert(picks.any { it.scheduleDay == "Tomorrow" })
  }
}
