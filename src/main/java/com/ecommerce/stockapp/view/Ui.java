package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public final class Ui {
    private Ui() {}

    public static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("title");
        return label;
    }

    public static Label subtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("subtitle");
        return label;
    }

    public static Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    public static Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    public static Button danger(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("danger-button");
        return button;
    }

    public static VBox card(Node... children) {
        VBox box = new VBox(14, children);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(18));
        return box;
    }

    public static HBox toolbar(Node... children) {
        HBox box = new HBox(10, children);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 12, 0));
        return box;
    }

    public static HBox header(User user, String section) {
        Label sectionTitle = title(section);
        VBox sectionBox = new VBox(3, sectionTitle, subtitle("Workspace " + user.getRole().name().replace('_', ' ').toLowerCase()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane avatar = avatar(user.getName());
        Label name = new Label(user.getName());
        name.getStyleClass().add("header-name");
        Label role = new Label(user.getRole().name().replace('_', ' '));
        role.getStyleClass().add("header-role");
        VBox identity = new VBox(2, name, role);
        identity.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(14, sectionBox, spacer, avatar, identity);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    public static StackPane avatar(String name) {
        String initials = initials(name);
        Circle circle = new Circle(23, Color.web("#0f766e"));
        Label label = new Label(initials);
        label.getStyleClass().add("avatar-text");
        StackPane avatar = new StackPane(circle, label);
        avatar.getStyleClass().add("avatar");
        return avatar;
    }

    public static TextField text(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    public static PasswordField password(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        return field;
    }

    public static void info(String title, String message) {
        alert(Alert.AlertType.INFORMATION, title, message);
    }

    public static void error(Throwable throwable) {
        String message = throwable.getMessage() == null ? "Unexpected error." : throwable.getMessage();
        alert(Alert.AlertType.ERROR, "Action failed", message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    
    public static VBox card(String text) {
        return card(new Label(text));
    }
}
