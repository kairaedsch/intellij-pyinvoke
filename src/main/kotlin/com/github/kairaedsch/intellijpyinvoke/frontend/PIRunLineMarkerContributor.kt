package com.github.kairaedsch.intellijpyinvoke.frontend

import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.frontend.tool.PIAction.Factory.createActionsList
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons.Actions.Execute
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFunction

class PIRunLineMarkerContributor: RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (!element.language.isKindOf(PythonLanguage.INSTANCE)) return null
        if (element.elementType != PyTokenTypes.DEF_KEYWORD) return null

        val pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction::class.java) ?: return null
        val decorators = pyFunction.decoratorList?.decorators ?: return null
        if (decorators.none { decorator -> decorator.name == "task" }) return null
        return Info(Execute, createActionsList({
            element.project.service<PIService>().getTask(pyFunction)
        }, false), { "PyInvoke" })
    }
}
