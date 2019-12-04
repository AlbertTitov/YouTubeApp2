package com.newfarm.youtubeapp2.presentation

import android.app.Activity
import android.content.Context
import com.github.mrengineer13.snackbar.SnackBar
import com.newfarm.youtubeapp2.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


class Utils {

    companion object {

        @JvmStatic
        fun showSnackBar(activity: Activity, message: String) {
            SnackBar.Builder(activity)
                .withMessage(message)
                .show()
        }

        @JvmStatic
        fun formatPublishedDate(activity: Activity, publishedDate: String): String? {
            var result = Date()
            val df1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            try {
                result = df1.parse(publishedDate)!!
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return getTimeAgo(result, activity)
        }

        @JvmStatic
        fun currentDate(): Date {
            val calendar = Calendar.getInstance()
            return calendar.time
        }

        @JvmStatic
        fun getTimeAgo(date: Date?, ctx: Context): String? {
            if (date == null) {
                return null
            }
            val time = date.time
            val curDate = currentDate()
            val now = curDate.time
            if (time > now || time <= 0) {
                return null
            }
            val dim = getTimeDistanceInMinutes(time)

            val timeAgo = if (dim == 0) {
                "${ctx.resources.getString(R.string.date_util_term_less)} ${ctx.resources
                    .getString(R.string.date_util_unit_minute)}"
            }
            else if (dim == 1 || dim == 21 || dim == 31 || dim == 41) {
                "$dim ${ctx.resources.getString(R.string.date_util_unit_minuta)}"
            }
            else if (dim == 2 || dim == 3 || dim == 4 || dim == 22 || dim == 23 || dim == 24 ||
                dim == 32 || dim == 33 || dim == 34 || dim == 42 || dim == 43 || dim == 44) {
                "$dim ${ctx.resources.getString(R.string.date_util_unit_minute)}"
            }
            else if (dim in 5..20 || dim in 25..30 || dim in 35..40) {
                "$dim ${ctx.resources.getString(R.string.date_util_unit_minutes)}"
            }
            else if (dim in 45..89) {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${ctx.resources
                    .getString(R.string.date_util_unit_hour)}"
            } else if (dim in 90..270) {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${(dim / 60)
                    .toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_hour)}"
            } else if (dim in 271..1439) {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${(dim / 60)
                    .toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_hours)}"
            } else if (dim in 1440..2519) {
                "1 ${ctx.resources.getString(R.string.date_util_unit_daya)}"
            } else if (dim in 2520..6480) {
                "${(dim / 1440).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_day)}"
            } else if (dim in 6481..29000 || dim in 34701..43200) {
                "${(dim / 1440).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_days)}"
            } else if (dim in 29001..30500) {
                "${(dim / 1440).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_daya)}"
            } else if (dim in 30501..34700) {
                "${(dim / 1440).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_day)}"
            } else if (dim in 43201..86399) {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${ctx.resources.getString(R.string.date_util_unit_month)}"
            } else if (dim in 86400..216000) {
                "${(dim / 43200).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_month)}"
            } else if (dim in 216001..492480) {
                "${(dim / 43200).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_months)}"
            } else if (dim in 492481..518400) {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${ctx.resources.getString(R.string.date_util_unit_year)}"
            } else if (dim in 518401..914399) {
                "${ctx.resources.getString(R.string.date_util_prefix_over)} ${ctx.resources.getString(R.string.date_util_unit_year)}"
            } else if (dim in 914400..1051199) {
                "${ctx.resources.getString(R.string.date_util_prefix_almost)} 2 ${ctx.resources.getString(R.string.date_util_unit_years)}"
            } else {
                "${ctx.resources.getString(R.string.date_util_prefix_about)} ${(dim / 525600).toFloat().roundToInt()} ${ctx.resources.getString(R.string.date_util_unit_years)}"
            }
            return "$timeAgo ${ctx.resources.getString(R.string.date_util_suffix)}"
        }

        private fun getTimeDistanceInMinutes(time: Long): Int {
            val timeDistance = currentDate().time - time
            return (abs(timeDistance) / 1000 / 60).toFloat().roundToInt()
        }

        @JvmStatic
        fun getTimeFromString(duration: String): String {
            var time = ""
            var hourexists = false
            var minutesexists = false
            var secondsexists = false
            if (duration.contains("H"))
                hourexists = true
            if (duration.contains("M"))
                minutesexists = true
            if (duration.contains("S"))
                secondsexists = true
            if (hourexists) {
                var hour: String
                hour = duration.substring(
                    duration.indexOf("T") + 1,
                    duration.indexOf("H")
                )
                if (hour.length == 1)
                    hour = "0$hour"
                time += "$hour:"
            }
            if (minutesexists) {
                var minutes: String
                minutes = if (hourexists)
                    duration.substring(
                        duration.indexOf("H") + 1,
                        duration.indexOf("M")
                    )
                else
                    duration.substring(
                        duration.indexOf("T") + 1,
                        duration.indexOf("M")
                    )
                if (minutes.length == 1)
                    minutes = "0$minutes"
                time += "$minutes:"
            } else {
                time += "00:"
            }
            if (secondsexists) {
                var seconds: String
                when {
                    hourexists -> {
                        seconds = if (minutesexists)
                            duration.substring(
                                duration.indexOf("M") + 1,
                                duration.indexOf("S")
                            )
                        else
                            duration.substring(
                                duration.indexOf("H") + 1,
                                duration.indexOf("S")
                            )
                    }
                    minutesexists -> seconds = duration.substring(
                        duration.indexOf("M") + 1,
                        duration.indexOf("S")
                    )
                    else -> seconds = duration.substring(
                        duration.indexOf("T") + 1,
                        duration.indexOf("S")
                    )
                }
                if (seconds.length == 1)
                    seconds = "0$seconds"
                time += seconds
            } else {
                time += "00"
            }
            return time
        }
    }
}