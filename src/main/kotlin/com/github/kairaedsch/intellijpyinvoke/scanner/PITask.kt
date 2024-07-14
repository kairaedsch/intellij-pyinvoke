package com.github.kairaedsch.intellijpyinvoke.scanner

import com.intellij.openapi.module.Module

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
