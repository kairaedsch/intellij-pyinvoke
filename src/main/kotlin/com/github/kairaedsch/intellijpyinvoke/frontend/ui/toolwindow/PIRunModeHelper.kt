package com.github.kairaedsch.intellijpyinvoke.frontend.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode.*
import com.intellij.icons.AllIcons.Actions.StartDebugger
import com.intellij.icons.AllIcons.Actions.Execute
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import org.jetbrains.plugins.terminal.TerminalIcons
import javax.swing.Icon


fun runModeAsAction(mode: PIRunMode, isSelected: () -> Boolean, select: (Boolean) -> Unit): AnAction = object : ToggleAction({ mode.title }, runModeIcon(mode)) {
    override fun isSelected(e: AnActionEvent): Boolean {
        return isSelected()
    }
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        select(state)
    }
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}

fun runModeIcon(mode: PIRunMode): Icon {
    return when (mode) {
        MODE_TERMINAL_RUN -> TerminalIcons.Command
        MODE_SDK_RUN -> Execute
        MODE_SDK_DEBUG -> StartDebugger
    }
}
