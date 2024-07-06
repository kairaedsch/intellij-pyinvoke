package com.github.kairaedsch.intellijpyinvoke.scanner

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex

class PIProject(private val project: Project) {
    val pyInvokeFolders = determineFolders()

    private fun determineFolders() = ModuleManager.getInstance(project).modules.flatMap { module ->
        determineFolderPaths(module)
            .map { folder -> PIFolder(module, folder.path) }
            .filter { it.tasks.isNotEmpty() }
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
}
