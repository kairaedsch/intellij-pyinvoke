package com.github.kairaedsch.intellijpyinvoke.ui

import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.run.PIAction.Factory.createActionsList
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons.Actions.Execute
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement

import com.intellij.openapi.diagnostic.thisLogger
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFunction

class PIRunLineMarkerContributor: RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        thisLogger().info("PyInvokeRunLineMarkerContributor.getInfo$element")
        if (!element.language.isKindOf(PythonLanguage.INSTANCE)) return null
        if (element !is PyFunction) return null
        val decorators = element.decoratorList?.decorators ?: return null
        if (decorators.none { decorator -> decorator.name == "task" }) return null

        return Info(Execute, createActionsList({
            element.project.service<PIService>().getTask(element)
        }, false), { "PyInvoke" })
    }
}
