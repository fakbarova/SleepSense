package com.circadianx.sleepsense.util

import android.util.Base64

object Base64Utils {
    fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun decode(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP)
}

