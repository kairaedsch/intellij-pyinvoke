package com.github.kairaedsch.intellijpyinvoke.frontend.run

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.common.PITask
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunDialog
import com.intellij.sh.run.ShConfigurationType
import com.intellij.sh.run.ShRunConfiguration
import com.jetbrains.python.run.PythonConfigurationType
import com.jetbrains.python.run.PythonRunConfiguration
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import org.jetbrains.plugins.terminal.TerminalUtil


fun createSdkRunConfiguration(task: PITask): RunnerAndConfigurationSettings {
    val runManager = RunManager.getInstance(task.module.project)
    val configuration = runManager.createConfiguration("Pyinvoke ${task.fullName}", PythonConfigurationType::class.java)
    val pythonRunConfiguration = configuration.configuration as PythonRunConfiguration

    pythonRunConfiguration.name = "invoke ${task.fullName}"
    pythonRunConfiguration.isUseModuleSdk = true
    pythonRunConfiguration.module = task.module
    pythonRunConfiguration.scriptName = "invoke"
    pythonRunConfiguration.isModuleMode = true
    pythonRunConfiguration.workingDirectory = task.path
    pythonRunConfiguration.scriptParameters = task.fullName

    return configuration
}

fun createTerminalRunConfiguration(task: PITask): RunnerAndConfigurationSettings {
    val runManager = RunManager.getInstance(task.module.project)
    val factory = ShConfigurationType.getInstance()
    val configuration: RunnerAndConfigurationSettings = runManager.createConfiguration("Pyinvoke ${task.fullName}",
        factory
    )
    val shRunConfiguration = configuration.configuration as ShRunConfiguration

    shRunConfiguration.name = "invoke ${task.fullName}"
    shRunConfiguration.scriptText = "invoke ${task.fullName}"
    shRunConfiguration.isExecuteInTerminal = false
    shRunConfiguration.scriptWorkingDirectory = task.path

    return configuration
}

fun run(configuration: RunnerAndConfigurationSettings) {
    add(configuration)
    val executor = DefaultRunExecutor.getRunExecutorInstance()
    ProgramRunnerUtil.executeConfiguration(configuration, executor)
}

fun runInTerminalTab(task: PITask) {
    val tool = TerminalToolWindowManager.getInstance(task.module.project)
    val widget = tool.terminalWidgets
        .filter { it.terminalTitle.defaultTitle?.startsWith("PyInvoke") ?: false }
        .filter {
            val ttyConnector = it.ttyConnector
            ttyConnector == null || !TerminalUtil.hasRunningCommands(ttyConnector)
        }
        .firstOrNull() ?: tool.createShellWidget(task.path, "PyInvoke", true, true)
    widget.sendCommandToExecute("invoke ${task.fullName}")

    tool.toolWindow?.show {
        val content = tool.getContainer(widget)?.content ?: return@show
        tool.toolWindow.contentManager.setSelectedContent(content, true, true)
    }
}

fun debug(configuration: RunnerAndConfigurationSettings) {
    add(configuration)
    val executor = DefaultDebugExecutor.getDebugExecutorInstance()
    ProgramRunnerUtil.executeConfiguration(configuration, executor)
}

fun modify(configuration: RunnerAndConfigurationSettings) {
    add(configuration)
    RunDialog.editConfiguration(configuration.configuration.project, configuration, PIBundle.message("create_run_conf", configuration.name))
}

private fun add(configuration: RunnerAndConfigurationSettings) {
    val runManager = RunManager.getInstance(configuration.configuration.project)

    runManager.addConfiguration(configuration)
    runManager.selectedConfiguration = configuration
}
