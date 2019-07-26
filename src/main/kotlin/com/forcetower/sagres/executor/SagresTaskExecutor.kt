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

package com.forcetower.sagres.executor

import androidx.annotation.RestrictTo
import io.reactivex.annotations.NonNull

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SagresTaskExecutor private constructor() : TaskExecutor() {
    private var mDelegate: TaskExecutor
    private val mDefaultTaskExecutor: TaskExecutor

    init {
        mDefaultTaskExecutor = DefaultTaskExecutor()
        mDelegate = mDefaultTaskExecutor
    }

    /**
     * Sets a delegate to handle task execution requests.
     *
     *
     * If you have a common executor, you can set it as the delegate and App Toolkit components will
     * use your executors. You may also want to use this for your tests.
     *
     *
     * Calling this method with `null` sets it to the default TaskExecutor.
     *
     * @param taskExecutor The task executor to handle task requests.
     */
    fun setDelegate(taskExecutor: TaskExecutor?) {
        mDelegate = taskExecutor ?: mDefaultTaskExecutor
    }

    override fun executeOnDiskIO(runnable: Runnable) {
        mDelegate.executeOnDiskIO(runnable)
    }

    override fun executeOnNetworkIO(@NonNull runnable: Runnable) {
        mDelegate.executeOnNetworkIO(runnable)
    }

    companion object {
        @Volatile
        private lateinit var sInstance: SagresTaskExecutor

        val ioThreadExecutor = { command: Runnable -> instance.executeOnDiskIO(command) }
        val networkThreadExecutor = { command: Runnable -> instance.executeOnNetworkIO(command) }

        /**
         * Returns an instance of the task executor.
         *
         * @return The singleton SagresTaskExecutor.
         */
        val instance: SagresTaskExecutor
            @NonNull
            get() {
                if (::sInstance.isInitialized) {
                    return sInstance
                }
                synchronized(SagresTaskExecutor::class.java) {
                    if (!::sInstance.isInitialized) {
                        sInstance = SagresTaskExecutor()
                    }
                }
                return sInstance
            }
    }
}