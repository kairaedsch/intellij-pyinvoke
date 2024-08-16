package com.github.kairaedsch.intellijpyinvoke.common

import com.github.kairaedsch.intellijpyinvoke.PIBundle


enum class PIRunMode(val title: String) {
    MODE_TERMINAL_RUN(PIBundle.message("terminal_run_mode")),
    MODE_SDK_RUN(PIBundle.message("run_mode")),
    MODE_SDK_DEBUG(PIBundle.message("debug_mode"));
}
