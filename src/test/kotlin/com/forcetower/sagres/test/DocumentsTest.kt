package com.forcetower.sagres.test

import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File

class DocumentsTest : BaseSagresTest() {
    @Test
    fun downloadEnrollment() = runBlockingTest {
        val callback = instance.login(credential.username, credential.password)
        assertEquals(200, callback.code)
        val file = File.createTempFile("enroll", ".pdf")
        val enrollCall = instance.downloadEnrollment(file)
        assertEquals(200, enrollCall.code)
        assertNotEquals(0, file.length())
    }
}
