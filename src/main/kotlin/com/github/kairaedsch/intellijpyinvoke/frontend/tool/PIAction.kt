package com.github.kairaedsch.intellijpyinvoke.frontend.tool

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.github.kairaedsch.intellijpyinvoke.backend.findMatchingPsiElement
import com.github.kairaedsch.intellijpyinvoke.frontend.run.*
import com.intellij.icons.ExpUiIcons.General.Edit
import com.intellij.icons.ExpUiIcons.Run.Debug
import com.intellij.icons.ExpUiIcons.Run.Run
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.plugins.terminal.TerminalIcons
import javax.swing.Icon

typealias TaskFinder = (callback: (task: PITask) -> Unit) -> Unit

enum class PIAction(private val title: (name: String) -> String, private val icon: Icon? = null) {
    TERMINAL_RUN({ name -> PIBundle.message("run_terminal_task", name) }, TerminalIcons.Command) {
        override fun run(piTask: PITask) {
            try {
                run(createTerminalRunConfiguration(piTask))
            } catch (e: LinkageError) {
                runInTerminalTab(piTask)
            }
        }
    },
    TERMINAL_MODIFY({ PIBundle.message("modify_run_conf") }) {
        override fun run(piTask: PITask) = modify(createTerminalRunConfiguration(piTask))
    },
    SDK_RUN({ name -> PIBundle.message("run_task", name) }, Run) {
        override fun run(piTask: PITask) = run(createSdkRunConfiguration(piTask))
    },
    SDK_DEBUG({ name -> PIBundle.message("debug_task", name) }, Debug) {
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
                    element is PyFunction && element.name  == piTask.fullName
                } as PyFunction? ?: return
                OpenFileDescriptor(project, file, pyFunction.textOffset).navigate(true)
            }
        }
    };

    abstract fun run(piTask: PITask)

    private fun getAction(name: String, taskFinder: TaskFinder): AnAction = object : AnAction({ this.title.invoke(name) }, this.icon) {
        override fun actionPerformed(e: AnActionEvent) {
            taskFinder { task -> this@PIAction.run(task)}
        }
    }

    companion object Factory {
        fun createActionsList(name: String, jumpToSource: Boolean, taskFinder: TaskFinder): Array<AnAction> {
            val actions = arrayListOf(
                TERMINAL_RUN.getAction(name, taskFinder),
                TERMINAL_MODIFY.getAction(name, taskFinder),
                Separator.create(),
                SDK_RUN.getAction(name, taskFinder),
                SDK_DEBUG.getAction(name, taskFinder),
                SDK_MODIFY.getAction(name, taskFinder),
            )
            if (jumpToSource) {
                actions += listOf(
                    Separator.create(),
                    JUMP_TO_SOURCE.getAction(name, taskFinder),
                )
            }
            return actions.toTypedArray()
        }
    }
}
