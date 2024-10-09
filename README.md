<p align="center"><img height="200px" src="src/main/resources/META-INF/pluginIcon.svg"/></p>

[![Build](https://github.com/kairaedsch/intellij-pyinvoke/workflows/Build/badge.svg)](https://github.com/kairaedsch/intellij-pyinvoke/actions)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.kairaedsch.intellijpyinvoke.svg)](https://plugins.jetbrains.com/plugin/24793-pyinvoke)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.kairaedsch.intellijpyinvoke.svg)](https://plugins.jetbrains.com/plugin/24793-pyinvoke)

## PyInvoke for IntelliJ IDEs
<!-- Plugin description -->
Provides [PyInvoke](https://github.com/pyinvoke/invoke) support to all IntelliJ-based IDEs.

Execute and debug your PyInvoke tasks directly within your IDE.

### Showcase
<p align="center">
    <img src="images/screenshot_1.png"/>
    <img src="images/screenshot_2.png"/>
</p>

### Features
- **▶️ Inline Run Buttons**: Easily run your PyInvoke tasks with convenient inline run buttons placed next to each task function.
- **📋 Task Overview**: Access a comprehensive overview of all available tasks in your project, allowing you to run any task with a single click.
- **⚙️ Multiple Execution Options**: Choose from three different execution methods:
    - **▶️ Terminal**: Execute tasks directly from the command line.
    - **🐍 SDK Run**: Run tasks using your project's configured Python SDK.
    - **🪲 SDK Debug**: Debug tasks seamlessly within IntelliJ.

### Upcoming Features
- **Parameter Detection**: Automatically detect possible task parameters and provide an intuitive interface for specifying these parameters within run configurations.
<!-- Plugin description end -->

### Installation
- Using the IDE built-in plugin system:\
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "intellij-pyinvoke"</kbd> >
  <kbd>Install</kbd>
  
- Manually:\
  Download the [latest release](https://github.com/kairaedsch/intellij-pyinvoke/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
