package com.example.starpathtracker.ui.main

import com.example.starpathtracker.data.StarPathRepository
import com.example.starpathtracker.data.model.StarPathDuty
import com.example.starpathtracker.data.model.StarPathList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun testDutyTabValues() {
        assertEquals("Path", DutyTab.PATH.label)
        assertEquals("Weekly", DutyTab.WEEKLY.label)
    }

    @Test
    fun testSortOrderValues() {
        assertEquals("Default", SortOrder.DEFAULT.label)
        assertEquals("Tokens (High-Low)", SortOrder.REWARD_HIGH_TO_LOW.label)
        assertEquals("Name (A-Z)", SortOrder.TITLE.label)
    }

    @Test
    fun testDefaultEntriesLastIsHauntingElegance() {
        val last = StarPathRepository.DEFAULT_ENTRIES.last()
        assertEquals("Haunting Elegance", last.title)
        assertTrue(last.url.contains("All_Haunting_Elegance_Star_Path_Duties"))
    }

    @Test
    fun testWeeklyDutySeparation() {
        val pathDuty = StarPathDuty(
            id = "d1",
            title = "Uproot Night Thorns",
            section = "All Haunting Elegance Star Path Duties"
        )
        val weeklyDuty = StarPathDuty(
            id = "d2",
            title = "Mine 20 Orange Gems",
            section = "All Haunting Elegance Star Path Routine Duties"
        )
        val week3Duty = StarPathDuty(
            id = "d3",
            title = "Hang Out with Jack",
            section = "Week 1: September 23 - September 30"
        )

        assertFalse(pathDuty.isWeeklyDuty)
        assertTrue(weeklyDuty.isWeeklyDuty)
        assertTrue(week3Duty.isWeeklyDuty)

        val list = StarPathList(
            id = "test",
            title = "Test Star Path",
            sourceUrl = "https://example.com",
            duties = listOf(pathDuty, weeklyDuty, week3Duty)
        )

        assertEquals(1, list.pathDuties.size)
        assertEquals("Uproot Night Thorns", list.pathDuties[0].title)

        assertEquals(2, list.weeklyDuties.size)
        assertEquals("Mine 20 Orange Gems", list.weeklyDuties[0].title)
        assertEquals("Hang Out with Jack", list.weeklyDuties[1].title)
    }

    @Test
    fun testHideCompletedFiltering() {
        val duties = listOf(
            StarPathDuty(id = "1", title = "Task 1", isCompleted = true, section = "Main"),
            StarPathDuty(id = "2", title = "Task 2", isCompleted = false, section = "Main")
        )

        val visibleAll = duties.filter { true }
        val visibleOnlyIncomplete = duties.filter { !it.isCompleted }

        assertEquals(2, visibleAll.size)
        assertEquals(1, visibleOnlyIncomplete.size)
        assertEquals("Task 2", visibleOnlyIncomplete[0].title)
    }
}

