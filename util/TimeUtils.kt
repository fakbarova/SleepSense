package com.circadianx.sleepsense.util

import java.time.LocalTime

object TimeUtils {
    fun toMinutes(time: LocalTime): Int = time.hour * 60 + time.minute
}

