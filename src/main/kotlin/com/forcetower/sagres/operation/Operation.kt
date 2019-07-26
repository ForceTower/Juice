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

package com.forcetower.sagres.operation

import com.google.gson.Gson
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import timber.log.debug
import java.util.concurrent.Executor

abstract class Operation<Result : BaseCallback<*>> constructor(private val executor: Executor?) {
    private val _result: PublishSubject<Result> = PublishSubject.create()
    val result: Subject<Result>
        get() = _result

    protected val gson: Gson = Gson()
    lateinit var finishedResult: Result
        protected set
    var isSuccess: Boolean = false
        protected set

    protected fun perform() {
        if (executor != null) {
            Timber.debug { "Executing on Executor" }
            executor.execute { this.execute() }
        } else {
            Timber.debug { "Executing on Current Thread" }
            this.execute()
        }
    }

    protected abstract fun execute()

    fun publishProgress(value: Result) {
        finishedResult = value
        result.onNext(value)
    }
}
