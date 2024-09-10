package com.github.kairaedsch.intellijpyinvoke.common

class PIInfo(val state: PIInfoState, val message: String) {
    companion object {
        fun info(message: String) = PIInfo(PIInfoState.INFO, message)
        fun warn(message: String) = PIInfo(PIInfoState.WARN, message)
        fun error(message: String) = PIInfo(PIInfoState.ERROR, message)
    }
}

enum class PIInfoState(val severity: Int, val keyName: String) {
    INFO(0, "info"),
    WARN(1, "warn"),
    ERROR(2, "error");
}
