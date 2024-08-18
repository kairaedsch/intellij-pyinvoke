package com.github.kairaedsch.intellijpyinvoke.frontend

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.github.kairaedsch.intellijpyinvoke.frontend.tool.PIAction.Factory.createActionsList
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons.Actions.Execute
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType.ERROR
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFunction
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

class PIRunLineMarkerContributor: RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (!element.language.isKindOf(PythonLanguage.INSTANCE)) return null
        if (element.elementType != PyTokenTypes.DEF_KEYWORD) return null

        val pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction::class.java) ?: return null
        val decorators = pyFunction.decoratorList?.decorators ?: return null
        if (decorators.none { decorator -> decorator.name == "task" }) return null
        return Info(Execute, createActionsList(pyFunction.name ?: "",false) { callback ->
            taskFinder(pyFunction, callback)
        }, { "PyInvoke" })
    }

    private fun taskFinder(element: PyFunction, callback: (task: PITask) -> Unit) {
        val piService = element.project.service<PIService>()
        val task = piService.pyInvokeProject.get()?.getTask(element)
        if (task != null) {
            callback(task)
            return
        }
        piService.refreshing.addListener(object : ChangeListener<Boolean> {
            override fun changed(observable: ObservableValue<out Boolean>, oldValue: Boolean, newValue: Boolean) {
                if (oldValue && !newValue) {
                    piService.refreshing.removeListener(this)
                    val task = piService.pyInvokeProject.get()?.getTask(element)
                    if (task != null) callback(task)
                    else {
                        Notification(
                            "com.github.kairaedsch.intellijpyinvoke.notifications",
                            PIBundle.message("notification.task_not_found.title"),
                            PIBundle.message("notification.task_not_found.description"),
                            ERROR,
                        )
                    }
                }
            }})
        piService.refresh()
    }
}
