package com.github.kairaedsch.intellijpyinvoke.backend

import com.github.kairaedsch.intellijpyinvoke.PIBundle
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode.*
import com.github.kairaedsch.intellijpyinvoke.common.PIInfo
import com.github.kairaedsch.intellijpyinvoke.common.PIRunMode
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil.execAndGetOutput
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUtil
import java.nio.charset.StandardCharsets.UTF_8

class PIDirectRunner(val onInfo: (piInfo: PIInfo) -> Unit) {
    fun runPyInvoke(
        runMode: PIRunMode,
        module: Module,
        path: String,
        vararg args: String
    ): String? {
        return when (runMode) {
            MODE_TERMINAL_RUN -> runConsolePyInvoke(path, args.toList())
            MODE_SDK_RUN -> runModulePyInvoke(module, path, args.toList())
            MODE_SDK_DEBUG -> runModulePyInvoke(module, path, args.toList())
        }
    }

    private fun runConsolePyInvoke(path: String, args: List<String>): String? {
        return runCommand(path, "invoke", args.toList())
    }

    private fun runProjectPyInvoke(project: Project, path: String, args: List<String>): String? {
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        if (sdk == null) {
            onInfo(PIInfo.error(PIBundle.message("info_no_project_sdk")))
            return null
        }
        return runSdkPyInvoke(sdk, path, args.toList())
    }

    private fun runModulePyInvoke(module: Module, path: String, args: List<String>): String? {
        val sdk = PythonSdkUtil.findPythonSdk(module)
        if (sdk == null) {
            onInfo(PIInfo.error(PIBundle.message("info_no_module_sdk")))
            return null
        }
        return runSdkPyInvoke(sdk, path, args.toList())
    }

    private fun runSdkPyInvoke(sdk: Sdk, path: String, args: List<String>): String? {
        if (sdk.sdkType !is PythonSdkType) {
            onInfo(PIInfo.error(PIBundle.message("info_no_python_sdk")))
            return null
        }
        val pythonHome = sdk.homePath
        if (pythonHome == null) {
            onInfo(PIInfo.error(PIBundle.message("info_no_python_home")))
            return null
        }
        val pythonExe = PythonSdkUtil.getPythonExecutable(pythonHome)
        if (pythonExe == null) {
            onInfo(PIInfo.error(PIBundle.message("info_no_python_exe")))
            return null
        }
        return runCommand(path, pythonExe, listOf("-m", "invoke") + args)
    }

    private fun runCommand(path: String, command: String, args: List<String>): String? {
        fileLogger().info("Running... '$command ${args.joinToString(" ")}' in $path")
        val generalCommandLine = GeneralCommandLine(command)
        generalCommandLine.addParameters(args)
        generalCommandLine.charset = UTF_8
        generalCommandLine.setWorkDirectory(path)
        try {
            val output = execAndGetOutput(generalCommandLine)
            if (output.exitCode != 0) {
                throw ExecutionException(output.stderr + output.stdout)
            }
            val stdout = output.stdout
            fileLogger().info("Ran! Output: \n$output")
            return stdout
        } catch (e: ExecutionException) {
            fileLogger().info("Error running '$command ${args.joinToString(" ")}' in $path", e)
            onInfo(PIInfo.error(PIBundle.message("info_run_error", e.message ?: "")))
            return null
        }
    }
}
