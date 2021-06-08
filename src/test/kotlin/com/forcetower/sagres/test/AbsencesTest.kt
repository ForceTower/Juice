package com.forcetower.sagres.test

import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.parsers.SagresMissedClassesParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class AbsencesTest : BaseSagresTest() {
    @Test
    fun common0() {
        val content = File("assets/test/absences/common0.html").readText()
        val document = content.asDocument()
        val missed = SagresMissedClassesParser.extractMissedClasses(document, 1)
        assertFalse(missed.first)
        assertEquals(4, missed.second.size)
        val split = missed.second.groupBy { it.disciplineCode }
        assertEquals(2, split["EXA841"]?.size)
        assertEquals(2, split["EXA852"]?.size)
    }

    @Test
    fun common1() {
        val content = File("assets/test/absences/common1.html").readText()
        val document = content.asDocument()
        val missed = SagresMissedClassesParser.extractMissedClasses(document, 1)
        assertFalse(missed.first)
        assertEquals(60, missed.second.size)
        val split = missed.second.groupBy { it.disciplineCode }
        assertEquals(2, split["EXA862"]?.size)
        assertEquals(null, split["TEC402"]?.size)
        assertEquals(6, split["TEC434"]?.size)
        assertEquals(null, split["TEC451"]?.size)
        assertEquals(null, split["TEC499"]?.size)
        assertEquals(52, split["FIS110"]?.size)

        assertNotNull(split["FIS110"])
        val grouping = split["FIS110"]?.groupBy { it.group }!!
        assertEquals(16, grouping["Prática"]?.size)
        assertEquals(36, grouping["Teórica"]?.size)
    }
}
