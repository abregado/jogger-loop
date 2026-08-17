package com.abregado.joggerloop.data

enum class PulseMode(val jsonValue: String) {
    SINGLE("single"),
    TRIPLE("triple");

    companion object {
        fun fromJsonValue(value: String?): PulseMode =
            entries.find { it.jsonValue == value } ?: SINGLE
    }
}
