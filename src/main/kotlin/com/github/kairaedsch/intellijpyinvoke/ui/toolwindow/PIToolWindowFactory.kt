package com.github.kairaedsch.intellijpyinvoke.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout

class PIToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val pyInvokeToolWindow = PIToolWindow(toolWindow)
            add(pyInvokeToolWindow.createContent(), BorderLayout.CENTER)

            project.service<PIService>().pyInvokeProject.addListener { _, _, _ ->
                if (toolWindow.isDisposed) return@addListener
                removeAll()
                add(pyInvokeToolWindow.createContent(), BorderLayout.CENTER)
            }
        }

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}
