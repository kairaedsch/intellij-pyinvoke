package com.github.kairaedsch.intellijpyinvoke.frontend.tool

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.github.kairaedsch.intellijpyinvoke.backend.findMatchingPsiElement
import com.github.kairaedsch.intellijpyinvoke.frontend.run.*
import com.intellij.icons.ExpUiIcons.General.Edit
import com.intellij.icons.ExpUiIcons.Run.Debug
import com.intellij.icons.ExpUiIcons.Run.Run
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.plugins.terminal.TerminalIcons
import javax.swing.Icon


enum class PIAction(private val title: (piTask: PITask?) -> String, private val icon: Icon? = null) {
    TERMINAL_RUN({ pt -> PIBundle.message("run_terminal_task", pt?.name ?: "") }, TerminalIcons.Command) {
        override fun run(piTask: PITask) = run(createTerminalRunConfiguration(piTask))
    },
    TERMINAL_MODIFY({ PIBundle.message("modify_run_conf") }) {
        override fun run(piTask: PITask) = modify(createTerminalRunConfiguration(piTask))
    },
    SDK_RUN({ pt -> PIBundle.message("run_task", pt?.name ?: "") }, Run) {
        override fun run(piTask: PITask) = run(createSdkRunConfiguration(piTask))
    },
    SDK_DEBUG({ pt -> PIBundle.message("debug_task", pt?.name ?: "") }, Debug) {
        override fun run(piTask: PITask) = debug(createSdkRunConfiguration(piTask))
    },
    SDK_MODIFY({ PIBundle.message("modify_run_conf") }) {
        override fun run(piTask: PITask) = modify(createTerminalRunConfiguration(piTask))
    },
    JUMP_TO_SOURCE({ PIBundle.message("to_source") }, Edit) {
        override fun run(piTask: PITask) {
            val project = piTask.module.project
            for (path in piTask.potentialFilePaths) {
                val file = LocalFileSystem.getInstance().findFileByPath(path) ?: continue
                FileEditorManager.getInstance(project).openFile(file, true)
                val pyFunction = findMatchingPsiElement(project, file) { element ->
                    element is PyFunction && element.name == piTask.fullName
                } as PyFunction? ?: return
                OpenFileDescriptor(project, file, pyFunction.textOffset).navigate(true)
            }
        }
    };

    abstract fun run(piTask: PITask)

    fun getAction(getPiTask: () -> PITask?): AnAction = object : AnAction({ this.title.invoke(getPiTask()) }, this.icon) {
        override fun actionPerformed(e: AnActionEvent) {
            val piTask = getPiTask() ?: return
            this@PIAction.run(piTask)
        }
    }

    companion object Factory {
        fun createActionsList(getPiTask: () -> PITask?, jumpToSource: Boolean = true): Array<AnAction> {
            val actions = arrayListOf(
                TERMINAL_RUN.getAction(getPiTask),
                TERMINAL_MODIFY.getAction(getPiTask),
                Separator.create(),
                SDK_RUN.getAction(getPiTask),
                SDK_DEBUG.getAction(getPiTask),
                SDK_MODIFY.getAction(getPiTask),
            )
            if (jumpToSource) {
                actions += listOf(
                    Separator.create(),
                    JUMP_TO_SOURCE.getAction(getPiTask),
                )
            }
            return actions.toTypedArray()
        }
    }
}
