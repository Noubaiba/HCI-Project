package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.AdminController;
import com.ecommerce.stockapp.model.*;
import jakarta.mail.MessagingException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;

public class AdminDashboardView {
    private final AdminController controller;
    private BorderPane root;

    public AdminDashboardView(AdminController controller) {
        this.controller = controller;
    }

    public Parent render() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setLeft(nav());
        showDashboard();
        return root;
    }

    private VBox nav() {
        VBox nav = new VBox(10);
        nav.getStyleClass().add("side-nav");
        nav.setPadding(new Insets(24));
        nav.getChildren().addAll(Ui.title("Admin"), Ui.subtitle(controller.currentUser().getName()),
                navButton("Dashboard", this::showDashboard),
                navButton("Products", this::showProducts),
                navButton("Users", this::showUsers),
                navButton("Orders", this::showOrders),
                navButton("Reports", this::showReports),
                navButton("System logs", this::showLogs));
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

    private void showDashboard() {
        DashboardStats stats = safe(controller::stats);
        if (stats == null) {
            return;
        }

        GridPane statGrid = new GridPane();
        statGrid.setHgap(16);
        statGrid.setVgap(16);
        statGrid.add(stat("Products", String.valueOf(stats.getProducts()), "Catalogue actif"), 0, 0);
        statGrid.add(stat("Users", String.valueOf(stats.getUsers()), "Comptes plateforme"), 1, 0);
        statGrid.add(stat("Orders", String.valueOf(stats.getOrders()), "Commandes totales"), 2, 0);
        statGrid.add(stat("Revenue", "$" + stats.getRevenue(), "Chiffre d'affaires"), 3, 0);

        HBox charts = new HBox(16, inventoryChart(), categoryChart());
        charts.setFillHeight(true);
        HBox.setHgrow(charts.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(charts.getChildren().get(1), Priority.ALWAYS);

        VBox insightPanel = Ui.card(
                Ui.subtitle("Operational health"),
                progressLine("Low stock pressure", stats.getLowStock(), Math.max(1, stats.getProducts())),
                progressLine("Order activity", stats.getOrders(), Math.max(1, stats.getUsers() * 3)),
                progressLine("Catalog coverage", stats.getProducts(), Math.max(1, stats.getProducts() + stats.getLowStock()))
        );

        setContent("Analytics overview", new VBox(18, statGrid, charts, insightPanel));
    }

    private VBox stat(String label, String value, String detail) {
        Label number = new Label(value);
        number.getStyleClass().add("stat-number");
        VBox card = Ui.card(Ui.subtitle(label), number, new Label(detail));
        card.setMinWidth(190);
        return card;
    }

    private VBox inventoryChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setTitle("Stock by product");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        controller.products("").stream().limit(8).forEach(product -> series.getData().add(new XYChart.Data<>(product.getName(), product.getQuantity())));
        chart.getData().add(series);
        VBox card = Ui.card(chart);
        card.getStyleClass().add("chart-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox categoryChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Products by category");
        chart.setLegendVisible(true);
        controller.products("").stream()
                .collect(java.util.stream.Collectors.groupingBy(Product::getCategoryName, java.util.stream.Collectors.counting()))
                .forEach((category, count) -> chart.getData().add(new PieChart.Data(category == null ? "Uncategorized" : category, count)));
        VBox card = Ui.card(chart);
        card.getStyleClass().add("chart-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox progressLine(String label, int value, int max) {
        Label caption = new Label(label + "  " + value + "/" + max);
        HBox track = new HBox();
        track.getStyleClass().add("progress-track");
        Region fill = new Region();
        fill.getStyleClass().add("progress-fill");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(Math.min(1.0, value / (double) max)));
        track.getChildren().add(fill);
        return new VBox(7, caption, track);
    }

    private void showProducts() {
        TextField search = Ui.text("Search products");
        TableView<Product> table = productTable();
        table.setItems(FXCollections.observableArrayList(controller.products("")));
        search.textProperty().addListener((o, old, value) -> table.setItems(FXCollections.observableArrayList(controller.products(value))));
        Button add = Ui.primary("+ Product");
        add.setOnAction(e -> {
            productDialog(null);
            table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
        });
        Button categories = Ui.secondary("+ Category");
        categories.setOnAction(e -> categoryDialog());
        Button delete = Ui.danger("Delete");
        delete.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && Ui.confirm("Delete product", "Delete " + selected.getName() + "?")) {
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
        setContent("Product management", new VBox(10, Ui.toolbar(search, add, categories, delete), table));
    }

    private void showUsers() {
        TableView<User> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(controller.users()));
        table.getColumns().add(col("Name", User::getName));
        table.getColumns().add(col("Email", User::getEmail));
        table.getColumns().add(col("Role", u -> u.getRole().name()));
        table.getColumns().add(col("Status", u -> u.getStatus().name()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button stockManager = Ui.primary("+ Stock manager");
        stockManager.setOnAction(e -> {
            createStockManagerDialog();
            table.setItems(FXCollections.observableArrayList(controller.users()));
        });
        Button activate = Ui.secondary("Activate");
        activate.setOnAction(e -> updateUser(table, UserStatus.ACTIVE));
        Button deactivate = Ui.secondary("Deactivate");
        deactivate.setOnAction(e -> updateUser(table, UserStatus.INACTIVE));
        Button block = Ui.danger("Block");
        block.setOnAction(e -> updateUser(table, UserStatus.BLOCKED));
        setContent("User administration", new VBox(10, Ui.toolbar(stockManager, activate, deactivate, block), table));
    }

    private void showOrders() {
        TableView<Order> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(controller.orders()));
        table.getColumns().add(col("Order", o -> "#" + o.getId()));
        table.getColumns().add(col("Customer", Order::getCustomerName));
        table.getColumns().add(col("Total", o -> "$" + o.getTotalPrice()));
        table.getColumns().add(col("Status", o -> o.getStatus().name()));
        table.getColumns().add(col("Date", o -> o.getDate().toString()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ComboBox<OrderStatus> status = new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));
        status.setPromptText("Set status");
        Button update = Ui.primary("Update order");
        update.setOnAction(e -> {
            Order order = table.getSelectionModel().getSelectedItem();
            if (order != null && status.getValue() != null) {
                run(() -> controller.updateOrderStatus(order, status.getValue()));
                table.setItems(FXCollections.observableArrayList(controller.orders()));
            }
        });
        setContent("All orders", new VBox(10, Ui.toolbar(status, update), table));
    }

    private void showReports() {
        TextArea report = new TextArea(controller.report());
        report.setEditable(false);
        report.getStyleClass().add("report-area");
        Button refresh = Ui.secondary("Refresh report");
        refresh.setOnAction(e -> report.setText(controller.report()));
        setContent("Reports", new VBox(10, Ui.toolbar(refresh), report));
    }

    private void showLogs() {
        ListView<String> list = new ListView<>(FXCollections.observableArrayList(controller.logs()));
        setContent("System logs", list);
    }

    private TableView<Product> productTable() {
        TableView<Product> table = new TableView<>();
        table.getColumns().add(col("Product", Product::getName));
        table.getColumns().add(col("Category", Product::getCategoryName));
        table.getColumns().add(col("Price", p -> "$" + p.getPrice()));
        table.getColumns().add(col("Qty", p -> String.valueOf(p.getQuantity())));
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
        dialog.setTitle(product == null ? "New product" : "Edit product");
        Button save = Ui.primary("Save product");
        save.setOnAction(e -> {
            Category selected = category.getValue();
            Product next = new Product(product == null ? 0 : product.getId(), name.getText(), description.getText(), imageUrl.getText(),
                    parseMoney(price.getText()), parseInt(quantity.getText()), selected == null ? 0 : selected.getId(), selected == null ? "" : selected.getName());
            run(() -> controller.saveProduct(next));
            dialog.close();
        });
        dialog.getDialogPane().setContent(Ui.card(Ui.title("Product details"), name, description, imageUrl, price, quantity, category, save));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void categoryDialog() {
        TextField name = Ui.text("Category name");
        Dialog<Void> dialog = new Dialog<>();
        Button create = Ui.primary("Create category");
        create.setOnAction(e -> {
            run(() -> controller.createCategory(name.getText()));
            dialog.close();
        });
        dialog.getDialogPane().setContent(Ui.card(Ui.title("New category"), name, create));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void createStockManagerDialog() {
        TextField name = Ui.text("Full name");
        TextField email = Ui.text("Email");
        Dialog<Void> dialog = new Dialog<>();
        Button create = Ui.primary("Create and email activation");
        create.setOnAction(e -> {
            try {
                String token = controller.createStockManager(name.getText(), email.getText());
                Ui.info("Stock manager created", "Activation email sent. Token: " + token);
                dialog.close();
            } catch (MessagingException | RuntimeException ex) {
                Ui.error(ex);
            }
        });
        dialog.getDialogPane().setContent(Ui.card(Ui.title("Create stock manager"), Ui.subtitle("The account starts inactive and receives a real activation email."), name, email, create));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void updateUser(TableView<User> table, UserStatus status) {
        User user = table.getSelectionModel().getSelectedItem();
        if (user != null) {
            run(() -> controller.updateUserStatus(user, status));
            table.setItems(FXCollections.observableArrayList(controller.users()));
        }
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

    private <T> T safe(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            Ui.error(e);
            return null;
        }
    }

    private int parseInt(String value) {
        return Integer.parseInt(value);
    }

    private BigDecimal parseMoney(String value) {
        return new BigDecimal(value);
    }
}