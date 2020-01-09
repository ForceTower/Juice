import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresBasicParser
import com.forcetower.sagres.utils.ConnectedStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/*
 * This file is part of the UNES Open Source Project.
 * UNES is licensed under the GNU GPLv3.
 *
 * Copyright (c) 2019. João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@ExperimentalCoroutinesApi
class LoginTest : BaseSagresTest() {
    @Before
    fun sessionClean() {
        instance.clearSession()
    }

    @Test
    fun loginCorrectly() = runBlockingTest {
        val callback = instance.login(credential.username, credential.password)
        assertEquals(200, callback.code)
        assertEquals(Status.SUCCESS, callback.status)
        assertNotNull(callback.document)
        assertEquals(ConnectedStates.CONNECTED, SagresBasicParser.isConnected(callback.document))
        val start = instance.startPage()
        println("is demand open? ${start.isDemandOpen}")
        assertEquals("joão paulo santos sena", SagresBasicParser.getName(callback.document)?.toLowerCase())
    }

    @Test
    fun loginIncorrectly() = runBlockingTest {
        val callback = instance.login("johnwickdauefs", "eu virei um cachorro da uefs")
        assertEquals(401, callback.code)
        assertEquals(Status.INVALID_LOGIN, callback.status)
    }
}
