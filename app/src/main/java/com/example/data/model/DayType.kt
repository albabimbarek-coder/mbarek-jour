package com.example.data.model

enum class DayType(val titleAr: String) {
    WORK("يوم عمل"),
    OFF("يوم راحة"),
    VACATION("إجازة رسمية / سنوية"),
    SICK("إجازة مرضية");

    companion object {
        fun fromString(value: String): DayType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: WORK
        }
    }
}
