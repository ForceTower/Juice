import com.forcetower.sagres.operation.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagesTest : BaseSagresTest() {
    @Test
    fun getFromAPI() {
        val me = instance.me()
        val person = me.person!!
        val messages = instance.messages(person.id)

        assertEquals(Status.SUCCESS, messages.status)
        assertNotNull(messages.messages)
        assertEquals(20, messages.messages!!.size)
    }

    @Test
    fun fetchAll() {
        val me = instance.me()
        val person = me.person!!
        val messages = instance.messages(person.id, fetchAll = true)

        assertEquals(Status.SUCCESS, messages.status)
        assertNotNull(messages.messages)
        assertTrue(messages.messages!!.size > 20)
    }

    @Test
    fun cachingFetch() {
        val me = instance.me()
        val person = me.person!!

        val start = System.currentTimeMillis()
        val messages = instance.messages(person.id)
        val end = System.currentTimeMillis()

        assertEquals(Status.SUCCESS, messages.status)
        val diff = end - start

        println("First timer: $diff")

        val startCached = System.currentTimeMillis()
        val messagesCached = instance.messages(person.id)
        val endCached = System.currentTimeMillis()

        assertEquals(Status.SUCCESS, messagesCached.status)
        val diffCached = endCached - startCached

        println("Cached timer: $diffCached")
        assertTrue(diffCached < diff)
    }
}
