package com.github.kairaedsch.intellijpyinvoke.run

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.intellij.icons.ExpUiIcons.Run.Debug
import com.intellij.icons.ExpUiIcons.Run.Run
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.plugins.terminal.TerminalIcons
import javax.swing.Icon


enum class PIRunMode(val title: String, val icon: Icon) {
    MODE_TERMINAL_RUN(PIBundle.message("terminal_run_mode"), TerminalIcons.Command),
    MODE_SDK_RUN(PIBundle.message("run_mode"), Run),
    MODE_SDK_DEBUG(PIBundle.message("debug_mode"), Debug);

    fun asAction(action: () -> Unit): AnAction = object : AnAction({ this.title }, this.icon) {
        override fun actionPerformed(e: AnActionEvent) {
            action()
        }
    }
}
