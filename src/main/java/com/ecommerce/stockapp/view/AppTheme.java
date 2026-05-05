package com.ecommerce.stockapp.view;

import javafx.scene.Scene;

public final class AppTheme {
    private AppTheme() {}

    public static void apply(Scene scene) {
        scene.getStylesheets().add(AppTheme.class.getResource("/styles/app.css").toExternalForm());
    }
}
