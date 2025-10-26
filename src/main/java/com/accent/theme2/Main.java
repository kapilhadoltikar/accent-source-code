package com.accent.theme2;
//
///*  simple working application  */
//import javafx.animation.TranslateTransition;
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//import java.io.IOException;
//import java.util.Scanner;
//
//public class Main extends Application {
//
//    private Circle toggleKnob;
//    private Pane toggleBackground;
//    private Label statusLabel;
//    private boolean isLightMode;
//
//    @Override
//    public void start(Stage primaryStage) {
//        statusLabel = new Label();
//        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        // Create toggle switch
//        toggleBackground = new Pane();
//        toggleBackground.setPrefSize(60, 30);
//        toggleBackground.setStyle("-fx-background-radius: 15; -fx-background-color: #718093;");
//
//        toggleKnob = new Circle(12);
//        toggleKnob.setFill(Color.WHITE);
//        toggleKnob.setTranslateX(15);
//        toggleKnob.setTranslateY(15);
//
//        StackPane toggleStack = new StackPane(toggleBackground, toggleKnob);
//        toggleStack.setOnMouseClicked(e -> toggleTheme());
//
//        // Initialize current theme
//        isLightMode = getCurrentTheme();
//        updateUI(false); // initial setup without animation
//
//        VBox root = new VBox(20);
//        root.setPadding(new Insets(30));
//        root.setAlignment(Pos.CENTER);
//        root.getChildren().addAll(statusLabel, toggleStack);
//
//        Scene scene = new Scene(root, 300, 150);
//        primaryStage.setTitle("Windows 11 Dark Mode Toggler");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private void toggleTheme() {
//        try {
//            boolean currentMode = getCurrentTheme();
//            String newValue = currentMode ? "0" : "1";
//
//            // Update registry
//            Process writeProcess = new ProcessBuilder("reg", "add",
//                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
//                    "/v", "AppsUseLightTheme", "/t", "REG_DWORD", "/d", newValue, "/f").start();
//            writeProcess.waitFor();
//
//            // Update UI
//            isLightMode = !currentMode;
//            updateUI(true); // animate toggle
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateUI(boolean animate) {
//        double startX = toggleKnob.getTranslateX();
//        double endX = isLightMode ? 45 : 15;
//
//        // Animate knob
//        if (animate) {
//            TranslateTransition transition = new TranslateTransition(Duration.millis(200), toggleKnob);
//            transition.setFromX(startX);
//            transition.setToX(endX);
//            transition.play();
//        } else {
//            toggleKnob.setTranslateX(endX);
//        }
//
//        // Change background color
//        if (isLightMode) {
//            toggleBackground.setStyle("-fx-background-radius: 15; -fx-background-color: #4cd137;");
//        } else {
//            toggleBackground.setStyle("-fx-background-radius: 15; -fx-background-color: #718093;");
//        }
//
//        statusLabel.setText("Current Mode: " + (isLightMode ? "Light" : "Dark"));
//    }
//
//    private boolean getCurrentTheme() {
//        try {
//            Process readProcess = new ProcessBuilder("reg", "query",
//                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
//                    "/v", "AppsUseLightTheme").start();
//            readProcess.waitFor();
//
//            Scanner scanner = new Scanner(readProcess.getInputStream()).useDelimiter("\\A");
//            String output = scanner.hasNext() ? scanner.next() : "";
//            scanner.close();
//
//            return output.contains("0x1"); // true if Light Mode
//        } catch (Exception e) {
//            e.printStackTrace();
//            return true; // default to Light Mode
//        }
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}




/*  system tray working application  */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Scanner;

public class Main extends Application {

    private boolean isLightMode;

