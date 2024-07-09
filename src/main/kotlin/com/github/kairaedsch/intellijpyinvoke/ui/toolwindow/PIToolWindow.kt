package com.github.kairaedsch.intellijpyinvoke.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.run.PIAction.Factory.createActionsList
import com.github.kairaedsch.intellijpyinvoke.scanner.PITask
import com.intellij.icons.ExpUiIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ActionPlaces.TOOLWINDOW_CONTENT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.modules
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
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
                        val projectNode = DefaultMutableTreeNode(folder.pathFromModule)
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
        tree.cellRenderer = PITreeCellRenderer()
        add(JBScrollPane(tree), BorderLayout.CENTER)

        toolWindow.stripeTitle = PIBundle.message("toolWindow.title")
        toolWindow.setTitleActions(listOf(
            object : AnAction({ PIBundle.message("refresh") }, ExpUiIcons.General.Refresh) {
                override fun actionPerformed(e: AnActionEvent) = service.refresh()
            },
            object : AnAction({ PIBundle.message("expand_all") }, ExpUiIcons.General.ExpandAll) {
                override fun actionPerformed(e: AnActionEvent) {
                    var i = 0
                    while (i++ <= tree.rowCount) tree.expandRow(i - 1)
                }
            },
            object : AnAction({ PIBundle.message("collapse_all") }, ExpUiIcons.General.CollapseAll) {
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
    }
}
