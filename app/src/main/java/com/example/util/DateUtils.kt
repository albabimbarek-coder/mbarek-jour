package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val arabicMonths = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    private val englishMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val frenchMonths = listOf(
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    )

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthPrefixFormat = SimpleDateFormat("yyyy-MM", Locale.US)

    fun getTodayDate(): String {
        return dbDateFormat.format(Date())
    }

    fun getCurrentMonthPrefix(): String {
        return monthPrefixFormat.format(Date())
    }

    fun formatToDisplayMonth(monthPrefix: String, language: AppLanguage = AppLanguage.AR): String {
        return try {
            val parts = monthPrefix.split("-")
            val year = parts[0]
            val monthIndex = parts[1].toInt() - 1
            val monthName = when (language) {
                AppLanguage.AR -> arabicMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
                AppLanguage.EN -> englishMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
                AppLanguage.FR -> frenchMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
            }
            "$monthName $year"
        } catch (_: Exception) {
            monthPrefix
        }
    }

    fun formatArabicDateWithDayName(dateStr: String, language: AppLanguage = AppLanguage.AR): String {
        return try {
            val date = dbDateFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val monthIndex = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            when (language) {
                AppLanguage.AR -> {
                    val dayName = when (dayOfWeek) {
                        Calendar.SATURDAY -> "السبت"
                        Calendar.SUNDAY -> "الأحد"
                        Calendar.MONDAY -> "الاثنين"
                        Calendar.TUESDAY -> "الثلاثاء"
                        Calendar.WEDNESDAY -> "الأربعاء"
                        Calendar.THURSDAY -> "الخميس"
                        Calendar.FRIDAY -> "الجمعة"
                        else -> ""
                    }
                    val monthName = arabicMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
                    "$dayName، $dayOfMonth $monthName $year"
                }
                AppLanguage.EN -> {
                    val dayName = when (dayOfWeek) {
                        Calendar.SATURDAY -> "Saturday"
                        Calendar.SUNDAY -> "Sunday"
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        else -> ""
                    }
                    val monthName = englishMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
                    "$dayName, $monthName $dayOfMonth, $year"
                }
                AppLanguage.FR -> {
                    val dayName = when (dayOfWeek) {
                        Calendar.SATURDAY -> "Samedi"
                        Calendar.SUNDAY -> "Dimanche"
                        Calendar.MONDAY -> "Lundi"
                        Calendar.TUESDAY -> "Mardi"
                        Calendar.WEDNESDAY -> "Mercredi"
                        Calendar.THURSDAY -> "Jeudi"
                        Calendar.FRIDAY -> "Vendredi"
                        else -> ""
                    }
                    val monthName = frenchMonths.getOrElse(monthIndex) { "${monthIndex + 1}" }
                    "$dayName $dayOfMonth $monthName $year"
                }
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    fun getDayOfWeekShort(dateStr: String, language: AppLanguage = AppLanguage.AR): String {
        return try {
            val date = dbDateFormat.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

            when (language) {
                AppLanguage.AR -> when (dayOfWeek) {
                    Calendar.SATURDAY -> "سبت"
                    Calendar.SUNDAY -> "أحد"
                    Calendar.MONDAY -> "اثنين"
                    Calendar.TUESDAY -> "ثلاثاء"
                    Calendar.WEDNESDAY -> "أربعاء"
                    Calendar.THURSDAY -> "خميس"
                    Calendar.FRIDAY -> "جمعة"
                    else -> ""
                }
                AppLanguage.EN -> when (dayOfWeek) {
                    Calendar.SATURDAY -> "Sat"
                    Calendar.SUNDAY -> "Sun"
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    else -> ""
                }
                AppLanguage.FR -> when (dayOfWeek) {
                    Calendar.SATURDAY -> "Sam"
                    Calendar.SUNDAY -> "Dim"
                    Calendar.MONDAY -> "Lun"
                    Calendar.TUESDAY -> "Mar"
                    Calendar.WEDNESDAY -> "Mer"
                    Calendar.THURSDAY -> "Jeu"
                    Calendar.FRIDAY -> "Ven"
                    else -> ""
                }
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun getWeekDayHeaders(language: AppLanguage = AppLanguage.AR): List<String> {
        return when (language) {
            AppLanguage.AR -> listOf("سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
            AppLanguage.EN -> listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
            AppLanguage.FR -> listOf("Sam", "Dim", "Lun", "Mar", "Mer", "Jeu", "Ven")
        }
    }

    fun getDaysInMonth(monthPrefix: String): List<String> {
        return try {
            val parts = monthPrefix.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val dates = mutableListOf<String>()
            for (day in 1..maxDay) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                dates.add(dbDateFormat.format(cal.time))
            }
            dates
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getPreviousMonthPrefix(monthPrefix: String): String {
        return try {
            val parts = monthPrefix.split("-")
            var year = parts[0].toInt()
            var month = parts[1].toInt() - 1 // 0-based
            month -= 1
            if (month < 0) {
                month = 11
                year -= 1
            }
            String.format(Locale.US, "%04d-%02d", year, month + 1)
        } catch (_: Exception) {
            monthPrefix
        }
    }

    fun getNextMonthPrefix(monthPrefix: String): String {
        return try {
            val parts = monthPrefix.split("-")
            var year = parts[0].toInt()
            var month = parts[1].toInt() - 1 // 0-based
            month += 1
            if (month > 11) {
                month = 0
                year += 1
            }
            String.format(Locale.US, "%04d-%02d", year, month + 1)
        } catch (_: Exception) {
            monthPrefix
        }
    }
}
