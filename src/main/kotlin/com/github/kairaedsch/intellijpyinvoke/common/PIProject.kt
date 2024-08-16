package com.github.kairaedsch.intellijpyinvoke.common

import com.intellij.openapi.module.Module
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.sdk.basePath

class PIProject(
    val runMode: PIRunMode,
    val pyInvokeFolders: List<PIFolder>,
)

class PIFolder(
    val module: Module,
    val path: String,
    val infos: List<PIInfo>,
    val tasks: List<PITask>,
) {
    val pathFromModule get() = path
        .removePrefix(module.basePath ?: "")
        .removePrefix("/")

    fun findCompatiblePiTask(function: PyFunction): PITask? {
        val functionName = function.name ?: return null
        val path = function.containingFile.virtualFile.path
        return tasks.find { task -> task.potentialFilePaths.any { it == path } && task.name == functionName }
    }
}

class PITask(
    val module: Module,
    val path: String,
    val fullName: String,
    val parameters: List<String> = emptyList(),
) {
    val name get() = fullName.split(".").last()
    val parents get() = fullName.split(".").dropLast(1)

    val potentialFilePaths get() = if (parents.isEmpty()) listOf(
        "$path/tasks.py",
        "$path/tasks/__init__.py",
    ) else listOf(
        "$path/${parents.joinToString("/")}.py",
        "$path/${parents.joinToString("/")}/__init__.py",
        "$path/tasks/${parents.joinToString("/")}.py",
        "$path/tasks/${parents.joinToString("/")}/__init__.py",
    )
}
