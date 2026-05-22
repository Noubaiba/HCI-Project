package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.StockManagerController;
import com.ecommerce.stockapp.model.*;
import com.ecommerce.stockapp.util.IconFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockManagerDashboardView {

    private final StockManagerController controller;
    private BorderPane root;
    private VBox navBox;
    private Button activeNavButton;

    private boolean collapsed = false;
    private VBox sidebar;
    private final List<Node> collapsibleNodes = new ArrayList<>();
    private Label logoText;
    private Label sectionTitle;

    public StockManagerDashboardView(StockManagerController controller) {
        this.controller = controller;
    }

    // =========================================================
    // RENDER
    // =========================================================
    public Parent render() {
        root = new BorderPane();
        root.getStyleClass().addAll("app-root", "admin-root");
        root.setLeft(nav());
        showProducts();
        return root;
    }

    // =========================================================
    // HAMBURGER ICON
    // =========================================================
    private VBox hamburgerIcon() {
        VBox bars = new VBox(4);
        bars.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Region bar = new Region();
            bar.getStyleClass().add("shell-menu-bar");
            bars.getChildren().add(bar);
        }
        return bars;
    }

    // =========================================================
    // SIDEBAR / NAV
    // =========================================================
    private VBox nav() {
        sidebar = new VBox(26);
        sidebar.getStyleClass().add("admin-sidebar");
        sidebar.setPadding(new Insets(24, 10, 24, 20));
        sidebar.setFillWidth(true);
        sidebar.setAlignment(Pos.TOP_LEFT);

        sectionTitle = new Label("STOCK SPACE");
        sectionTitle.getStyleClass().add("admin-sidebar-eyebrow");

        // Hamburger
        Button menuButton = new Button();
        menuButton.setGraphic(hamburgerIcon());
        menuButton.getStyleClass().add("hamburger-btn");
        menuButton.setFocusTraversable(false);
        menuButton.setOnAction(e -> toggleSidebar());

        HBox menuPill = new HBox(menuButton);
        menuPill.setAlignment(Pos.CENTER);
        menuPill.setMaxWidth(Double.MAX_VALUE);

        // Logo block
        VBox logoBlock = new VBox(10, brandBlock(), sectionTitle);
        logoBlock.setAlignment(Pos.TOP_LEFT);
        logoBlock.setMaxWidth(Double.MAX_VALUE);

        // Nav buttons
        navBox = new VBox(10);
        navBox.setMaxWidth(Double.MAX_VALUE);
        navBox.getChildren().addAll(
                navButton(IconFactory.box(),   "Products",      this::showProducts),
                navButton(IconFactory.log(),   "Low Stock",     this::showLowStock),
                navButton(IconFactory.chart(), "Stock History", this::showHistory)
        );

        VBox top = new VBox(12, menuPill, logoBlock);
        top.setAlignment(Pos.TOP_LEFT);

        // Profile + logout at bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Log out");
        logout.getStyleClass().add("shell-logout-button");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> controller.logout());

        Label profileName = new Label(controller.currentUser().getName());
        profileName.getStyleClass().add("shell-profile-name");
        Label profileRole = new Label("Stock Manager");
        profileRole.getStyleClass().add("shell-profile-email");
        VBox profileInfo = new VBox(2, profileName, profileRole);

        HBox profileRow = new HBox(10, profileInfo);
        profileRow.setAlignment(Pos.CENTER_LEFT);
        profileRow.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(profileInfo, Priority.ALWAYS);

        VBox profile = new VBox(10, profileRow, logout);
        profile.getStyleClass().add("shell-profile");

        if (!collapsibleNodes.contains(profileRow)) collapsibleNodes.add(profileRow);

        sidebar.getChildren().addAll(top, navBox, spacer, profile);
        return sidebar;
    }

    // =========================================================
    // BRAND BLOCK
    // =========================================================
    private VBox brandBlock() {
        ImageView logoImage = new ImageView();
        try {
            var res = getClass().getResource("/images/stockify.png");
            if (res != null) logoImage.setImage(new Image(res.toExternalForm()));
        } catch (Exception ignored) {}
        logoImage.setFitWidth(45);
        logoImage.setFitHeight(45);
        logoImage.setPreserveRatio(true);

        logoText = new Label("Stockify");  // product name
        logoText.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #111827;");
        if (!collapsibleNodes.contains(logoText)) collapsibleNodes.add(logoText);

        HBox logoRow = new HBox(12, logoImage, logoText);
        logoRow.setId("logoRow");
        logoRow.setAlignment(Pos.CENTER_LEFT);

        VBox brand = new VBox(8, logoRow);
        brand.setId("brandBlock");
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 15, 0));
        return brand;
    }

    // =========================================================
    // TOGGLE SIDEBAR
    // =========================================================
    private void toggleSidebar() {
        collapsed = !collapsed;

        double w = collapsed ? 80 : 280;
        sidebar.setPrefWidth(w);
        sidebar.setMinWidth(w);
        sidebar.setMaxWidth(w);

        if (collapsed) {
            sidebar.setPadding(new Insets(24, 0, 24, 0));
            sidebar.setAlignment(Pos.TOP_CENTER);
        } else {
            sidebar.setPadding(new Insets(24, 10, 24, 20));
            sidebar.setAlignment(Pos.TOP_LEFT);
        }

        sectionTitle.setVisible(!collapsed);
        sectionTitle.setManaged(!collapsed);
        logoText.setVisible(!collapsed);
        logoText.setManaged(!collapsed);

        for (Node n : collapsibleNodes) {
            n.setVisible(!collapsed);
            n.setManaged(!collapsed);
        }

        navBox.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                if (btn.getGraphic() instanceof HBox hbox) {
                    hbox.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
                }
            }
        });

        if (sidebar.getChildren().get(0) instanceof VBox topContainer) {
            if (topContainer.getChildren().get(0) instanceof HBox menuPill) {
                menuPill.setAlignment(Pos.CENTER);
            }
            if (topContainer.getChildren().get(1) instanceof VBox logoBlock) {
                if (collapsed) {
                    logoBlock.setAlignment(Pos.TOP_CENTER);
                    topContainer.setAlignment(Pos.TOP_CENTER);
                    if (logoBlock.getChildren().get(0) instanceof VBox brandBlock) {
                        brandBlock.setAlignment(Pos.CENTER);
                        if (brandBlock.getChildren().get(0) instanceof HBox logoRow) {
                            logoRow.setAlignment(Pos.CENTER);
                        }
                    }
                } else {
                    logoBlock.setAlignment(Pos.TOP_LEFT);
                    topContainer.setAlignment(Pos.TOP_LEFT);
                    if (logoBlock.getChildren().get(0) instanceof VBox brandBlock) {
                        brandBlock.setAlignment(Pos.CENTER_LEFT);
                        if (brandBlock.getChildren().get(0) instanceof HBox logoRow) {
                            logoRow.setAlignment(Pos.CENTER_LEFT);
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // NAV BUTTON
    // =========================================================
    private Button navButton(Node icon, String label, Runnable action) {
        Label text = new Label(label);
        text.getStyleClass().add("admin-sidebar-nav-label");
        collapsibleNodes.add(text);

        HBox content = new HBox(12, icon, text);
        content.setAlignment(Pos.CENTER_LEFT);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.getStyleClass().add("sidebar-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getProperties().put("navText", label);

        btn.setOnAction(e -> {
            if (activeNavButton != null) activeNavButton.getStyleClass().remove("active");
            activeNavButton = btn;
            btn.getStyleClass().add("active");
            action.run();
        });
        return btn;
    }

    private void selectNav(String text) {
        if (navBox == null || text == null) return;
        navBox.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .filter(btn -> text.equals(btn.getProperties().get("navText")))
                .findFirst()
                .ifPresent(btn -> {
                    if (activeNavButton != null) activeNavButton.getStyleClass().remove("active");
                    activeNavButton = btn;
                    if (!btn.getStyleClass().contains("active")) btn.getStyleClass().add("active");
                });
    }

    // =========================================================
    // PAGES
    // =========================================================

    /** Products page — operational inventory table */
    private void showProducts() {
        selectNav("Products");

        TextField search = Ui.text("Search stock");
        TableView<Product> table = productTable();
        table.setItems(FXCollections.observableArrayList(controller.products("")));
        search.textProperty().addListener((o, old, v) ->
                table.setItems(FXCollections.observableArrayList(controller.products(v))));

        Button addStock = Ui.primary("+ Stock");
        addStock.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), false, table, search));

        Button adjust = Ui.secondary("Adjust quantity");
        adjust.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), true, table, search));

        Button addProduct = Ui.secondary("+ Product");
        addProduct.setOnAction(e -> {
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

        // KPI mini-cards at the top
        List<Product> all = controller.products("");
        int totalProducts = all.size();
        int lowStockCount = (int) all.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() < 10).count();
        int outOfStock    = (int) all.stream().filter(p -> p.getQuantity() == 0).count();
        int totalUnits    = all.stream().mapToInt(Product::getQuantity).sum();

        HBox kpiRow = new HBox(16,
                kpiCard("📦", "Total Products",  String.valueOf(totalProducts)),
                kpiCard("📊", "Total Units",     String.valueOf(totalUnits)),
                kpiCard("⚠",  "Low Stock",       String.valueOf(lowStockCount)),
                kpiCard("🚫", "Out of Stock",    String.valueOf(outOfStock))
        );
        kpiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        VBox body = new VBox(16,
                kpiRow,
                surfaceCard(
                        sectionTitle("Operational inventory", "Browse, update stock and manage products.", "📦"),
                        Ui.toolbar(search, addProduct, addStock, adjust, delete)
                ),
                surfaceCard(table)
        );
        body.getStyleClass().add("dash-body");
        setContent("Inventory", body);
    }

    /** Low stock alerts page */
    private void showLowStock() {
        selectNav("Low Stock");

        List<Product> lowProducts = controller.lowStock();

        // Overview mini-cards
        int criticalCount = (int) lowProducts.stream().filter(p -> p.getQuantity() == 0).count();
        int warningCount  = lowProducts.size() - criticalCount;

        HBox kpiRow = new HBox(16,
                kpiCard("⚠",  "Low Stock Alerts", String.valueOf(lowProducts.size())),
                kpiCard("🚫", "Out of Stock",      String.valueOf(criticalCount)),
                kpiCard("🟡", "Low but in Stock",  String.valueOf(warningCount))
        );
        kpiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Alert cards grid
        FlowPane grid = new FlowPane(16, 16);
        grid.getStyleClass().add("catalog-grid");
        if (lowProducts.isEmpty()) {
            Label empty = new Label("✅  All products are well stocked!");
            empty.getStyleClass().add("subtitle");
            grid.getChildren().add(empty);
        } else {
            lowProducts.forEach(p -> grid.getChildren().add(lowStockCard(p)));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("admin-page-scroll");

        VBox body = new VBox(16,
                kpiRow,
                surfaceCard(sectionTitle("Low stock alerts", "Products that need restocking attention.", "⚠"), scroll)
        );
        body.getStyleClass().add("dash-body");
        setContent("Low Stock", body);
    }

    /** Stock history page */
    private void showHistory() {
        selectNav("Stock History");

        List<StockMovement> movements = controller.history();

        // Overview stats — compare by type name to avoid compile errors if enum values differ
        long deliveries  = movements.stream().filter(m -> "DELIVERY".equals(m.getType().name())).count();
        long adjustments = movements.stream().filter(m -> "ADJUSTMENT".equals(m.getType().name())).count();
        long sales       = movements.stream().filter(m -> "SALE".equals(m.getType().name())).count();

        HBox kpiRow = new HBox(16,
                kpiCard("📋", "Total Movements", String.valueOf(movements.size())),
                kpiCard("🚚", "Deliveries",       String.valueOf(deliveries)),
                kpiCard("✏",  "Adjustments",      String.valueOf(adjustments)),
                kpiCard("🛒", "Sales",             String.valueOf(sales))
        );
        kpiRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        TableView<StockMovement> table = historyTable();
        table.setItems(FXCollections.observableArrayList(movements));

        VBox body = new VBox(16,
                kpiRow,
                surfaceCard(sectionTitle("Stock movements", "Full audit trail of every stock change.", "📋"), table)
        );
        body.getStyleClass().add("dash-body");
        setContent("Stock History", body);
    }

    // =========================================================
    // TABLES
    // =========================================================
    private TableView<Product> productTable() {
        TableView<Product> table = new TableView<>();
        decorateTable(table, 78);
        table.getColumns().add(productImageColumn());
        table.getColumns().add(productInfoColumn());
        table.getColumns().add(productCategoryColumn());
        table.getColumns().add(productPriceColumn());
        table.getColumns().add(productStockColumn());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private TableView<StockMovement> historyTable() {
        TableView<StockMovement> table = new TableView<>();
        decorateTable(table, 68);
        table.getColumns().add(movementProductColumn());
        table.getColumns().add(movementTypeColumn());
        table.getColumns().add(movementQuantityColumn());
        table.getColumns().add(movementDateColumn());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private void decorateTable(TableView<?> table, double rowHeight) {
        table.getStyleClass().add("admin-data-table");
        table.setFixedCellSize(rowHeight);
    }

    // ── Product table columns ──────────────────────────────────────────────────

    private TableColumn<Product, String> productImageColumn() {
        TableColumn<Product, String> col = new TableColumn<>("Image");
        col.setPrefWidth(100);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImageUrl()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                StackPane frame = new StackPane();
                frame.getStyleClass().add("admin-image-frame");
                frame.setPrefSize(60, 60);
                ImageView img = new ImageView();
                img.setFitWidth(52);
                img.setFitHeight(52);
                img.setPreserveRatio(true);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    try {
                        img.setImage(new Image(resolveImage(imageUrl), true));
                        frame.getChildren().add(img);
                    } catch (Exception ignored) {
                        frame.getChildren().add(imagePlaceholder());
                    }
                } else {
                    frame.getChildren().add(imagePlaceholder());
                }
                setText(null);
                setGraphic(frame);
            }
        });
        return col;
    }

    private TableColumn<Product, String> productInfoColumn() {
        TableColumn<Product, String> col = new TableColumn<>("Product");
        col.setPrefWidth(280);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); setText(null); return;
                }
                Product p = getTableView().getItems().get(getIndex());
                Label name = new Label(p.getName());
                name.getStyleClass().add("admin-product-name");
                name.setWrapText(true);
                String desc = (p.getDescription() == null || p.getDescription().isBlank())
                        ? "No description available" : p.getDescription();
                Label description = new Label(desc);
                description.getStyleClass().add("admin-product-description");
                description.setWrapText(true);
                description.setMaxWidth(260);
                VBox content = new VBox(5, name, description);
                content.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(content);
            }
        });
        return col;
    }

    private TableColumn<Product, String> productCategoryColumn() {
        TableColumn<Product, String> col = new TableColumn<>("Category");
        col.setPrefWidth(160);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                Label badge = new Label((item == null || item.isBlank()) ? "Uncategorized" : item);
                badge.getStyleClass().add("admin-category-badge");
                setText(null);
                setGraphic(badge);
            }
        });
        return col;
    }

    private TableColumn<Product, String> productPriceColumn() {
        TableColumn<Product, String> col = new TableColumn<>("Price");
        col.setPrefWidth(120);
        col.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPrice() == null ? "$0.00" : "$" + data.getValue().getPrice()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                Label price = new Label(item);
                price.getStyleClass().add("admin-price-label");
                setText(null);
                setGraphic(price);
            }
        });
        return col;
    }

    private TableColumn<Product, String> productStockColumn() {
        TableColumn<Product, String> col = new TableColumn<>("Stock");
        col.setPrefWidth(140);
        col.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); setText(null); return;
                }
                Product p = getTableView().getItems().get(getIndex());
                int qty = p.getQuantity();
                String badgeText  = qty == 0 ? "Out of stock" : qty + " units";
                String badgeClass = qty <= 5 ? "admin-stock-badge-low" : "admin-stock-badge-ok";
                Label stock = new Label(badgeText);
                stock.getStyleClass().add(badgeClass);
                setText(null);
                setGraphic(stock);
            }
        });
        return col;
    }

    // ── History table columns ──────────────────────────────────────────────────

    private TableColumn<StockMovement, String> movementProductColumn() {
        TableColumn<StockMovement, String> col = new TableColumn<>("Product");
        col.setPrefWidth(260);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                // Avatar circle with initials
                Label avatar = new Label(initials(item));
                avatar.getStyleClass().add("admin-user-avatar");
                Label name = new Label(item);
                name.getStyleClass().add("admin-product-name");
                name.setWrapText(true);
                HBox row = new HBox(10, avatar, name);
                row.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
            }
        });
        return col;
    }

    private TableColumn<StockMovement, String> movementTypeColumn() {
        TableColumn<StockMovement, String> col = new TableColumn<>("Type");
        col.setPrefWidth(160);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item.replace('_', ' '));
                badge.getStyleClass().add("admin-role-badge");
                setText(null);
                setGraphic(badge);
            }
        });
        return col;
    }

    private TableColumn<StockMovement, String> movementQuantityColumn() {
        TableColumn<StockMovement, String> col = new TableColumn<>("Quantity");
        col.setPrefWidth(120);
        col.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                int qty = Integer.parseInt(item);
                Label label = new Label((qty >= 0 ? "+" : "") + qty + " units");
                label.getStyleClass().add(qty >= 0 ? "admin-stock-badge-ok" : "admin-stock-badge-low");
                setText(null);
                setGraphic(label);
            }
        });
        return col;
    }

    private TableColumn<StockMovement, String> movementDateColumn() {
        TableColumn<StockMovement, String> col = new TableColumn<>("Date");
        col.setPrefWidth(180);
        col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate().toString()));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label label = new Label(item);
                label.getStyleClass().add("admin-user-id");
                setText(null);
                setGraphic(label);
            }
        });
        return col;
    }

    // =========================================================
    // DIALOGS
    // =========================================================
    private void productDialog(Product product) {
        TextField name        = Ui.text("Name");
        TextField description = Ui.text("Description");
        TextField imageUrl    = Ui.text("Image URL or /images/... path");
        TextField price       = Ui.text("Price");
        TextField quantity    = Ui.text("Quantity");
        ComboBox<Category> category = new ComboBox<>(FXCollections.observableArrayList(controller.categories()));
        category.setPromptText("Category");
        category.setMaxWidth(Double.MAX_VALUE);

        if (product != null) {
            name.setText(product.getName());
            description.setText(product.getDescription());
            imageUrl.setText(product.getImageUrl());
            price.setText(String.valueOf(product.getPrice()));
            quantity.setText(String.valueOf(product.getQuantity()));
            category.getItems().stream()
                    .filter(cat -> cat.getId() == product.getCategoryId())
                    .findFirst()
                    .ifPresent(category::setValue);
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "New Product" : "Edit Product");

        Button save = Ui.primary(product == null ? "Create product" : "Save changes");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            Category selected = category.getValue();
            Product next = new Product(
                    product == null ? 0 : product.getId(),
                    name.getText(), description.getText(), imageUrl.getText(),
                    new BigDecimal(price.getText()), Integer.parseInt(quantity.getText()),
                    selected == null ? 0 : selected.getId(),
                    selected == null ? "" : selected.getName()
            );
            run(() -> controller.saveProduct(next));
            dialog.close();
        });

        // Dialog layout matching admin style
        Label overline = new Label("STOCK FORM");
        overline.getStyleClass().add("admin-dialog-overline");
        Label badge = new Label(product == null ? "NEW" : "EDIT");
        badge.getStyleClass().add("admin-dialog-badge");
        HBox topRow = new HBox(10, overline, badge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label(product == null ? "Add a new product" : "Edit product details");
        heading.getStyleClass().add("admin-dialog-title");
        Label copy = new Label("Fill in the fields below to " + (product == null ? "create" : "update") + " a product in the catalogue.");
        copy.getStyleClass().add("admin-dialog-subtitle");
        copy.setWrapText(true);

        Region divider = new Region();
        divider.getStyleClass().add("admin-dialog-divider");
        divider.setPrefHeight(1);

        VBox form = new VBox(16,
                topRow, heading, copy, divider,
                dialogField("Name", name),
                dialogField("Description", description),
                dialogField("Image URL", imageUrl),
                new HBox(16, dialogField("Price", price), dialogField("Quantity", quantity)),
                dialogField("Category", category),
                save
        );
        ((HBox) form.getChildren().get(7)).getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        form.getStyleClass().add("admin-dialog-card");
        form.setPadding(new Insets(24));

        dialog.getDialogPane().setContent(form);
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
        dialog.setTitle(adjust ? "Adjust stock" : "Validate delivery");

        Button save = Ui.primary(adjust ? "Adjust" : "Validate delivery");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            int value = Integer.parseInt(qty.getText());
            run(() -> {
                if (adjust) controller.adjustStock(product, value);
                else        controller.addStock(product, value);
            });
            table.setItems(FXCollections.observableArrayList(controller.products(search.getText())));
            dialog.close();
        });

        Label overline = new Label("STOCK OPERATION");
        overline.getStyleClass().add("admin-dialog-overline");
        Label badge = new Label(adjust ? "ADJUST" : "DELIVERY");
        badge.getStyleClass().add("admin-dialog-badge");
        HBox topRow = new HBox(10, overline, badge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label(product.getName());
        heading.getStyleClass().add("admin-dialog-title");
        Label copy = new Label(adjust
                ? "Enter the new total quantity for this product."
                : "Enter the quantity received in this delivery.");
        copy.getStyleClass().add("admin-dialog-subtitle");
        copy.setWrapText(true);

        Region divider = new Region();
        divider.getStyleClass().add("admin-dialog-divider");
        divider.setPrefHeight(1);

        // Current stock badge
        String badgeClass = product.getQuantity() == 0 ? "dash-badge-out"
                : product.getQuantity() < 10 ? "dash-badge-low" : "dash-badge-ok";
        Label currentBadge = new Label("Current stock: " + product.getQuantity());
        currentBadge.getStyleClass().addAll("dash-stock-badge", badgeClass);

        VBox form = new VBox(16,
                topRow, heading, copy, currentBadge, divider,
                dialogField(adjust ? "New quantity" : "Received quantity", qty),
                save
        );
        form.getStyleClass().add("admin-dialog-card");
        form.setPadding(new Insets(24));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // =========================================================
    // LOW STOCK CARD
    // =========================================================
    private VBox lowStockCard(Product p) {
        Label catLabel = new Label(p.getCategoryName() == null ? "—" : p.getCategoryName());
        catLabel.getStyleClass().add("dash-product-category");

        Label nameLabel = new Label(p.getName());
        nameLabel.getStyleClass().add("dash-product-name");
        nameLabel.setWrapText(true);

        String badgeText  = p.getQuantity() == 0 ? "Out of Stock" : "Low — " + p.getQuantity() + " left";
        String badgeClass = p.getQuantity() == 0 ? "dash-badge-out" : "dash-badge-low";
        Label badge = new Label(badgeText);
        badge.getStyleClass().addAll("dash-stock-badge", badgeClass);

        // Progress bar
        double ratio = Math.min(1.0, p.getQuantity() / 20.0);
        HBox track = new HBox();
        track.getStyleClass().add("dash-progress-track");
        Region fill = new Region();
        fill.getStyleClass().addAll("dash-progress-fill", p.getQuantity() == 0 ? "out" : "low");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(ratio));
        track.getChildren().add(fill);

        VBox card = new VBox(10, catLabel, nameLabel, badge, track);
        card.getStyleClass().add("dash-product-card");
        card.setPrefWidth(260);
        return card;
    }

    // =========================================================
    // KPI CARD
    // =========================================================
    private VBox kpiCard(String icon, String label, String value) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().addAll("dash-kpi-icon", "blue");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, iconLabel, spacer);
        top.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("dash-kpi-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("dash-kpi-value");

        VBox card = new VBox(10, top, labelNode, valueNode);
        card.getStyleClass().add("dash-kpi-card");
        return card;
    }

    // =========================================================
    // CELL HELPERS
    // =========================================================
    private String resolveImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return "";
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("file:"))
            return imageUrl;
        if (imageUrl.startsWith("/")) {
            var resource = getClass().getResource(imageUrl);
            if (resource != null) return resource.toExternalForm();
        }
        return new java.io.File(imageUrl).toURI().toString();
    }

    private Label imagePlaceholder() {
        Label placeholder = new Label("IMG");
        placeholder.getStyleClass().add("admin-image-placeholder");
        return placeholder;
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    // =========================================================
    // LAYOUT HELPERS
    // =========================================================
    private void setContent(String title, Node content) {
        VBox page = new VBox(18, Ui.header(controller.currentUser(), title), content);
        page.setPadding(new Insets(28));
        VBox.setVgrow(content, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("admin-page-scroll");
        root.setCenter(scrollPane);
    }

    private VBox surfaceCard(Node... nodes) {
        VBox card = new VBox(16, nodes);
        card.getStyleClass().add("admin-surface-card");
        card.setPadding(new Insets(20));
        return card;
    }

    private VBox sectionTitle(String title, String subtitle, String iconText) {
        StackPane bubble = new StackPane(new Label(iconText));
        bubble.getStyleClass().add("admin-icon-bubble");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title");

        HBox titleRow = new HBox(10, bubble, titleLabel);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label s = new Label(subtitle);
        s.getStyleClass().add("subtitle");
        s.setWrapText(true);
        return new VBox(4, titleRow, s);
    }

    private VBox dialogField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("admin-dialog-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return new VBox(8, label, field);
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