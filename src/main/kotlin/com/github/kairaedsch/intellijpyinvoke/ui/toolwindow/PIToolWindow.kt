package com.github.kairaedsch.intellijpyinvoke.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.run.PIAction.*
import com.github.kairaedsch.intellijpyinvoke.run.PIAction.Factory.createActionsList
import com.github.kairaedsch.intellijpyinvoke.run.PIRunMode.MODE_TERMINAL_RUN
import com.github.kairaedsch.intellijpyinvoke.run.PIRunMode.MODE_SDK_RUN
import com.github.kairaedsch.intellijpyinvoke.run.PIRunMode.MODE_SDK_DEBUG
import com.github.kairaedsch.intellijpyinvoke.scanner.PITask
import com.intellij.icons.ExpUiIcons
import com.intellij.icons.ExpUiIcons.General.CollapseAll
import com.intellij.icons.ExpUiIcons.General.ExpandAll
import com.intellij.icons.ExpUiIcons.Status.InfoOutline
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ActionPlaces.TOOLWINDOW_CONTENT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.modules
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.tree.DefaultMutableTreeNode

class PIToolWindow(private val toolWindow: ToolWindow) {
    private val service = toolWindow.project.service<PIService>()

    fun createContent() = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()

        val root = DefaultMutableTreeNode(toolWindow.project)
        toolWindow.project.modules.forEach { module ->
            val moduleNode = DefaultMutableTreeNode(module)
            root.add(moduleNode)
            service.pyInvokeProject.get()?.pyInvokeFolders
                ?.filter { it.module == module }
                ?.forEach { folder ->
                    val folderNode = if (folder.pathFromModule == "") moduleNode else {
                        val projectNode = DefaultMutableTreeNode(folder)
                        moduleNode.add(projectNode)
                        projectNode
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

        toolWindow.stripeTitle = PIBundle.message("toolWindow.title")
        toolWindow.setTitleActions(listOf(
            object : AnAction({ PIBundle.message("refresh") }, ExpUiIcons.General.Refresh) {
                override fun actionPerformed(e: AnActionEvent) = service.refresh()
            },
            object : ActionGroup(PIBundle.message("current_mode", service.runMode.title), true) {
                init {
                    isPopup = true
                    templatePresentation.icon = service.runMode.icon
                }
                override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                    return arrayOf(
                        MODE_TERMINAL_RUN.asAction { service.refresh(MODE_TERMINAL_RUN) },
                        MODE_SDK_RUN.asAction { service.refresh(MODE_SDK_RUN) },
                        MODE_SDK_DEBUG.asAction { service.refresh(MODE_SDK_DEBUG) },
                    )
                }
            },
            Separator.create(),
            object : AnAction({ PIBundle.message("expand_all") }, ExpandAll) {
                override fun actionPerformed(e: AnActionEvent) {
                    var i = 0
                    while (i++ <= tree.rowCount) tree.expandRow(i - 1)
                }
            },
            object : AnAction({ PIBundle.message("collapse_all") }, CollapseAll) {
                override fun actionPerformed(e: AnActionEvent) {
                    var i = 0
                    while (i++ <= tree.rowCount) tree.collapseRow(i)
                }
            }
        ))

        val popupMenuActionGroup = object : ActionGroup() {
            override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                val selectedNode = tree.getSelectedNodes(DefaultMutableTreeNode::class.java, null)[0] ?: return emptyArray()
                if (!selectedNode.isLeaf) return emptyArray()
                return createActionsList({selectedNode.userObject as PITask})
            }
        }
        val popupMenu = ActionManager.getInstance().createActionPopupMenu(TOOLWINDOW_CONTENT, popupMenuActionGroup)
        tree.componentPopupMenu = popupMenu.component

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount != 2) return
                val selectedNode = tree.getSelectedNodes(DefaultMutableTreeNode::class.java, null)[0] ?: return
                if (!selectedNode.isLeaf) return
                val piTask = selectedNode.userObject as PITask
                when (service.runMode) {
                    MODE_SDK_RUN -> SDK_RUN.run(piTask)
                    MODE_SDK_DEBUG -> SDK_DEBUG.run(piTask)
                    MODE_TERMINAL_RUN -> TERMINAL_RUN.run(piTask)
                }
            }
        })

    }
}
