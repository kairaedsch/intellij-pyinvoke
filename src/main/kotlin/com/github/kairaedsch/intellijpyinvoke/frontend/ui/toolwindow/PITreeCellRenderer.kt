package com.github.kairaedsch.intellijpyinvoke.frontend.ui.toolwindow

import com.github.kairaedsch.intellijpyinvoke.PIBundle.message
import com.github.kairaedsch.intellijpyinvoke.PIService
import com.github.kairaedsch.intellijpyinvoke.common.PIFolder
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.github.kairaedsch.intellijpyinvoke.common.PIInfo
import com.github.kairaedsch.intellijpyinvoke.frontend.ui.Icons
import com.intellij.icons.AllIcons.RunConfigurations.Compound
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes.*
import java.util.*
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode


class PITreeCellRenderer(private val service: PIService) : ColoredTreeCellRenderer() {
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
        val taskCount = tasksCount(node)

        val name = when (obj) {
            is Project -> obj.name
            is Module -> obj.name
            is PIFolder -> obj.pathFromModule
            is PIInfo -> obj.message
            is PITask -> obj.name
            is PICollection -> obj.name
            else -> "Unknown object $obj"
        }
        append(name, if (isBoldText(obj)) REGULAR_BOLD_ATTRIBUTES else REGULAR_ATTRIBUTES)
        if (obj is Project || obj is Module || obj is PIFolder) append("  " + message("x_tasks", taskCount), GRAYED_ATTRIBUTES)
        icon = when {
            level <= 1 -> Icons.logo
            !leaf -> Compound
            obj is PIInfo -> infoStateIcon(obj.state)
            else -> runModeIcon(service.runMode)
        }
        toolTipText = when (obj) {
            is PIInfo -> obj.message
            is PITask -> obj.description
            else -> ""
        }
    }

    private fun isBoldText(obj: Any) = obj is Project || obj is Module

    private fun tasksCount(node: DefaultMutableTreeNode): Int {
        var count = 0
        @Suppress("UNCHECKED_CAST")
        for (child in node.children() as Enumeration<DefaultMutableTreeNode>) {
            count += if (child.userObject is PITask) 1 else tasksCount(child)
        }
        return count
    }
}
