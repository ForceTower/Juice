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

package com.forcetower.sagres.cookies

import okhttp3.Cookie
import okhttp3.internal.and

import java.io.*

class SerializableCookie : Serializable {
    @Transient
    private var cookie: Cookie? = null

    fun encode(cookie: Cookie): String? {
        this.cookie = cookie

        val byteArrayOutputStream = ByteArrayOutputStream()
        var objectOutputStream: ObjectOutputStream? = null

        try {
            objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
            objectOutputStream.writeObject(this)
        } catch (e: IOException) {
            return null
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close()
                } catch (ignored: IOException) {
                }

            }
        }

        return byteArrayToHexString(byteArrayOutputStream.toByteArray())
    }

    fun decode(encodedCookie: String): Cookie? {

        val bytes = hexStringToByteArray(encodedCookie)
        val byteArrayInputStream = ByteArrayInputStream(
            bytes
        )

        var cookie: Cookie? = null
        try {
            ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                cookie = (objectInputStream.readObject() as SerializableCookie).cookie
            }
        } catch (ignored: IOException) {
        } catch (ignored: ClassNotFoundException) {
        }

        return cookie
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookie!!.name)
        out.writeObject(cookie!!.value)
        out.writeLong(if (cookie!!.persistent) cookie!!.expiresAt else NON_VALID_EXPIRES_AT)
        out.writeObject(cookie!!.domain)
        out.writeObject(cookie!!.path)
        out.writeBoolean(cookie!!.secure)
        out.writeBoolean(cookie!!.httpOnly)
        out.writeBoolean(cookie!!.hostOnly)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val builder = Cookie.Builder()

        builder.name(`in`.readObject() as String)

        builder.value(`in`.readObject() as String)

        val expiresAt = `in`.readLong()
        if (expiresAt != NON_VALID_EXPIRES_AT) {
            builder.expiresAt(expiresAt)
        }

        val domain = `in`.readObject() as String
        builder.domain(domain)

        builder.path(`in`.readObject() as String)

        if (`in`.readBoolean())
            builder.secure()

        if (`in`.readBoolean())
            builder.httpOnly()

        if (`in`.readBoolean())
            builder.hostOnlyDomain(domain)

        cookie = builder.build()
    }

    companion object {
        private val TAG = SerializableCookie::class.java.simpleName

        private const val serialVersionUID = -8594045714036645534L

        /**
         * Using some super basic byte array &lt;-&gt; hex conversions so we don't
         * have to rely on any large Base64 libraries. Can be overridden if you
         * like!
         *
         * @param bytes byte array to be converted
         * @return string containing hex values
         */
        private fun byteArrayToHexString(bytes: ByteArray): String {
            val sb = StringBuilder(bytes.size * 2)
            for (element in bytes) {
                val v = element and 0xff
                if (v < 16) {
                    sb.append('0')
                }
                sb.append(Integer.toHexString(v))
            }
            return sb.toString()
        }

        /**
         * Converts hex values from strings to byte array
         *
         * @param hexString string of hex-encoded values
         * @return decoded byte array
         */
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character
                    .digit(hexString[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private val NON_VALID_EXPIRES_AT = -1L
    }

}