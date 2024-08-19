package com.github.kairaedsch.intellijpyinvoke.backend

import com.github.kairaedsch.intellijpyinvoke.common.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import kotlin.text.RegexOption.MULTILINE

fun scan(project: Project, runMode: PIRunMode, onProgress: (Double) -> Unit): PIProject {
    val modules = ModuleManager.getInstance(project).modules
    val folders = modules.flatMapIndexed { moduleId, module ->
        val folderPaths = determineFolderPaths(module)
        folderPaths.mapIndexed { folderId, folder ->
            val piFolder = scanFolder(module, runMode, folder.path)
            val moduleCount = (modules.size).toDouble()
            val folderCount = (folderPaths.size).toDouble()
            onProgress(moduleId / moduleCount + ((1.0 / moduleCount) * (folderId + 1.0) / folderCount))
            piFolder
        }
    }
    return PIProject(runMode, folders)
}

private fun determineFolderPaths(module: Module): List<VirtualFile> {
    fileLogger().info("Searching trough module: ${module.name}")
    val moduleScope = module.moduleContentScope

    val files = ApplicationManager.getApplication().runReadAction<List<VirtualFile>> {
        val taskFiles = FilenameIndex.getVirtualFilesByName("tasks.py", moduleScope)
        val taskFolders = FilenameIndex.getVirtualFilesByName("tasks", moduleScope)
        taskFiles + taskFolders
    }

    val folders = files.flatMap { file ->
        val parent = file.parent ?: return@flatMap emptyList()
        listOf(parent)
    }
    fileLogger().info("Found folders: ${folders.joinToString { it.path }}")
    return folders
}

private fun scanFolder(module: Module, runMode: PIRunMode, path: String): PIFolder {
    val infos = ArrayList<PIInfo>()
    val runner = PIDirectRunner { infos.add(it) }
    val list = runner.runPyInvoke(runMode, module, path, "--list") ?: return PIFolder(module, path, infos, emptyList())
    val taskPattern = """^\s*(\w+(?:\.\w+)*)\s*${'$'}""".toRegex(MULTILINE)
    val matches = taskPattern.findAll(list)
    val tasks = matches.map { task ->
        val fullName = task.groupValues[1]
        PITask(module, path, fullName)
    }.toList()
    return PIFolder(module, path, infos, tasks)
}
