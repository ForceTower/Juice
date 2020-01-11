import org.junit.Test

class MessagesTest : BaseSagresTest() {
    @Test
    fun getFromAPI() {
        val me = instance.me()
        val person = me.person!!
        val messages = instance.messages(person.id)
        println(messages.status)
        println(messages.messages!!.size)

        messages.messages!!.forEach {
            println(it.message)
            println("${it.discipline}, ${it.disciplineCode}, ${it.senderName}, ${it.sagresId}")
        }
    }
}
