import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DocumentsTest : BaseSagresTest() {
    @Test
    fun downloadEnrollment() {
        val callback = instance.login(credential.username, credential.password)
        assertEquals(200, callback.code)
        val file = File.createTempFile("enroll", ".pdf")
        val enrollCall = instance.downloadEnrollment(file)
        assertEquals(200, enrollCall.code)
        assertNotEquals(0, file.length())
    }
}
