package com.github.kairaedsch.intellijpyinvoke.scanner

import com.github.kairaedsch.intellijpyinvoke.run.runPyInvoke
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.sdk.basePath
import kotlin.text.RegexOption.MULTILINE

class PIFolder(
    val module: Module,
    val path: String,
) {
    val tasks = scanPyInvokeFolder()
    val pathFromModule get() = path.removePrefix(module.basePath ?: "")

    private fun scanPyInvokeFolder(): List<PITask> {
        val list = runPyInvoke(module, path, "--list") ?: return emptyList()
        val taskPattern = """^\s*(\w+(?:\.\w+)*)\s*${'$'}""".toRegex(MULTILINE)
        val matches = taskPattern.findAll(list)
        return matches.map { task ->
            val fullName = task.groupValues[1]
            PITask(module, path, fullName)
        }.toList()
    }

    fun findCompatiblePiTask(function: PyFunction): PITask? {
        val module = ModuleUtilCore.findModuleForPsiElement(function) ?: return null
        val functionName = function.name ?: return null
        val path = function.containingFile.virtualFile.path

        return tasks.find { task -> task.module == module && task.potentialFilePaths.any { it == path } && task.name == functionName }
    }
}
