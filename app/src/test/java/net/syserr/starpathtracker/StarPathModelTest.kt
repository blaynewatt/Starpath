package net.syserr.starpathtracker

import net.syserr.starpathtracker.data.model.StarPathDuty
import net.syserr.starpathtracker.data.model.StarPathList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarPathModelTest {

    @Test
    fun testProgressAndTokenCalculations() {
        val duties = listOf(
            StarPathDuty(id = "1", title = "Task 1", tokenReward = 5, isCompleted = true),
            StarPathDuty(id = "2", title = "Task 2", tokenReward = 10, isCompleted = false),
            StarPathDuty(id = "3", title = "Task 3", tokenReward = 25, isCompleted = true),
            StarPathDuty(id = "4", title = "Task 4", tokenReward = 40, isCompleted = false)
        )

        val list = StarPathList(
            id = "test_list",
            title = "Test Star Path",
            sourceUrl = "https://example.com/test",
            duties = duties
        )

        assertEquals(4, list.totalDuties)
        assertEquals(2, list.completedDuties)
        assertEquals(0.5f, list.progressFraction, 0.001f)
        assertEquals(50, list.progressPercentage)

        assertEquals(80, list.totalTokens)
        assertEquals(30, list.earnedTokens)
        assertEquals(50, list.remainingTokens)
    }

    @Test
    fun testSectionsExtraction() {
        val duties = listOf(
            StarPathDuty(id = "1", title = "Task 1", section = "Main Duties"),
            StarPathDuty(id = "2", title = "Task 2", section = "Week 1"),
            StarPathDuty(id = "3", title = "Task 3", section = "Main Duties"),
            StarPathDuty(id = "4", title = "Task 4", section = "Week 2")
        )

        val list = StarPathList(
            id = "test_list",
            title = "Test Star Path",
            sourceUrl = "https://example.com/test",
            duties = duties
        )

        assertEquals(listOf("Main Duties", "Week 1", "Week 2"), list.sections)
    }
}
