package com.github.kairaedsch.intellijpyinvoke.frontend.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.common.PIInfo
import com.github.kairaedsch.intellijpyinvoke.common.PIInfoState.INFO
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode.*
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.github.kairaedsch.intellijpyinvoke.frontend.tool.PIAction.*
import com.github.kairaedsch.intellijpyinvoke.frontend.tool.PIAction.Factory.createActionsList
import com.github.kairaedsch.intellijpyinvoke.frontend.ui.tool.PIAutoUpdatePanel
import com.intellij.icons.ExpUiIcons
import com.intellij.icons.ExpUiIcons.General.CollapseAll
import com.intellij.icons.ExpUiIcons.General.ExpandAll
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ActionPlaces.TOOLWINDOW_CONTENT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.modules
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.AnimatedIcon
import java.awt.Component
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import javafx.beans.property.SimpleBooleanProperty
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class PIToolWindow(private val toolWindow: ToolWindow): PIAutoUpdatePanel() {
    private val project = toolWindow.project
    private val service = project.service<PIService>()
    private val infoMode = SimpleBooleanProperty(false)

    init {
        listen(service.pyInvokeProject)
        listen(service.refreshing)
        listen(service.refreshingProgress)
        listen(infoMode)
    }

    override fun createComponent() = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()
        if (service.refreshing.get()) add(createLoadingPanel(), BorderLayout.CENTER)
        else if (service.pyInvokeProject.get() == null) add(createLoadTasksPanel(), BorderLayout.CENTER)
        else add(createTreePanel(), BorderLayout.CENTER)
    }

    private fun createLoadingPanel() = JBPanel<JBPanel<*>>().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val bar = JProgressBar()
        bar.minimum = -5
        bar.maximum = 100
        bar.value = ((service.refreshingProgress.get() ?: 0.0) * 100).toInt()
        add(bar, SwingConstants.CENTER)

        add(Box.createVerticalStrut(JBUI.scale(10)))

        val label = JLabel(PIBundle.message("loading", bar.value), AnimatedIcon.Default(), SwingConstants.CENTER)
        label.alignmentX = Component.CENTER_ALIGNMENT
        add(label)
        addTitle(null)
    }

    private fun createLoadTasksPanel() = JBPanel<JBPanel<*>>().apply {
        layout = FlowLayout(FlowLayout.CENTER)

        add(JLabel(PIBundle.message("no_tasks_loaded")))

        val button = JButton(PIBundle.message("load_tasks"))
        button.addActionListener { service.refresh() }
        add(button)

        addTitle(null)
    }

    private fun createTreePanel() = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()

        val root = DefaultMutableTreeNode(project)
        project.modules.forEach { module ->
            val moduleNode = DefaultMutableTreeNode(module)
            root.add(moduleNode)
            val folders = service.pyInvokeProject.get()?.pyInvokeFolders
                ?.filter { it.module == module }
            if (folders.isNullOrEmpty() && infoMode.value) {
                moduleNode.add(DefaultMutableTreeNode(PIInfo.info(PIBundle.message("info_no_task_folder_found"))))
            }
            folders
                ?.filter { it.tasks.isNotEmpty() || infoMode.value}
                ?.forEach { folder ->
                    val folderNode = if (folder.pathFromModule == "") moduleNode else {
                        val projectNode = DefaultMutableTreeNode(folder)
                        moduleNode.add(projectNode)
                        projectNode
                    }
                    if (infoMode.value) {
                        folder.infos.forEach { info -> folderNode.add(DefaultMutableTreeNode(info)) }
                        if (folder.tasks.isEmpty()) {
                            folderNode.add(DefaultMutableTreeNode(PIInfo.info(PIBundle.message("info_no_tasks_found"))))
                        }
                    }
                    folder.tasks.forEach { task -> folderNode.add(DefaultMutableTreeNode(task)) }
                }
        }
        val tree = Tree(root)
        var i = 0
        while (i++ <= tree.rowCount) tree.expandRow(i - 1)
        tree.isRootVisible = false
        tree.cellRenderer = PITreeCellRenderer(service)
        add(JBScrollPane(tree), BorderLayout.CENTER)

        val popupMenuActionGroup = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                val selectedNodes = tree.getSelectedNodes(DefaultMutableTreeNode::class.java, null)
                if (selectedNodes.isEmpty()) return emptyArray()
                val selectedNode = selectedNodes[0] ?: return emptyArray()
                if (selectedNode.userObject !is PITask) return emptyArray()
                val piTask = selectedNode.userObject as PITask
                return createActionsList(piTask.name, true) { callback -> callback(piTask) }
            }
        }
        val popupMenu = ActionManager.getInstance().createActionPopupMenu(TOOLWINDOW_CONTENT, popupMenuActionGroup)
        tree.componentPopupMenu = popupMenu.component

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2) return
                val selectedNode = tree.getSelectedNodes(DefaultMutableTreeNode::class.java, null)[0] ?: return
                if (selectedNode.userObject !is PITask) return
                val piTask = selectedNode.userObject as PITask
                when (service.runMode) {
                    MODE_SDK_RUN -> SDK_RUN.run(piTask)
                    MODE_SDK_DEBUG -> SDK_DEBUG.run(piTask)
                    MODE_TERMINAL_RUN -> TERMINAL_RUN.run(piTask)
                }
            }
        })

        addTitle(tree)
    }

    private fun addTitle(tree: Tree?) {
        val infoSeverity = service.pyInvokeProject.get()?.pyInvokeFolders
            ?.flatMap { it.infos }
            ?.map { it.state }
            ?.maxBy { it.severity } ?: INFO
        toolWindow.stripeTitle = PIBundle.message("toolWindow.title")
        toolWindow.setTitleActions(
            listOf(
                object : AnAction({ PIBundle.message("refresh") }, ExpUiIcons.General.Refresh) {
                    override fun actionPerformed(e: AnActionEvent) = service.refresh()
                },
                object : ActionGroup(PIBundle.message("current_mode", service.runMode.title), true) {
                    init {
                        isPopup = true
                        templatePresentation.icon = runModeIcon(service.runMode)
                    }
                    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                        return arrayOf(
                            runModeAsAction(MODE_TERMINAL_RUN, { service.runMode == MODE_TERMINAL_RUN }, { service.refresh(MODE_TERMINAL_RUN) }),
                            runModeAsAction(MODE_SDK_RUN, { service.runMode == MODE_SDK_RUN }, { service.refresh(MODE_SDK_RUN) }),
                            runModeAsAction(MODE_SDK_DEBUG, { service.runMode == MODE_SDK_DEBUG }, { service.refresh(MODE_SDK_DEBUG) }),
                        )
                    }
                },
                Separator.create(),
                object : AnAction({ PIBundle.message("expand_all") }, ExpandAll) {
                    override fun actionPerformed(e: AnActionEvent) {
                        actionUpdateThread
                        var i = 0
                        while (i++ <= (tree?.rowCount ?: 0)) tree?.expandRow(i - 1)
                    }
                },
                object : AnAction({ PIBundle.message("collapse_all") }, CollapseAll) {
                    override fun actionPerformed(e: AnActionEvent) {
                        var i = 0
                        while (i++ <= (tree?.rowCount ?: 0)) tree?.collapseRow(i)
                    }
                },
                Separator.create(),
                object : ToggleAction ({ PIBundle.message(if (infoMode.value) "hide_info" else "show_info") }, infoStateIcon(infoSeverity)) {
                    override fun isSelected(e: AnActionEvent): Boolean {
                        return infoMode.get()
                    }
                    override fun setSelected(e: AnActionEvent, state: Boolean) {
                        infoMode.set(state)
                    }
                    override fun getActionUpdateThread(): ActionUpdateThread {
                        return ActionUpdateThread.EDT
                    }
                },
            )
        )
    }
}
