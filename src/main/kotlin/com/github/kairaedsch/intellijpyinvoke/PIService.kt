package com.github.kairaedsch.intellijpyinvoke

import com.github.kairaedsch.intellijpyinvoke.scanner.PIProject
import com.github.kairaedsch.intellijpyinvoke.scanner.PITask
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType.ERROR
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyFunction
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@Service(Service.Level.PROJECT)
class PIService(private val project: Project): Disposable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val _pyInvokeProject: SimpleObjectProperty<PIProject?> = SimpleObjectProperty(null)
    val pyInvokeProject: ReadOnlyObjectProperty<PIProject?> get() = _pyInvokeProject

    fun refresh() {
        launch {
            withBackgroundProgress(project, PIBundle.message("background_scan", project.name), true) {
                val piProject = PIProject(project)
                ApplicationManager.getApplication().invokeLater {
                    _pyInvokeProject.set(piProject)
                }
            }
        }
    }

    fun getTask(element: PyFunction): PITask? {
        for (folder in pyInvokeProject.get()?.pyInvokeFolders ?: emptyList()) {
            val task = folder.findCompatiblePiTask(element) ?: continue
            return task
        }
        Notifications.Bus.notify(
            Notification(
                "com.github.kairaedsch.intellijpyinvoke.notifications",
                PIBundle.message("notification.task_not_found.title"),
                PIBundle.message("notification.task_not_found.description"),
                ERROR,
            ))
        return null
    }

    override fun dispose() = job.cancel()
}
