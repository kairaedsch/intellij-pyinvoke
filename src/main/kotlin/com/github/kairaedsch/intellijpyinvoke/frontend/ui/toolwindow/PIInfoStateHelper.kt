package com.github.kairaedsch.intellijpyinvoke.frontend.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.common.PIInfoState
import com.github.kairaedsch.intellijpyinvoke.common.PIInfoState.*
import com.intellij.icons.ExpUiIcons.Status.*
import javax.swing.Icon


fun infoStateIcon(state: PIInfoState): Icon {
    return when (state) {
        INFO -> InfoOutline
        WARN -> WarningOutline
        ERROR -> ErrorOutline
    }
}
