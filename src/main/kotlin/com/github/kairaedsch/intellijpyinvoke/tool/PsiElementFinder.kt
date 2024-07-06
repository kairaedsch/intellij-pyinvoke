package com.github.kairaedsch.intellijpyinvoke.tool

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiManager

fun findMatchingPsiElement(project: Project, file: VirtualFile, matcher: (PsiElement) -> Boolean): PsiElement? {
    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return null
    var targetElement: PsiElement? = null
    psiFile.accept(object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if (targetElement != null) return
            if (matcher(element)) targetElement = element
            element.acceptChildren(this)
        }
    })
    return targetElement
}
