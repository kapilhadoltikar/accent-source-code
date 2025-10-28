package com.accent.theme2;

/*  system tray working application  */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Scanner;

public class App extends Application {

    private boolean isLightMode;

    @Override
    public void start(Stage primaryStage) {
        // Initialize theme
        isLightMode = getCurrentTheme();

        // Setup system tray
        if (SystemTray.isSupported()) {
            setupSystemTray(primaryStage);
        } else {
            System.out.println("System tray not supported!");
        }
    }

    private void setupSystemTray(Stage stage) {
        try {
            SystemTray tray = SystemTray.getSystemTray();

            java.net.URL iconURL = getClass().getResource("/tray-icon.png");
            if (iconURL == null) {
                throw new IllegalStateException("Icon resource '/tray-icon.png' not found");
            }

            Image image = Toolkit.getDefaultToolkit().getImage(iconURL);

            PopupMenu popup = new PopupMenu();

            MenuItem toggleItem = new MenuItem("Toggle Dark/Light Mode");
            toggleItem.addActionListener(_ -> toggleTheme());

            MenuItem openItem = new MenuItem("Open Window");
            openItem.addActionListener(_ -> Platform.runLater(stage::show));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(_ -> {
                tray.remove(tray.getTrayIcons()[0]);
                Platform.exit();
                System.exit(0);
            });

            popup.add(toggleItem);
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Dark / Light Mode", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggle Windows Dark/Light theme using PowerShell for instant effect.
     */
    private void toggleTheme() {
        try {
            String newValue = isLightMode ? "0" : "1";

            String psCommand = String.format(
                    "$registryPath = 'HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize';" +
                            "Set-ItemProperty -Path $registryPath -Name 'AppsUseLightTheme' -Value %s;" +
                            "Set-ItemProperty -Path $registryPath -Name 'SystemUsesLightTheme' -Value %s;" +
                            "RUNDLL32.EXE USER32.DLL,UpdatePerUserSystemParameters",
                    newValue, newValue
            );

            Process powerShellProcess = new ProcessBuilder(
                    "powershell.exe", "-Command", psCommand
            ).start();

            powerShellProcess.waitFor();

            isLightMode = !isLightMode;
            System.out.println("Current Mode: " + (isLightMode ? "Light" : "Dark"));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads current theme from the registry. True = Light Mode, False = Dark Mode
     */
    private boolean getCurrentTheme() {
        try {
            Process readProcess = new ProcessBuilder("reg", "query",
                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme").start();
            readProcess.waitFor();

            Scanner scanner = new Scanner(readProcess.getInputStream()).useDelimiter("\\A");
            String output = scanner.hasNext() ? scanner.next() : "";
            scanner.close();

            return output.contains("0x1"); // true if Light Mode
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
