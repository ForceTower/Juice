import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.database.model.SagresCredential
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import org.junit.BeforeClass

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

abstract class BaseSagresTest {
    companion object {
        lateinit var instance: SagresNavigator
        lateinit var credential: SagresCredential
        lateinit var gson: Gson

        @BeforeClass
        @JvmStatic
        fun init() {
            gson = GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()

            val environment = File("environment.json").readText()
            credential = gson.fromJson(environment, SagresCredential::class.java)
            SagresNavigator.initialize(null)
            instance = SagresNavigator.instance
        }

        protected fun toJson(source: Any) = gson.toJson(source)
    }
}
