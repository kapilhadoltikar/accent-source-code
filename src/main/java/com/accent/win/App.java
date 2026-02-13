package com.accent.win;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private boolean isLightMode;
    private TrayIcon trayIcon;
    private Stage mainStage;
    private Circle toggleTrigger;

    private static final String REG_PATH = "HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String RUN_KEY = "HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "Accent";

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Accent application...");
        this.mainStage = primaryStage;
        Platform.setImplicitExit(false);
        isLightMode = getCurrentTheme();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(new Label("Windows Theme Switcher"), createCustomToggle());

        Scene scene = new Scene(root, 350, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Settings");

        applyCSS();

        if (SystemTray.isSupported()) {
            setupSystemTray();
        } else {
            logger.warn("System Tray not supported!");
        }
    }

    private Node createCustomToggle() {
        Rectangle toggleBg = new Rectangle(60, 30);
        toggleBg.setArcWidth(30);
        toggleBg.setArcHeight(30);
        toggleBg.getStyleClass().add("toggle-background");

        toggleTrigger = new Circle(12);
        toggleTrigger.getStyleClass().add("toggle-trigger");
        toggleTrigger.setTranslateX(isLightMode ? 15 : -15);

        StackPane toggle = new StackPane(toggleBg, toggleTrigger);
        toggle.setMaxWidth(60);
        toggle.setCursor(Cursor.HAND);
        toggle.setOnMouseClicked(event -> toggleTheme());

        return toggle;
    }

    private void setupSystemTray() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();

            MenuItem toggleItem = new MenuItem("Toggle Mode");
            toggleItem.addActionListener(e -> toggleTheme());

            CheckboxMenuItem startupItem = new CheckboxMenuItem("Start with Windows");
            startupItem.setState(isStartupEnabled());
            startupItem.addItemListener(e -> toggleStartup(startupItem.getState()));

            MenuItem openItem = new MenuItem("Settings");
            openItem.addActionListener(e -> Platform.runLater(mainStage::show));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                tray.remove(trayIcon);
                Platform.exit();
                System.exit(0);
            });

            popup.add(toggleItem);
            popup.add(startupItem);
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            trayIcon = new TrayIcon(getIconImage(), "Accent", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception e) {
            logger.error("Failed to setup system tray", e);
        }
    }

    private void toggleTheme() {
        isLightMode = !isLightMode;
        String val = isLightMode ? "1" : "0";

        // Update UI
        updateToggleAnimation();
        applyCSS();
        if (trayIcon != null) {
            trayIcon.setImage(getIconImage());
        }

        // Windows Registry Update
        new Thread(() -> {
            try {
                String cmd = String.format(
                        "Set-ItemProperty -Path '%s' -Name 'AppsUseLightTheme' -Value %s; " +
                                "Set-ItemProperty -Path '%s' -Name 'SystemUsesLightTheme' -Value %s; " +
                                "RUNDLL32.EXE USER32.DLL,UpdatePerUserSystemParameters",
                        REG_PATH, val, REG_PATH, val);
                new ProcessBuilder("powershell.exe", "-Command", cmd).start().waitFor();
                logger.info("Theme toggled to: {}", isLightMode ? "Light" : "Dark");
            } catch (Exception e) {
                logger.error("Failed to toggle theme via Registry", e);
            }
        }).start();
    }

    private void updateToggleAnimation() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), toggleTrigger);
        tt.setToX(isLightMode ? 15 : -15);
        tt.play();
    }

    private Image getIconImage() {
        String path = isLightMode ? "/sun-icon.png" : "/moon-icon.png";
        try {
            URL resource = getClass().getResource(path);
            if (resource == null)
                throw new Exception("Icon not found: " + path);
            return Toolkit.getDefaultToolkit().getImage(resource);
        } catch (Exception e) {
            logger.error("Failed to load icon: {}", path, e);
            // Fallback
            BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = fallback.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
            return fallback;
        }
    }

    // Helper to apply CSS to the JavaFX Stage
    private void applyCSS() {
        Platform.runLater(() -> {
            if (mainStage.getScene() == null)
                return;

            mainStage.getScene().getStylesheets().clear();
            String css = isLightMode ? "/light.css" : "/dark.css";
            var resource = getClass().getResource(css);

            if (resource != null) {
                mainStage.getScene().getStylesheets().add(resource.toExternalForm());
            } else {
                logger.error("CSS Resource not found: {}", css);
            }
        });
    }

    private boolean getCurrentTheme() {
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v",
                    "AppsUseLightTheme").start();
            try (Scanner s = new Scanner(p.getInputStream())) {
                String out = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                return out.contains("0x1");
            }
        } catch (Exception e) {
            logger.warn("Failed to read current theme from registry, defaulting to Light", e);
            return true;
        }
    }

    private void toggleStartup(boolean enable) {
        try {
            String path = ProcessHandle.current().info().command().orElse("");
            String cmd = enable
                    ? String.format("Set-ItemProperty -Path '%s' -Name '%s' -Value '\"%s\"'", RUN_KEY, APP_NAME, path)
                    : String.format("Remove-ItemProperty -Path '%s' -Name '%s' -ErrorAction SilentlyContinue", RUN_KEY,
                    APP_NAME);
            new ProcessBuilder("powershell.exe", "-Command", cmd).start();
            logger.info("Startup enabled: {}", enable);
        } catch (IOException e) {
            logger.error("Failed to toggle startup registry key", e);
        }
    }

    private boolean isStartupEnabled() {
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", "/v", APP_NAME).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            logger.debug("Failed to check if startup is enabled (likely not enabled)", e);
            return false;
        }
    }
}