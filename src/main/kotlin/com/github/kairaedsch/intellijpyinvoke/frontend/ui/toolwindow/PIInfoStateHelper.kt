package com.github.kairaedsch.intellijpyinvoke.frontend.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.common.PIInfoState
import com.github.kairaedsch.intellijpyinvoke.common.PIInfoState.*
import com.intellij.icons.AllIcons.General.Note
import com.intellij.icons.AllIcons.General.Warning
import com.intellij.icons.AllIcons.General.Error
import javax.swing.Icon


fun infoStateIcon(state: PIInfoState): Icon {
    return when (state) {
        INFO -> Note
        WARN -> Warning
        ERROR -> Error
    }
}
