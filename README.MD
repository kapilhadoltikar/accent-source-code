

# 🌗 Accent - Intelligent Theme Switcher for Windows

> **"Because manual toggling is so 2010."**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-4285F4?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)

## 🚀 Introduction

**Accent** is a lightweight, native Windows utility that allows you to instantly toggle between Dark and Light system themes directly from your system tray.

Built with performance and modernity in mind, this project demonstrates how modern Java can be used to build native-feeling desktop applications that integrate deeply with the Operating System.

## 🛠️ Tech Stack & Architectural Decisions

As a developer, I believe in choosing the right tool for the job. Here's why I chose this specific stack:

### ☕ Java 21 (LTS)
**Why?** Scalability & Modern Syntax.
Java 21 isn't your grandfather's Java. I utilized its modern features for cleaner code and better performance. It provides robust process management (`ProcessBuilder`) which is crucial for the underlying registry hooks this app relies on.

### 🎨 JavaFX
**Why?** Superior UI/UX Capability.
Swing is outdated. Electron is heavy. JavaFX provides the perfect middle ground—hardware-accelerated graphics, CSS-based styling, and a rich scene graph. This allowed me to create a custom, fluid toggle animation and a translucent, non-intrusive UI that looks native to Windows 11.

### 📦 JPackage (Native Image)
**Why?** User Experience (UX).
Asking a user to "install the JRE" is a barrier to entry. I used `jpackage` to bundle a slimmed-down custom runtime image with the application. The result is a standalone `.exe` installer (approx. 40MB) that runs on any Windows machine, regardless of whether Java is installed.

### ⚡ PowerShell & Win32 Registry Integration
**Why?** Direct OS Manipulation.
To achieve an *instant* theme switch without requiring a logout or restart, the app interfaces directly with the Windows Registry (`HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize`) and broadcasts a `WM_SETTINGCHANGE` message via user32.dll calls. This is efficient and bypasses bloated wrapper libraries.

## ✨ Features

- **Instant Toggle**: Switch themes in milliseconds.
- **System Tray Integration**: Stays out of your way, accessible when you need it.
- **State Persistence**: Remembers your preference across restarts.
- **Start on Boot**: Optional registry hook to launch automatically with Windows.
- **Zero Dependencies**: Runs as a standalone native executable.

## 🏗️ Building from Source

This project uses Maven for dependency management and a custom PowerShell script for native packaging.

**Prerequisites:**
- JDK 21+
- Maven
- WiX Toolset (optional, for MSI installers)

**1. Build the Project**
First, compile the Java source code and package it into a shaded JAR.
```powershell
mvn clean install
```

**2. Create Native Installer (EXE)**
Use the `jpackage` tool (bundled with JDK) to generate a standalone Windows installer.
*Note: This requires the [WiX Toolset](https://wixtoolset.org/) (v3.11 or later) to be installed and added to your PATH.*

```powershell
jpackage `
 --type exe `
 --input target `
 --main-jar win-1.0-SNAPSHOT.jar `   
 --main-class com.accent.win.Launcher `   
 --name Accent `
 --icon src/main/resources/sun-icon.ico `
 --dest dist `
 --win-shortcut `
 --win-menu
```

**3. Install & Run**
Navigate to the `dist` folder and run `Accent-1.0.exe` to install the application. It will then be available in your Start Menu.

---

*Crafted with 💻 and ☕ by a developer who cares about the details.*


