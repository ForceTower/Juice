import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.io.File

class DocumentsTest : BaseSagresTest() {
    @Test
    fun downloadEnrollment() = runTest {
        val callback = instance.login(credential.username, credential.password)
        Assert.assertEquals(200, callback.code)
        val file = File.createTempFile("enroll", ".pdf")
        val enrollCall = instance.downloadEnrollment(file)
        Assert.assertEquals(200, enrollCall.code)
        Assert.assertNotEquals(0, file.length())
    }
}