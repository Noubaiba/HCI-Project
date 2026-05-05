package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.StockManagerController;
import com.ecommerce.stockapp.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;

public class StockManagerDashboardView {
    private final StockManagerController controller;
    private BorderPane root;

    public StockManagerDashboardView(StockManagerController controller) {
        this.controller = controller;
    }

    public Parent render() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(nav());
        showProducts();
        return root;
    }

    private VBox nav() {
        VBox nav = new VBox(10);
        nav.getStyleClass().add("side-nav");
        nav.setPadding(new Insets(24));
        nav.getChildren().addAll(Ui.title("Stock"), Ui.subtitle(controller.currentUser().getName()),
                navButton("Products", this::showProducts),
                navButton("Low stock", this::showLowStock),
                navButton("Stock history", this::showHistory));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button logout = Ui.danger("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> controller.logout());
        nav.getChildren().addAll(spacer, logout);
        return nav;
    }

    private Button navButton(String text, Runnable action) {
        Button button = Ui.secondary(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void showProducts() {
        TextField search = Ui.text("Search stock");
        TableView<Product> table = productTable();
        table.setItems(FXCollections.observableArrayList(controller.products("")));
        search.textProperty().addListener((o, old, value) -> table.setItems(FXCollections.observableArrayList(controller.products(value))));
        Button addStock = Ui.primary("+ Stock");
        addStock.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), false, table, search));
        Button adjust = Ui.secondary("Adjust quantity");
        adjust.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), true, table, search));
        Button product = Ui.secondary("+ Product");
        product.setOnAction(e -> {
            productDialog(null);
            table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
        });
        Button delete = Ui.danger("Delete obsolete");
        delete.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && Ui.confirm("Delete product", "Delete obsolete product " + selected.getName() + "?")) {
                run(() -> controller.deleteProduct(selected));
                table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
            }
        });
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    productDialog(row.getItem());
                    table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
                }
            });
            return row;
        });
        setContent("Operational inventory", new VBox(10, Ui.toolbar(search, product, addStock, adjust, delete), table));
    }

    private void showLowStock() {
        FlowPane alerts = new FlowPane(14, 14);
        controller.lowStock().forEach(p -> alerts.getChildren().add(Ui.card(Ui.subtitle(p.getCategoryName()), new Label(p.getName()), new Label("Quantity: " + p.getQuantity()))));
        setContent("Low stock alerts", alerts);
    }

    private void showHistory() {
        TableView<StockMovement> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(controller.history()));
        table.getColumns().add(col("Product", StockMovement::getProductName));
        table.getColumns().add(col("Type", m -> m.getType().name()));
        table.getColumns().add(col("Quantity", m -> String.valueOf(m.getQuantity())));
        table.getColumns().add(col("Date", m -> m.getDate().toString()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        setContent("Stock history", table);
    }

    private TableView<Product> productTable() {
        TableView<Product> table = new TableView<>();
        table.getColumns().add(col("Product", Product::getName));
        table.getColumns().add(col("Category", Product::getCategoryName));
        table.getColumns().add(col("Price", p -> "$" + p.getPrice()));
        table.getColumns().add(col("Quantity", p -> String.valueOf(p.getQuantity())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private void productDialog(Product product) {
        TextField name = Ui.text("Name");
        TextField description = Ui.text("Description");
        TextField imageUrl = Ui.text("Image URL or /images/... path");
        TextField price = Ui.text("Price");
        TextField quantity = Ui.text("Quantity");
        ComboBox<Category> category = new ComboBox<>(FXCollections.observableArrayList(controller.categories()));
        category.setPromptText("Category");
        if (product != null) {
            name.setText(product.getName());
            description.setText(product.getDescription());
            imageUrl.setText(product.getImageUrl());
            price.setText(String.valueOf(product.getPrice()));
            quantity.setText(String.valueOf(product.getQuantity()));
            category.getItems().stream().filter(c -> c.getId() == product.getCategoryId()).findFirst().ifPresent(category::setValue);
        }
        Dialog<Void> dialog = new Dialog<>();
        Button save = Ui.primary("Save product");
        save.setOnAction(e -> {
            Category selected = category.getValue();
            Product next = new Product(product == null ? 0 : product.getId(), name.getText(), description.getText(), imageUrl.getText(),
                    new BigDecimal(price.getText()), Integer.parseInt(quantity.getText()), selected == null ? 0 : selected.getId(), selected == null ? "" : selected.getName());
            run(() -> controller.saveProduct(next));
            dialog.close();
        });
        dialog.getDialogPane().setContent(Ui.card(Ui.title("Product details"), name, description, imageUrl, price, quantity, category, save));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void stockDialog(Product product, boolean adjust, TableView<Product> table, TextField search) {
        if (product == null) {
            Ui.info("Choose product", "Select a product first.");
            return;
        }
        TextField qty = Ui.text(adjust ? "New quantity" : "Received quantity");
        Dialog<Void> dialog = new Dialog<>();
        Button save = Ui.primary(adjust ? "Adjust" : "Validate delivery");
        save.setOnAction(e -> {
            int value = Integer.parseInt(qty.getText());
            run(() -> {
                if (adjust) {
                    controller.adjustStock(product, value);
                } else {
                    controller.addStock(product, value);
                }
            });
            table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
            dialog.close();
        });
        dialog.getDialogPane().setContent(Ui.card(Ui.title(product.getName()), qty, save));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void setContent(String title, javafx.scene.Node content) {
        VBox page = new VBox(18, Ui.header(controller.currentUser(), title), content);
        page.setPadding(new Insets(28));
        root.setCenter(page);
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> mapper) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(mapper.apply(data.getValue())));
        return column;
    }

    private void run(Runnable action) {
        try {
            action.run();
            Ui.info("Done", "Action completed successfully.");
        } catch (RuntimeException e) {
            Ui.error(e);
        }
    }
}