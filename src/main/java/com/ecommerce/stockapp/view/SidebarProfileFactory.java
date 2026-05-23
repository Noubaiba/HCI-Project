package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.util.IconFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.StageStyle;

public final class SidebarProfileFactory {
    private SidebarProfileFactory() {}

    public static VBox create(User user, Runnable profileAction, Runnable logoutAction) {
        HBox profileContainer = profileRow(user);
        ContextMenu menu = profileMenu(profileContainer, profileAction, logoutAction);
        profileContainer.setOnMouseClicked(e -> menu.show(profileContainer, Side.TOP, 0, -10));

        VBox wrapper = new VBox(profileContainer);
        wrapper.getStyleClass().add("shell-profile-wrapper");
        return wrapper;
    }

    public static VBox createGuest(Runnable loginAction, Runnable registerAction) {
        Label title = new Label("Guest mode");
        title.getStyleClass().add("shell-profile-name");

        Label subtitle = new Label("Browse freely, sign up to order.");
        subtitle.getStyleClass().add("shell-profile-email");
        subtitle.setWrapText(true);

        Button login = new Button("Sign In");
        login.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;");
        login.setOnAction(e -> loginAction.run());

        Button register = new Button("Sign Up");
        register.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;");
        register.setOnAction(e -> registerAction.run());

        HBox actions = new HBox(10, login, register);
        VBox wrapper = new VBox(10, title, subtitle, actions);
        wrapper.setPadding(new Insets(14));
        wrapper.getStyleClass().add("shell-profile-wrapper");
        return wrapper;
    }

    private static HBox profileRow(User user) {
        var avatar = Ui.avatar(user.getName());

        Label name = new Label(user.getName());
        name.getStyleClass().add("shell-profile-name");
        Label email = new Label(user.getEmail());
        email.getStyleClass().add("shell-profile-email");
        VBox text = new VBox(2, name, email);
        text.setAlignment(Pos.CENTER_LEFT);

        Label arrow = new Label("⌄");
        arrow.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, avatar, text, spacer, arrow);
        row.getStyleClass().add("shell-profile");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(Cursor.HAND);
        return row;
    }

    private static ContextMenu profileMenu(HBox owner, Runnable profileAction, Runnable logoutAction) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("stockify-profile-popup");

        CustomMenuItem profile = item(IconFactory.profileIcon(), "My Profile", "#475569", null);
        profile.setOnAction(e -> profileAction.run());

        CustomMenuItem settings = item(IconFactory.box(), "Settings", "#475569", null);

        SVGPath logoutIcon = logoutIcon();
        CustomMenuItem logout = item(logoutIcon, "Logout", "#ef4444", "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        logout.setOnAction(e -> showLogoutDialog(owner, logoutAction));

        menu.getItems().addAll(profile, settings, new SeparatorMenuItem(), logout);
        return menu;
    }

    private static CustomMenuItem item(SVGPath icon, String text, String iconColor, String labelStyle) {
        icon.setFill(Color.web(iconColor));
        HBox row = new HBox(12, icon, new Label(text));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        if (labelStyle != null) {
            ((Label) row.getChildren().get(1)).setStyle(labelStyle);
        }
        CustomMenuItem item = new CustomMenuItem(row);
        item.setHideOnClick(true);
        return item;
    }

    private static SVGPath logoutIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M16 17v-3H9v-4h7V7l5 5-5 5M14 2a2 2 0 0 0-2 2v2h2V4h6v16h-6v-2h-2v2a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2h-6z");
        return icon;
    }

    private static void showLogoutDialog(HBox owner, Runnable logoutAction) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(StageStyle.UNDECORATED);

        if (owner.getScene() != null) {
            dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }
        dialog.getDialogPane().getStyleClass().add("stockify-custom-dialog");

        VBox dialogContent = new VBox(16);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setPrefWidth(340);
        dialogContent.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        SVGPath dialogIcon = logoutIcon();
        dialogIcon.setFill(Color.web("#ef4444"));
        dialogIcon.setScaleX(1.8);
        dialogIcon.setScaleY(1.8);

        Label title = new Label("Logout");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label message = new Label("Are you sure you want to logout?\nYou will be returned to the login screen.");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-text-alignment: center;");

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancel.setOnAction(e -> {
            dialog.setResult(ButtonType.CANCEL);
            dialog.close();
        });

        Button confirm = new Button("Yes, Logout");
        confirm.setStyle("-fx-background-color: #ef4444; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        confirm.setOnAction(e -> {
            dialog.setResult(ButtonType.OK);
            dialog.close();
        });

        HBox actions = new HBox(12, cancel, confirm);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(8, 0, 0, 0));

        dialogContent.getChildren().addAll(dialogIcon, title, message, actions);
        dialog.getDialogPane().setContent(dialogContent);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                logoutAction.run();
            }
        });
    }
}
