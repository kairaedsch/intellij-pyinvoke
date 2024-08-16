package com.github.kairaedsch.intellijpyinvoke.frontend.ui.tool

import com.intellij.ui.components.JBPanel
import javafx.beans.value.ObservableValue
import java.awt.BorderLayout

abstract class PIAutoUpdatePanel() {
    private val listenValues = mutableSetOf<ObservableValue<out Any?>>()
    val panel = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()
    }
        get() {
            if (field.componentCount == 0) field.add(createComponent(), BorderLayout.CENTER)
            return field
        }

    fun listen(value: ObservableValue<out Any?>) {
        val added = listenValues.add(value)
        if (added) {
            value.addListener { _, _, _ ->
                panel.removeAll()
                panel.add(createComponent(), BorderLayout.CENTER)
            }
        }
    }

    abstract fun createComponent(): JBPanel<*>
}
