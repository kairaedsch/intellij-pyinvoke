package com.github.kairaedsch.intellijpyinvoke

import com.github.kairaedsch.intellijpyinvoke.backend.scan
import com.github.kairaedsch.intellijpyinvoke.common.PIProject
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode.MODE_TERMINAL_RUN
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import javafx.beans.property.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Service(Service.Level.PROJECT)
class PIService(private val project: Project): Disposable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val _pyInvokeProject: SimpleObjectProperty<PIProject?> = SimpleObjectProperty(null)
    val pyInvokeProject: ReadOnlyObjectProperty<PIProject?> get() = _pyInvokeProject
    private val _refreshingProgress: SimpleObjectProperty<Double> = SimpleObjectProperty(null)
    val refreshingProgress: ReadOnlyObjectProperty<Double?> get() = _refreshingProgress
    private val _refreshing: SimpleBooleanProperty = SimpleBooleanProperty().apply {
        bind(_refreshingProgress.isNotNull)
    }
    val refreshing: ReadOnlyBooleanProperty get() = _refreshing

    fun refresh(runMode: PIRunMode? = null) {
        _refreshingProgress.set(0.0)
        FileDocumentManager.getInstance().saveAllDocuments()
        val realRunMode = runMode ?: this.runMode
        launch {
            withBackgroundProgress(project, PIBundle.message("background_scan", project.name), true) {
                val app = ApplicationManager.getApplication()
                try {
                    val piProject = scan(project, realRunMode) { p -> app.invokeLater { _refreshingProgress.set(p) } }
                    app.invokeLater { _pyInvokeProject.set(piProject) }
                }
                finally {
                    app.invokeLater { _refreshingProgress.set(null) }
                }
            }
        }
    }

    val runMode: PIRunMode
        get() = pyInvokeProject.get()?.runMode ?: MODE_TERMINAL_RUN

    override fun dispose() = job.cancel()
}
