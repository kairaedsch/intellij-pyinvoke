package com.github.kairaedsch.intellijpyinvoke.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIBundle.message
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.ui.Icons
import com.intellij.icons.ExpUiIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes.*
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class PITreeCellRenderer : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        val node = value as DefaultMutableTreeNode
        val obj = node.userObject
        val level = node.level

        val name = when {
            obj is Project -> obj.name
            obj is Module -> obj.name
            else -> obj.toString()
        }
        append(name, if (level <= 1) REGULAR_BOLD_ATTRIBUTES else REGULAR_ATTRIBUTES)
        if (node.leafCount > 1) append("  " + message("x_tasks", node.leafCount), GRAYED_ATTRIBUTES)
        icon = when {
            level <= 1 -> Icons.Logo
            !leaf -> ExpUiIcons.RunConfigurations.Compound
            else -> ExpUiIcons.Run.Run
        }
        if (obj is Module) {
            val folders = obj.project.service<PIService>().pyInvokeProject.get()?.pyInvokeFolders
                ?.filter { it.module == obj } ?: emptyList()
            toolTipText = message("module_info", folders.size, folders.joinToString("\n") { it.path })
        }
    }
}
