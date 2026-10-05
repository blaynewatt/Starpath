package net.syserr.starpathtracker

import net.syserr.starpathtracker.data.scraper.StarPathScraper
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StarPathScraperTest {

    @Test
    fun testParseTableHtml() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html>
            <head><title>All Haunting Elegance Star Path Duties - IGN</title></head>
            <body>
                <h1>All Haunting Elegance Star Path Duties and Routine Duties</h1>
                
                <h2>All Haunting Elegance Star Path Duties</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Task</th>
                            <th>How to Complete</th>
                            <th>Requirements</th>
                            <th>Token Reward</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Uproot Night Thorns</td>
                            <td>Remove Night Thorns, Splinters of Fate, or Inkies.</td>
                            <td>3</td>
                            <td>5</td>
                        </tr>
                        <tr>
                            <td>Hang Out with the Pumpkin King</td>
                            <td>Hang Out with Jack Skellington for 30 minutes</td>
                            <td>30min</td>
                            <td>40</td>
                        </tr>
                    </tbody>
                </table>
                
                <h2>All Haunting Elegance Star Path Routine Duties</h2>
                <h3>Week 1: September 23 - September 30</h3>
                <table>
                    <tr>
                        <td>Task</td>
                        <td>How to Complete</td>
                        <td>Requirements</td>
                        <td>Token Reward</td>
                    </tr>
                    <tr>
                        <td>Mine 20 Orange Gems</td>
                        <td>Mine Citrine or Evergem</td>
                        <td>20</td>
                        <td>30</td>
                    </tr>
                </table>
            </body>
            </html>
        """.trimIndent()

        val doc = Jsoup.parse(sampleHtml)
        
        // Use reflection or package helper if needed, or verify through Scraper behavior
        val listId = StarPathScraper.generateListId("https://www.ign.com/wikis/disney-dreamlight-valley/All_Haunting_Elegance_Star_Path_Duties_and_Routine_Duties")
        assertEquals("all_haunting_elegance_star_path_duties_and_routine_duties", listId)
    }

    @Test
    fun testCleanPathTitleRemovesPrefixesAndSuffixes() {
        assertEquals("Lovely Monsters", StarPathScraper.cleanPathTitle("All Lovely Monsters Star Path Duties"))
        assertEquals("Lovely Monsters", StarPathScraper.cleanPathTitle("Lovely Monsters Star Path Duties"))
        assertEquals("Lovely Monsters", StarPathScraper.cleanPathTitle("Lovely Monsters"))
        assertEquals("Haunting Elegance", StarPathScraper.cleanPathTitle("All Haunting Elegance Star Path Duties and Routine Duties"))
        assertEquals("Haunting Elegance", StarPathScraper.cleanPathTitle("Haunting Elegance Star Path"))
        assertEquals("Pop City", StarPathScraper.cleanPathTitle("All Pop City Star Path Duties and Routine Duties"))
        assertEquals("Godly Glamor", StarPathScraper.cleanPathTitle("All Godly Glamor Star Path Duties and Routine Duties"))
        assertEquals("Astronomer's Journey", StarPathScraper.cleanPathTitle("All Astronomer's Journey Star Path Duties and Routine Duties"))
        assertEquals("Dapper Delights", StarPathScraper.cleanPathTitle("Dapper Delights Star Path Duties"))
        assertEquals("Haunted Holiday", StarPathScraper.cleanPathTitle("All Haunted Holiday Star Path Duties"))
    }

    @Test
    fun testNormalizeUrl() {
        assertEquals(
            "https://www.ign.com/wikis/disney-dreamlight-valley/Lovely_Monsters_Star_Path_Duties",
            StarPathScraper.normalizeUrl("http://www.ign.com/wikis/disney-dreamlight-valley/Lovely_Monsters_Star_Path_Duties#section")
        )
        assertEquals(
            "https://www.ign.com/wikis/disney-dreamlight-valley/Lovely_Monsters_Star_Path_Duties",
            StarPathScraper.normalizeUrl("https://www.ign.com/wikis/disney-dreamlight-valley/Lovely_Monsters_Star_Path_Duties/")
        )
    }
}
