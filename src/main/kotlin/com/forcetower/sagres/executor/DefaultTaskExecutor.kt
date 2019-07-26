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
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultTaskExecutor : TaskExecutor() {

    private val mLock = Any()

    private val mDiskIO = Executors.newFixedThreadPool(2, object : ThreadFactory {
        private val THREAD_NAME_STEM = "unes_disk_io_%d"

        private val mThreadId = AtomicInteger(0)

        override fun newThread(r: Runnable): Thread {
            val t = Thread(r)
            t.name = String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement())
            return t
        }
    })

    private val mNetworkIO = Executors.newFixedThreadPool(8, object : ThreadFactory {
        private val THREAD_NAME_STEM = "unes_net_io_%d"

        private val mThreadId = AtomicInteger(0)

        override fun newThread(r: Runnable): Thread {
            val t = Thread(r)
            t.name = String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement())
            return t
        }
    })

    override fun executeOnDiskIO(runnable: Runnable) {
        mDiskIO.execute(runnable)
    }

    override fun executeOnNetworkIO(runnable: Runnable) {
        mNetworkIO.execute(runnable)
    }
}