    @Override
    public void start(Stage primaryStage) {
//        // Minimal main window (optional)
//        Label label = new Label("Windows 11 Dark Mode Toggler");
//        StackPane root = new StackPane(label);
//        Scene scene = new Scene(root, 300, 100);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Dark Mode Toggler");
//        primaryStage.show();

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
            Image image = Toolkit.getDefaultToolkit().createImage("/icon.png"); // optional icon file

            PopupMenu popup = new PopupMenu();

            MenuItem toggleItem = new MenuItem("Toggle Dark/Light Mode");
            toggleItem.addActionListener(e -> toggleTheme());

            MenuItem openItem = new MenuItem("Open Window");
            openItem.addActionListener(e -> Platform.runLater(stage::show));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                tray.remove(tray.getTrayIcons()[0]);
                Platform.exit();
                System.exit(0);
            });

            popup.add(toggleItem);
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Dark Mode Toggler", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleTheme() {
        try {
            boolean currentMode = getCurrentTheme();
            String newValue = currentMode ? "0" : "1";

            Process writeProcess = new ProcessBuilder("reg", "add",
                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme", "/t", "REG_DWORD", "/d", newValue, "/f").start();
            writeProcess.waitFor();

            isLightMode = !currentMode;
            System.out.println("Current Mode: " + (isLightMode ? "Light" : "Dark"));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    public static void main(String[] args) {
        launch(args);
    }
}


//
//import javafx.animation.FillTransition;
//import javafx.animation.ParallelTransition;
//import javafx.animation.TranslateTransition;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//import java.awt.*;
//import java.io.IOException;
//import java.util.Scanner;
//
//public class AnimatedDarkModeToggler extends Application {
//
//    private Circle toggleKnob;
//    private Pane toggleBackground;
//    private Label statusLabel;
//    private boolean isLightMode;
//
//    private Stage primaryStage;
//
//    @Override
//    public void start(Stage stage) {
//        this.primaryStage = stage;
//
//        // Status label
//        statusLabel = new Label();
//        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        // Toggle background
//        toggleBackground = new Pane();
//        toggleBackground.setPrefSize(60, 30);
//        toggleBackground.setBackground(new Background(new BackgroundFill(Color.web("#718093"), new CornerRadii(15), Insets.EMPTY)));
//
//        // Toggle knob
//        toggleKnob = new Circle(12);
//        toggleKnob.setFill(Color.WHITE);
//        toggleKnob.setTranslateX(15);
//        toggleKnob.setTranslateY(15);
//
//        StackPane toggleStack = new StackPane(toggleBackground, toggleKnob);
//        toggleStack.setOnMouseClicked(e -> toggleTheme(true));
//
//        // Initialize current theme
//        isLightMode = getCurrentTheme();
//        updateUI(false);
//
//        VBox root = new VBox(20);
//        root.setPadding(new Insets(30));
//        root.setAlignment(Pos.CENTER);
//        root.getChildren().addAll(statusLabel, toggleStack);
//
//        Scene scene = new Scene(root, 300, 150);
//        stage.setScene(scene);
//        stage.setTitle("Windows 11 Dark Mode Toggler");
//        stage.show();
//
//        // System tray
//        if (SystemTray.isSupported()) setupSystemTray();
//
//        // Watcher thread for real-time sync
//        startThemeWatcher();
//    }
//
//    private void toggleTheme(boolean animate) {
//        try {
//            boolean currentMode = getCurrentTheme();
//            String newValue = currentMode ? "0" : "1";
//
//            Process writeProcess = new ProcessBuilder("reg", "add",
//                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
//                    "/v", "AppsUseLightTheme", "/t", "REG_DWORD", "/d", newValue, "/f").start();
//            writeProcess.waitFor();
//
//            isLightMode = !currentMode;
//            updateUI(animate);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateUI(boolean animate) {
//        double startX = toggleKnob.getTranslateX();
//        double endX = isLightMode ? 45 : 15;
//
//        Color startColor = (Color) toggleBackground.getBackground().getFills().get(0).getFill();
//        Color endColor = isLightMode ? Color.web("#4cd137") : Color.web("#718093");
//
//        if (animate) {
//            // Move knob
//            TranslateTransition slide = new TranslateTransition(Duration.millis(200), toggleKnob);
//            slide.setFromX(startX);
//            slide.setToX(endX);
//
//            // Fade background color
//            FillTransition colorFade = new FillTransition(Duration.millis(250), toggleBackground.getShape(), startColor, endColor);
//            colorFade.setOnFinished(ev -> toggleBackground.setBackground(
//                    new Background(new BackgroundFill(endColor, new CornerRadii(15), Insets.EMPTY)))
//            );
//
//            ParallelTransition animation = new ParallelTransition(slide, colorFade);
//            animation.play();
//        } else {
//            toggleKnob.setTranslateX(endX);
//            toggleBackground.setBackground(new Background(new BackgroundFill(endColor, new CornerRadii(15), Insets.EMPTY)));
//        }
//
//        statusLabel.setText("Current Mode: " + (isLightMode ? "Light" : "Dark"));
//    }
//
//    private boolean getCurrentTheme() {
//        try {
//            Process readProcess = new ProcessBuilder("reg", "query",
//                    "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
//                    "/v", "AppsUseLightTheme").start();
//            readProcess.waitFor();
//
//            Scanner scanner = new Scanner(readProcess.getInputStream()).useDelimiter("\\A");
//            String output = scanner.hasNext() ? scanner.next() : "";
//            scanner.close();
//
//            return output.contains("0x1");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return true;
//        }
//    }
//
//    private void setupSystemTray() {
//        try {
//            SystemTray tray = SystemTray.getSystemTray();
//            Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // Optional icon (16x16 or 24x24)
//
//            PopupMenu popup = new PopupMenu();
//
//            MenuItem toggleItem = new MenuItem("Toggle Dark/Light Mode");
//            toggleItem.addActionListener(e -> Platform.runLater(() -> toggleTheme(true)));
//
//            MenuItem openItem = new MenuItem("Open Window");
//            openItem.addActionListener(e -> Platform.runLater(() -> {
//                if (!primaryStage.isShowing()) primaryStage.show();
//                primaryStage.toFront();
//            }));
//
//            MenuItem exitItem = new MenuItem("Exit");
//            exitItem.addActionListener(e -> {
//                tray.remove(tray.getTrayIcons()[0]);
//                Platform.exit();
//                System.exit(0);
//            });
//
//            popup.add(toggleItem);
//            popup.add(openItem);
//            popup.addSeparator();
//            popup.add(exitItem);
//
//            TrayIcon trayIcon = new TrayIcon(image, "Dark Mode Toggler", popup);
//            trayIcon.setImageAutoSize(true);
//            tray.add(trayIcon);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startThemeWatcher() {
//        Thread watcher = new Thread(() -> {
//            boolean lastMode = isLightMode;
//            while (true) {
//                boolean currentMode = getCurrentTheme();
//                if (currentMode != lastMode) {
//                    lastMode = currentMode;
//                    Platform.runLater(() -> {
//                        isLightMode = currentMode;
//                        updateUI(true);
//                    });
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ignored) {}
//            }
//        });
//        watcher.setDaemon(true);
//        watcher.start();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
//
