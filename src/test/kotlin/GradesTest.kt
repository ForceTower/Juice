import com.forcetower.sagres.database.model.SagresGrade
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.parsers.SagresGradesParser
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GradesTest : BaseSagresTest() {
    @Test
    fun extractCommonGrades() {
        val content = File("assets/test/grades/common1.html").readText()
        val result = File("assets/test/grades/common1_result.json").readText()
        val expectedSemesters = File("assets/test/grades/common1_semesters.json").readText()
        val document = content.asDocument()

        val canExtract = SagresGradesParser.canExtractGrades(document)
        assertTrue(canExtract)

        val variants = SagresGradesParser.extractCourseVariants(document)
        assertTrue(variants.isEmpty())

        val selected = SagresGradesParser.getSelectedSemester(document)
        assertNotNull(selected)
        assertEquals(true, selected!!.first)
        assertEquals(1000000861, selected.second)

        val semesters = SagresGradesParser.extractSemesterCodes(document)
        val tokenSemester = object : TypeToken<List<Pair<Long, String>>> () { }.type
        val expectedSemester = gson.fromJson<List<Pair<Long, String>>>(expectedSemesters, tokenSemester)
        expectedSemester.forEachIndexed { index, it ->
            val pair = semesters[index]
            assertTrue(pair.first == it.first)
            assertTrue(pair.second == it.second)
        }

        val grades = SagresGradesParser.extractGrades(document, 1000000861)
        val token = object : TypeToken<List<SagresGrade>> () { }.type
        val expected = gson.fromJson<List<SagresGrade>>(result, token)
        assertTrue(grades.containsAll(expected))
        assertTrue(expected.containsAll(grades))
    }

    @Test
    fun uescGradeExtractionBug1() {
        val result = File("assets/test/grades/uesc_bug_1_result.json").readText()
        val content = File("assets/test/grades/uesc_bug_1.html").readText()
        val document = content.asDocument()

        val canExtract = SagresGradesParser.canExtractGrades(document)
        assertTrue(canExtract)

        val variants = SagresGradesParser.extractCourseVariants(document)
        assertTrue(variants.isEmpty())

        val selected = SagresGradesParser.getSelectedSemester(document)
        assertNotNull(selected)
        assertEquals(true, selected!!.first)
        assertEquals(2000000276, selected.second)

        val grades = SagresGradesParser.extractGrades(document, 2000000276)
        val token = object : TypeToken<List<SagresGrade>> () { }.type
        val expected = gson.fromJson<List<SagresGrade>>(result, token)
        assertTrue(grades.containsAll(expected))
        assertTrue(expected.containsAll(grades))
    }
}