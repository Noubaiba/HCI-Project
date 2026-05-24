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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.application.Platform;

public class StockManagerDashboardView {

    private static final int PRODUCT_PAGE_SIZE = 10;
    private static final int HISTORY_PAGE_SIZE = 10;

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

    public Parent render() {
        root = new BorderPane();
        root.getStyleClass().addAll("app-root", "stock-manager-root");
        root.setLeft(nav());
        showProducts();
        return root;
    }

    // ═══════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════

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

    private VBox nav() {
        sidebar = new VBox(26);
        sidebar.getStyleClass().add("admin-sidebar");
        sidebar.setPadding(new Insets(24, 10, 24, 20));
        sidebar.setFillWidth(true);
        sidebar.setAlignment(Pos.TOP_LEFT);

        sectionTitle = new Label("STOCK SPACE");
        sectionTitle.getStyleClass().add("admin-sidebar-eyebrow");

        Button menuButton = new Button();
        menuButton.setGraphic(hamburgerIcon());
        menuButton.getStyleClass().add("hamburger-btn");
        menuButton.setFocusTraversable(false);
        menuButton.setOnAction(e -> toggleSidebar());

        HBox menuPill = new HBox(menuButton);
        menuPill.setAlignment(Pos.CENTER);
        menuPill.setMaxWidth(Double.MAX_VALUE);

        VBox logoBlock = new VBox(10, brandBlock(), sectionTitle);
        logoBlock.setAlignment(Pos.TOP_LEFT);
        logoBlock.setMaxWidth(Double.MAX_VALUE);

        navBox = new VBox(10);
        navBox.setMaxWidth(Double.MAX_VALUE);
        navBox.getChildren().addAll(
                navButton(IconFactory.box(), "Products", this::showProducts),
                navButton(IconFactory.log(), "Low Stock", this::showLowStock),
                navButton(IconFactory.chart(), "Stock History", this::showHistory),
                navButton(IconFactory.profileIcon(), "Profile", this::showProfile)
        );

        VBox top = new VBox(12, menuPill, logoBlock);
        top.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox profile = SidebarProfileFactory.create(
                controller.currentUser(),
                this::showProfile,
                controller::logout
        );
        collapsibleNodes.add(profile);

        sidebar.getChildren().addAll(top, navBox, spacer, profile);
        return sidebar;
    }

    private VBox brandBlock() {
        ImageView logoImage = new ImageView();
        try {
            var res = getClass().getResource("/images/Stockify.png");
            if (res != null) logoImage.setImage(new Image(res.toExternalForm()));
        } catch (Exception ignored) {}
        logoImage.setFitWidth(45);
        logoImage.setFitHeight(45);
        logoImage.setPreserveRatio(true);

        logoText = new Label("Stockify");
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
            if (node instanceof Button btn && btn.getGraphic() instanceof HBox hbox) {
                hbox.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
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
    // ═══════════════════════════════════════════════
    //  PRODUCTS PAGE
    // ═══════════════════════════════════════════════

    private void showProducts() {
        selectNav("Products");
        List<Product> allProducts = controller.products("");
        final String[] activeFilter = {"all"};

        // ── Topbar ──
        TextField search = new TextField();
        search.setPromptText("Search stock...");
        search.getStyleClass().add("search-field");
        HBox topbar = managerTopbar(
                "Operational inventory",
                "Track product availability, restock alerts, and inventory actions.",
                search
        );

        // ── Stats ──
        HBox stats = buildStatsRow(allProducts);

        // ── Table ──
        TableView<Product> table = buildProductTable();
        Pagination pagination = new Pagination();
        pagination.getStyleClass().add("table-pagination");
        refreshProductPage(table, pagination, filteredProducts("", activeFilter[0]));
        pagination.currentPageIndexProperty().addListener((o, old, val) ->
                refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0])));
        search.textProperty().addListener((o, old, val) -> {
            pagination.setCurrentPageIndex(0);
            refreshProductPage(table, pagination, filteredProducts(val, activeFilter[0]));
        });

        HBox tabBar = buildTabBar(activeFilter[0], filter -> {
            activeFilter[0] = filter;
            pagination.setCurrentPageIndex(0);
            refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]));
        });

        // ── Toolbar buttons ──
        Button btnAdd    = styledBtn("+ Product",        "btn-primary");
        Button btnStock  = styledBtn("+ Add stock",      "btn-secondary");
        Button btnAdjust = styledBtn("Adjust qty",       "btn-secondary");
        Button btnDelete = styledBtn("Delete obsolete",  "btn-danger");

        btnAdd.setOnAction(e -> {
            productDialog(null);
            pagination.setCurrentPageIndex(0);
            refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]));
        });
        btnStock.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), false,
                () -> refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]))));
        btnAdjust.setOnAction(e -> stockDialog(table.getSelectionModel().getSelectedItem(), true,
                () -> refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]))));
        btnDelete.setOnAction(e -> {
            Product sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && confirmDialog("Delete product",
                    "Delete obsolete product \"" + sel.getName() + "\"?")) {
                runAction(() -> controller.deleteProduct(sel));
                refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]));
            }
        });

        Region tbSpacer = new Region(); HBox.setHgrow(tbSpacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, btnAdd, btnStock, btnAdjust, tbSpacer, btnDelete);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Double-click to edit ──
        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    productDialog(row.getItem());
                    refreshProductPage(table, pagination, filteredProducts(search.getText(), activeFilter[0]));
                }
            });
            return row;
        });
        // ── Table shell: pagination stays visible while the table uses remaining space ──
        BorderPane tableWrap = new BorderPane();
        tableWrap.getStyleClass().add("table-wrapper");
        tableWrap.setCenter(table);
        tableWrap.setBottom(pagination);
        BorderPane.setMargin(pagination, new Insets(10, 0, 0, 0));
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(tableWrap, Priority.ALWAYS);

        VBox content = new VBox(0, topbar, stats, tabBar, toolbar, tableWrap);
        content.getStyleClass().add("main-content");

        ScrollPane pageScroll = new ScrollPane(content);
        pageScroll.getStyleClass().add("manager-page-scroll");
        pageScroll.setFitToWidth(true);
        pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(pageScroll);
    }

    private List<Product> filteredProducts(String search, String filter) {
        return controller.products(search).stream()
                .filter(p -> switch (filter) {
                    case "in" -> p.getQuantity() > 0;
                    case "low" -> p.getQuantity() > 0 && p.getQuantity() <= 5;
                    case "obsolete" -> p.getQuantity() == 0;
                    default -> true;
                })
                .toList();
    }

    private void refreshProductPage(TableView<Product> table, Pagination pagination, List<Product> products) {
        int pageCount = Math.max(1, (int) Math.ceil(products.size() / (double) PRODUCT_PAGE_SIZE));
        if (pagination.getPageCount() != pageCount) {
            pagination.setPageCount(pageCount);
        }

        int pageIndex = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        if (pageIndex != pagination.getCurrentPageIndex()) {
            pagination.setCurrentPageIndex(pageIndex);
        }

        int from = pageIndex * PRODUCT_PAGE_SIZE;
        int to = Math.min(from + PRODUCT_PAGE_SIZE, products.size());
        table.setItems(FXCollections.observableArrayList(products.subList(from, to)));
    }

    private HBox buildStatsRow(List<Product> products) {
        int total   = products.size();
        long low    = products.stream().filter(p -> p.getQuantity() > 0 && p.getQuantity() <= 5).count();
        long out    = products.stream().filter(p -> p.getQuantity() == 0).count();
        int inStock = (int)(total - out);

        HBox row = new HBox(16);
        row.getStyleClass().add("stats-row");
        row.getChildren().addAll(
                statCard("Total products", String.valueOf(total), inStock + " in stock",     "badge-blue"),
                statCard("Low stock",      String.valueOf(low),   "Need restock",             "badge-amber"),
                statCard("In stock",       String.valueOf(inStock),"All good",                "badge-green"),
                statCard("Out of stock",   String.valueOf(out),   out > 0 ? "Action required" : "None",
                        out > 0 ? "badge-red" : "badge-green")
        );
        return row;
    }

    private VBox statCard(String labelText, String value, String badge, String badgeStyle) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label bdg = new Label(badge);
        bdg.getStyleClass().addAll("stat-badge", badgeStyle);

        VBox card = new VBox(8, lbl, val, bdg);
        card.getStyleClass().addAll("stat-card", badgeStyle.replace("badge-", "stat-card-"));
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private HBox buildTabBar(String activeFilter, Consumer<String> onFilter) {
        HBox bar = new HBox(0);
        bar.getStyleClass().add("tab-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        String[][] tabs = {
                {"all", "All products"},
                {"in", "In stock"},
                {"low", "Low stock"},
                {"obsolete", "Obsolete"}
        };
        for (String[] item : tabs) {
            Label tab = new Label(item[1]);
            tab.getStyleClass().add("tab");
            if (item[0].equals(activeFilter)) {
                tab.getStyleClass().add("tab-active");
            }
            tab.setOnMouseClicked(e -> {
                bar.getChildren().forEach(node -> node.getStyleClass().remove("tab-active"));
                tab.getStyleClass().add("tab-active");
                onFilter.accept(item[0]);
            });
            bar.getChildren().add(tab);
        }
        return bar;
    }

    private TableView<Product> buildProductTable() {
        TableView<Product> table = new TableView<>();
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setFixedCellSize(78);
        table.setPrefHeight(575);
        table.setMinHeight(575);
        table.setMaxHeight(575);


        // Product (name + SKU)
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setMinWidth(330);
        nameCol.setPrefWidth(430);
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Product p = getTableView().getItems().get(getIndex());
                Label nm  = new Label(p.getName());
                nm.setWrapText(true);
                nm.getStyleClass().add("cell-name");
                Label sku = new Label("SKU #" + String.format("%06d", p.getId())); sku.getStyleClass().add("cell-sub");
                nm.setMaxWidth(360);
                VBox text = new VBox(3, nm, sku);
                HBox row = new HBox(text);
                HBox.setHgrow(text, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row); setText(null);
            }
        });

        // Category pill
        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryName()));
        catCol.setMinWidth(180);
        catCol.setPrefWidth(220);
        catCol.setMaxWidth(220);

        catCol.setStyle("-fx-alignment: CENTER;");

        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label pill = new Label(item);
                pill.getStyleClass().addAll("category-pill", categoryStyle(item));
                pill.setAlignment(Pos.CENTER);

                HBox box = new HBox(pill);
                box.setAlignment(Pos.CENTER);

                setAlignment(Pos.CENTER);
                setGraphic(box);
                setText(null);
            }
        });

        // Price
        // Price
        TableColumn<Product, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getPrice()));

        priceCol.setMinWidth(130);
        priceCol.setPrefWidth(150);
        priceCol.setMaxWidth(180);
        priceCol.setStyle("-fx-alignment: CENTER;");

        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setAlignment(Pos.CENTER);
                setText(item);
                setGraphic(null);

                if (!getStyleClass().contains("cell-price")) {
                    getStyleClass().add("cell-price");
                }
            }
        });

        // Quantity bar
        TableColumn<Product, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        qtyCol.setMaxWidth(160); qtyCol.setMinWidth(120);
        qtyCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                int qty = Integer.parseInt(item);

                Region bar = new Region();
                double pct = Math.min((double) qty / 100, 1.0);
                bar.setPrefWidth(88 * pct);
                bar.setPrefHeight(4);
                bar.getStyleClass().add("qty-bar");
                bar.getStyleClass().add(qtyBarStyle(qty));

                StackPane bg = new StackPane(bar);
                bg.getStyleClass().add("qty-bar-bg");
                bg.setPrefSize(88, 4); bg.setAlignment(Pos.CENTER_LEFT);

                Label num = new Label(item);
                num.getStyleClass().add("qty-num");
                num.getStyleClass().add(qtyTextStyle(qty));

                HBox hb = new HBox(12, bg, num); hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb); setText(null);
            }
        });

        // Actions
        TableColumn<Product, Void> actCol = new TableColumn<>("Actions");
        actCol.setMaxWidth(150); actCol.setMinWidth(120);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button edit = makeIconBtn("✎", "Edit product");
            private final Button del  = makeIconBtn("×", "Delete product");
            { del.getStyleClass().add("icon-btn-danger"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                edit.setOnAction(e -> {
                    productDialog(getTableView().getItems().get(getIndex()));
                    getTableView().refresh();
                });
                del.setOnAction(e -> {
                    Product p = getTableView().getItems().get(getIndex());
                    if (confirmDialog("Delete", "Delete \"" + p.getName() + "\"?")) {
                        runAction(() -> controller.deleteProduct(p));
                        getTableView().getItems().remove(p);
                    }
                });
                HBox hb = new HBox(6, edit, del); hb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(hb);
            }
        });

        table.getColumns().addAll(nameCol, catCol, priceCol, qtyCol, actCol);
        return table;
    }

    private String categoryStyle(String category) {
        if (category == null || category.isBlank()) return "category-pill-blue";
        return switch (category.trim()) {
            case "Beauté & Santé"  -> "category-pill-amber";
            case "Bébé & Jouets"  -> "category-pill-purple";
            case "Électroménager"    -> "category-pill-teal";
            case "Informatique"      -> "category-pill-pink";
            case "Jeux vidéos "         -> "category-pill-green";
            case "Maison"     -> "category-pill-rose";
            case "Sports & Loisirs"       -> "category-pill-indigo";
            case "Supermarché"    -> "category-pill-orange";
            case "Téléphone & Tablette"        -> "category-pill-cyan";
            case "TV & HIGH TECH"       -> "category-pill-lime";
            case "Vêtements & Chaussures" -> "category-pill-blue";
            default             -> "category-pill-blue";
        };
    }

    private String qtyBarStyle(int qty) {
        if (qty <= 3) return "qty-bar-red";
        if (qty <= 10) return "qty-bar-amber";
        if (qty >= 60) return "qty-bar-green";
        return "qty-bar-blue";
    }

    private String qtyTextStyle(int qty) {
        if (qty <= 3) return "qty-red";
        if (qty <= 10) return "qty-amber";
        if (qty >= 60) return "qty-green";
        return "qty-blue";
    }
    private void showLowStock() {
        selectNav("Low Stock");
        List<Product> lowProducts = controller.lowStock();

        HBox topbar = managerTopbar(
                "Low stock alerts",
                "Prioritize restock actions before products go unavailable."
        );

        long out = lowProducts.stream().filter(p -> p.getQuantity() == 0).count();

        HBox overview = new HBox(16,
                statCard("Alert products", String.valueOf(lowProducts.size()), "Needs review", "badge-amber"),
                statCard("Out of stock", String.valueOf(out), out > 0 ? "Urgent" : "Clear", out > 0 ? "badge-red" : "badge-green"),
                statCard("Restock queue", String.valueOf(Math.max(0, lowProducts.size() - out)), "Ready to receive", "badge-blue")
        );
        overview.getStyleClass().add("stats-row");

        GridPane alerts = new GridPane();
        alerts.setHgap(16);
        alerts.setVgap(16);
        alerts.setPadding(new Insets(18, 28, 24, 28));
        alerts.getStyleClass().add("low-stock-content");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.333);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.333);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.333);

        alerts.getColumnConstraints().addAll(col1, col2, col3);

        if (lowProducts.isEmpty()) {
            Label emptyTitle = new Label("Stock health is good");
            emptyTitle.getStyleClass().add("empty-title");

            Label emptyCopy = new Label("No product needs restock right now.");
            emptyCopy.getStyleClass().add("empty-copy");

            VBox empty = new VBox(8, emptyTitle, emptyCopy);
            empty.getStyleClass().add("empty-state-card");

            alerts.add(empty, 0, 0, 3, 1);
        } else {
            int index = 0;

            for (Product p : lowProducts) {
                Label cat = new Label(p.getCategoryName());
                cat.getStyleClass().add("category-pill");

                Label name = new Label(p.getName());
                name.getStyleClass().add("cell-name");
                name.setWrapText(true);

                Label sku = new Label("SKU #" + String.format("%06d", p.getId()));
                sku.getStyleClass().add("cell-sub");

                Label qty = new Label(p.getQuantity() == 0 ? "Out of stock" : "Only " + p.getQuantity() + " left");
                qty.getStyleClass().addAll(
                        "alert-status",
                        p.getQuantity() == 0 ? "alert-status-red" : "alert-status-amber"
                );

                Button add = styledBtn("+ Add stock", "btn-secondary");
                Button adjust = styledBtn("Adjust", "btn-secondary");

                add.setOnAction(e -> stockDialog(p, false, this::showLowStock));
                adjust.setOnAction(e -> stockDialog(p, true, this::showLowStock));

                HBox actions = new HBox(8, add, adjust);

                VBox card = new VBox(10, cat, name, sku, qty, actions);
                card.getStyleClass().add("alert-card");

                card.setMaxWidth(Double.MAX_VALUE);
                GridPane.setFillWidth(card, true);

                int col = index % 3;
                int row = index / 3;

                alerts.add(card, col, row);
                index++;
            }
        }

        VBox content = new VBox(0, topbar, overview, alerts);
        content.getStyleClass().add("main-content");

        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("manager-page-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroll);
    }

    private void resizeLowStockCards(FlowPane alerts) {
        double availableWidth = alerts.getWidth();

        if (availableWidth <= 0) {
            return;
        }

        double hGap = alerts.getHgap();
        Insets padding = alerts.getPadding();

        double leftRightPadding = padding.getLeft() + padding.getRight();

        // 3 cartes par ligne = 2 espaces entre les cartes
        double cardWidth = (availableWidth - leftRightPadding - (hGap * 2)) / 3;

        // Largeur minimale pour éviter des cartes trop petites
        cardWidth = Math.max(cardWidth, 220);

        for (Node node : alerts.getChildren()) {
            if (node instanceof VBox card && card.getStyleClass().contains("alert-card")) {
                card.setPrefWidth(cardWidth);
                card.setMinWidth(cardWidth);
                card.setMaxWidth(cardWidth);
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  HISTORY PAGE
    // ═══════════════════════════════════════════════

    private void showHistory() {
        selectNav("Stock History");
        List<StockMovement> history = controller.history();
        long in = history.stream().filter(m -> m.getType().name().equalsIgnoreCase("IN")).count();
        long out = history.stream().filter(m -> m.getType().name().equalsIgnoreCase("OUT")).count();
        long adjust = history.size() - in - out;
        final String[] activeFilter = {"all"};

        HBox topbar = managerTopbar(
                "Stock history",
                "Audit incoming deliveries, outgoing stock, and manual quantity changes."
        );

        HBox overview = new HBox(16,
                statCard("Movements", String.valueOf(history.size()), "Total records", "badge-blue"),
                statCard("Stock in", String.valueOf(in), "Received", "badge-green"),
                statCard("Stock out", String.valueOf(out), "Removed", "badge-red"),
                statCard("Adjustments", String.valueOf(adjust), "Manual edits", "badge-amber")
        );
        overview.getStyleClass().add("stats-row");

        TableView<StockMovement> table = new TableView<>();
        table.getStyleClass().addAll("modern-table", "history-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(720);

        Pagination historyPagination = new Pagination();
        historyPagination.getStyleClass().add("table-pagination");
        refreshHistoryPage(table, historyPagination, filteredHistory(history, activeFilter[0]));
        historyPagination.currentPageIndexProperty().addListener((o, old, val) ->
                refreshHistoryPage(table, historyPagination, filteredHistory(history, activeFilter[0])));

        HBox historyTabs = buildHistoryTabBar(activeFilter[0], filter -> {
            activeFilter[0] = filter;
            historyPagination.setCurrentPageIndex(0);
            refreshHistoryPage(table, historyPagination, filteredHistory(history, activeFilter[0]));
        });

        // Product column
        TableColumn<StockMovement, String> historyProductCol = strCol("Product", StockMovement::getProductName);
        historyProductCol.setPrefWidth(590);
        historyProductCol.setMinWidth(460);
        historyProductCol.setCellFactory(col -> wrapTextCell());
        table.getColumns().add(historyProductCol);

        // Type column with colored badge
        TableColumn<StockMovement, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(170);
        typeCol.setMinWidth(150);
        typeCol.setMaxWidth(190);
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType().name()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatMovementType(item));
                badge.getStyleClass().add(item.equalsIgnoreCase("IN") ? "type-in"
                        : item.equalsIgnoreCase("OUT") ? "type-out" : "type-adjust");
                setGraphic(badge); setText(null);
            }
        });

        table.getColumns().add(typeCol);
        TableColumn<StockMovement, String> qtyHistoryCol = strCol("Quantity", m -> String.valueOf(m.getQuantity()));
        qtyHistoryCol.setPrefWidth(125);
        qtyHistoryCol.setMinWidth(110);
        qtyHistoryCol.setMaxWidth(140);
        table.getColumns().add(qtyHistoryCol);

        TableColumn<StockMovement, String> dateCol = strCol("Date", m -> m.getDate().toString());
        dateCol.setPrefWidth(210);
        dateCol.setMinWidth(190);
        dateCol.setMaxWidth(230);
        dateCol.setCellFactory(col -> wrapTextCell());
        table.getColumns().add(dateCol);

        BorderPane historyTableShell = new BorderPane();
        historyTableShell.setCenter(table);
        historyTableShell.setBottom(historyPagination);
        BorderPane.setMargin(historyPagination, new Insets(10, 0, 0, 0));

        VBox tableWrap = new VBox(0, historyTabs, historyTableShell);
        tableWrap.getStyleClass().add("table-wrapper");

        VBox content = new VBox(0, topbar, overview, tableWrap);
        content.getStyleClass().add("main-content");

        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("manager-page-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scroll);
    }

    private void showProfile() {
        selectNav("Profile");
        User user = controller.currentUser();

        StackPane avatar = new StackPane(new Label(getInitials(user.getName())));
        avatar.getStyleClass().add("role-profile-avatar");

        Label name = new Label(value(user.getName()).toUpperCase());
        name.getStyleClass().add("role-profile-hero-name");
        Label email = new Label(value(user.getEmail()));
        email.getStyleClass().add("role-profile-hero-email");
        Label badge = new Label("STOCK MANAGER");
        badge.getStyleClass().add("role-profile-badge");

        VBox identity = new VBox(8, name, email, badge);
        HBox hero = new HBox(34, avatar, identity);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(58, 54, 48, 54));

        VBox rows = new VBox(16,
                profileMenuRow("Contact & Support", "Modifier le nom et le telephone", "C",
                        () -> setProfileContent("CONTACT", createContactProfilePage())),
                profileMenuRow("Adresse", profileSubtitle(user.getDeliveryAddress(), "Aucune adresse definie"), "A",
                        () -> setProfileContent("ADRESSE", createAddressProfilePage())),
                profileMenuRow("Securite du compte", "Compte magasinier actif et protege", "S",
                        () -> setProfileContent("SECURITE", createSecurityProfilePage()))
        );
        rows.setPadding(new Insets(0, 54, 54, 54));

        VBox card = new VBox(0, hero, rows);
        card.getStyleClass().add("role-profile-main-card");
        card.setMaxWidth(980);

        Button logout = profileLogoutButton(controller::logout);
        VBox body = new VBox(20, card, logout);
        body.setAlignment(Pos.TOP_CENTER);
        body.setPadding(new Insets(12, 0, 40, 0));
        setProfileContent("VOTRE PROFIL", body);
    }

    private Button profileLogoutButton(Runnable action) {
        Button logout = new Button("Logout");
        logout.getStyleClass().add("role-profile-logout");
        logout.setOnAction(e -> action.run());
        return logout;
    }

    private HBox profileMenuRow(String title, String subtitle, String iconText, Runnable action) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("role-profile-row-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("role-profile-row-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("role-profile-row-subtitle");
        VBox texts = new VBox(4, titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("->");
        arrow.getStyleClass().add("role-profile-row-arrow");

        HBox row = new HBox(24, icon, texts, spacer, arrow);
        row.getStyleClass().add("role-profile-menu-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setOnMouseClicked(e -> action.run());
        return row;
    }

    private Node createContactProfilePage() {
        User user = controller.currentUser();
        TextField name = profileInput(value(user.getName()), "Nom complet");
        TextField email = profileInput(value(user.getEmail()), "Email");
        email.setEditable(false);
        TextField phone = profileInput(value(user.getPhone()), "+212 600 000 000");

        Button save = new Button("Mettre a jour le profil");
        save.getStyleClass().add("role-profile-wide-save");
        save.setOnAction(e -> saveProfile(name.getText(), phone.getText(), user.getDeliveryAddress(), "Profil mis a jour."));

        return profileFormPage("Coordonnees",
                profileBackButton(this::showProfile),
                profileField("NOM COMPLET", name),
                profileField("ADRESSE EMAIL", email),
                profileField("TELEPHONE", phone),
                save);
    }

    private Node createAddressProfilePage() {
        User user = controller.currentUser();
        TextArea address = new TextArea(value(user.getDeliveryAddress()));
        address.setPromptText("Adresse de depot, bureau ou contact");
        address.setPrefRowCount(5);
        address.setWrapText(true);
        address.getStyleClass().add("role-profile-area");

        Button save = new Button("Enregistrer l'adresse");
        save.getStyleClass().add("role-profile-wide-save");
        save.setOnAction(e -> saveProfile(user.getName(), user.getPhone(), address.getText(), "Adresse mise a jour."));

        return profileFormPage("Adresse",
                profileBackButton(this::showProfile),
                profileField("ADRESSE", address),
                save);
    }

    private Node createSecurityProfilePage() {
        PasswordField currentPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();

        Label success = profileFeedback("role-profile-success");
        Label errorCurrent = profileError();
        Label errorNew = profileError();
        Label errorConfirm = profileError();

        VBox currentBox = profileField("MOT DE PASSE ACTUEL", currentPass);
        VBox newBox = profileField("NOUVEAU MOT DE PASSE", newPass);
        VBox confirmBox = profileField("CONFIRMER LE MOT DE PASSE", confirmPass);

        Button save = new Button("Changer le mot de passe");
        save.getStyleClass().add("role-profile-password-save");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            resetSecurityFeedback(success, errorCurrent, errorNew, errorConfirm, currentBox, newBox, confirmBox);

            String oldPassword = currentPass.getText();
            String newPassword = newPass.getText();
            String confirmation = confirmPass.getText();

            if (oldPassword == null || oldPassword.isBlank()) {
                showFieldError(errorCurrent, currentBox, "Veuillez saisir votre mot de passe actuel.");
                return;
            }
            if (newPassword == null || newPassword.isBlank()) {
                showFieldError(errorNew, newBox, "Veuillez saisir un nouveau mot de passe.");
                return;
            }
            if (!newPassword.equals(confirmation)) {
                showFieldError(errorConfirm, confirmBox, "Les mots de passe ne correspondent pas.");
                return;
            }

            int result = controller.changerMotDePasse(oldPassword, newPassword);
            if (result == 1) {
                success.setText("Mot de passe mis a jour avec succes !");
                success.setVisible(true);
                success.setManaged(true);
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> showProfile());
                delay.play();
            } else if (result == -1) {
                showFieldError(errorCurrent, currentBox, "Le mot de passe actuel est incorrect.");
            } else {
                showFieldError(errorCurrent, currentBox, "Une erreur est survenue lors de la mise a jour.");
            }
        });

        return profileFormPage("Securite du compte",
                profileBackButton(this::showProfile),
                success,
                currentBox,
                errorCurrent,
                newBox,
                errorNew,
                confirmBox,
                errorConfirm,
                save);
    }

    private VBox profileFormPage(String title, Node... children) {
        Label pageTitle = new Label(title);
        pageTitle.getStyleClass().add("role-profile-form-title");

        VBox form = new VBox(22);
        if (children.length > 0) {
            form.getChildren().add(children[0]);
        }
        form.getChildren().add(pageTitle);
        for (int i = 1; i < children.length; i++) {
            form.getChildren().add(children[i]);
        }
        form.setMaxWidth(660);

        VBox page = new VBox(30, form);
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(54, 0, 70, 0));
        page.setStyle("-fx-background-color: white;");
        return page;
    }

    private Button profileBackButton(Runnable action) {
        Button back = new Button("<-");
        back.getStyleClass().add("role-profile-back");
        back.setOnAction(e -> action.run());
        return back;
    }

    private TextField profileInput(String value, String prompt) {
        TextField field = field(prompt);
        field.setText(value(value));
        return field;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        return p.length == 1
                ? p[0].substring(0, Math.min(2, p[0].length())).toUpperCase()
                : ("" + p[0].charAt(0) + p[p.length - 1].charAt(0)).toUpperCase();
    }

    private HBox managerTopbar(String title, String subtitle, Node... actions) {
        User user = controller.currentUser();
        VBox titleBlock = new VBox(3,
                Ui.title(title),
                Ui.subtitle("Workspace " + user.getRole().name().replace('_', ' ').toLowerCase())
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(14);
        right.setAlignment(Pos.CENTER_RIGHT);
        if (actions != null) {
            right.getChildren().addAll(actions);
        }
        right.getChildren().addAll(managerHeaderAvatar(user), managerHeaderIdentity(user));

        HBox topbar = new HBox(16, titleBlock, spacer, right);
        topbar.getStyleClass().add("app-header");
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(topbar, new Insets(32, 28, 18, 28));
        return topbar;
    }

    private StackPane managerHeaderAvatar(User user) {
        StackPane avatar = Ui.avatar(user.getName());
        avatar.setOnMouseClicked(e -> showProfile());
        return avatar;
    }

    private VBox managerHeaderIdentity(User user) {
        Label name = new Label(value(user.getName()));
        name.getStyleClass().add("header-name");
        Label role = new Label(user.getRole().name().replace('_', ' '));
        role.getStyleClass().add("header-role");

        VBox identity = new VBox(2, name, role);
        identity.setAlignment(Pos.CENTER_LEFT);
        identity.setOnMouseClicked(e -> showProfile());
        return identity;
    }

    private void saveProfile(String name, String phone, String address, String message) {
        if (name == null || name.trim().isEmpty()) {
            errorDialog("Name is required.");
            return;
        }
        User user = controller.currentUser();
        user.setName(name.trim());
        user.setPhone(phone == null ? null : phone.trim());
        user.setDeliveryAddress(address == null ? null : address.trim());
        try {
            controller.updateProfile(user);
            successDialog("Profile", message);
            showProfile();
        } catch (RuntimeException ex) {
            errorDialog(ex.getMessage());
        }
    }

    private VBox profileField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("role-profile-label");
        if (field instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.getStyleClass().add("role-profile-input");
        }
        return new VBox(8, label, field);
    }

    private Label profileFeedback(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    private Label profileError() {
        return profileFeedback("role-profile-error");
    }

    private void resetSecurityFeedback(Label success, Label errorCurrent, Label errorNew, Label errorConfirm,
                                       VBox currentBox, VBox newBox, VBox confirmBox) {
        success.setVisible(false);
        success.setManaged(false);
        errorCurrent.setVisible(false);
        errorCurrent.setManaged(false);
        errorNew.setVisible(false);
        errorNew.setManaged(false);
        errorConfirm.setVisible(false);
        errorConfirm.setManaged(false);
        currentBox.getStyleClass().remove("role-profile-field-error");
        newBox.getStyleClass().remove("role-profile-field-error");
        confirmBox.getStyleClass().remove("role-profile-field-error");
    }

    private void showFieldError(Label label, VBox fieldBox, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
        if (!fieldBox.getStyleClass().contains("role-profile-field-error")) {
            fieldBox.getStyleClass().add("role-profile-field-error");
        }
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private String profileSubtitle(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void setProfileContent(String title, Node content) {
        VBox page = new VBox(0, managerTopbar(title, ""), content);
        page.getStyleClass().add("main-content");
        VBox.setVgrow(content, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.getStyleClass().add("manager-page-scroll");
        root.setCenter(scrollPane);
    }
    private void refreshHistoryPage(TableView<StockMovement> table, Pagination pagination, List<StockMovement> movements) {
        int pageCount = Math.max(1, (int) Math.ceil(movements.size() / (double) HISTORY_PAGE_SIZE));
        if (pagination.getPageCount() != pageCount) {
            pagination.setPageCount(pageCount);
        }

        int pageIndex = Math.min(pagination.getCurrentPageIndex(), pageCount - 1);
        if (pageIndex != pagination.getCurrentPageIndex()) {
            pagination.setCurrentPageIndex(pageIndex);
        }

        int from = pageIndex * HISTORY_PAGE_SIZE;
        int to = Math.min(from + HISTORY_PAGE_SIZE, movements.size());
        table.setItems(FXCollections.observableArrayList(movements.subList(from, to)));
    }

    private List<StockMovement> filteredHistory(List<StockMovement> history, String filter) {
        return history.stream()
                .filter(m -> switch (filter) {
                    case "in" -> m.getType() == StockMovementType.IN;
                    case "out" -> m.getType() == StockMovementType.OUT;
                    case "adjustment" -> m.getType() == StockMovementType.ADJUSTMENT;
                    default -> true;
                })
                .toList();
    }

    private HBox buildHistoryTabBar(String activeFilter, Consumer<String> onFilter) {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("history-filter-bar");
        String[][] tabs = {
                {"all", "All movements"},
                {"in", "Stock in"},
                {"out", "Stock out"},
                {"adjustment", "Adjustments"}
        };
        for (String[] item : tabs) {
            Label tab = new Label(item[1]);
            tab.getStyleClass().add("history-filter-chip");
            if (item[0].equals(activeFilter)) {
                tab.getStyleClass().add("history-filter-active");
            }
            tab.setOnMouseClicked(e -> {
                bar.getChildren().forEach(node -> node.getStyleClass().remove("history-filter-active"));
                tab.getStyleClass().add("history-filter-active");
                onFilter.accept(item[0]);
            });
            bar.getChildren().add(tab);
        }
        return bar;
    }

    private String formatMovementType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "IN" -> "Stock in";
            case "OUT" -> "Stock out";
            case "ADJUSTMENT" -> "Adjusted";
            default -> type;
        };
    }

    // ═══════════════════════════════════════════════
    //  DIALOGS
    // ═══════════════════════════════════════════════

    private void productDialog(Product product) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "New product" : "Edit product");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("modern-dialog");
        dialog.getDialogPane().setPrefWidth(440);

        // ── Fields ──
        TextField name        = field("ex: Wireless Keyboard");
        TextField description = field("ex: Imported from...");
        TextField imageUrl    = field("https://... or /images/...");
        TextField price       = field("0.00");
        TextField quantity    = field("0");
        ComboBox<Category> category = new ComboBox<>(FXCollections.observableArrayList(controller.categories()));
        category.setPromptText("Select a category");
        category.getStyleClass().add("modern-combo");
        category.setMaxWidth(Double.MAX_VALUE);
        name.setFocusTraversable(false);
        description.setFocusTraversable(false);
        imageUrl.setFocusTraversable(false);
        price.setFocusTraversable(false);
        quantity.setFocusTraversable(false);
        category.setFocusTraversable(false);

        if (product != null) {
            name.setText(product.getName());
            description.setText(product.getDescription());
            imageUrl.setText(product.getImageUrl());
            price.setText(String.valueOf(product.getPrice()));
            quantity.setText(String.valueOf(product.getQuantity()));
            category.getItems().stream()
                    .filter(c -> c.getId() == product.getCategoryId())
                    .findFirst().ifPresent(category::setValue);
        }

        // ── Header ──
        Label title = new Label(product == null ? "New product" : "Edit product");
        title.getStyleClass().add("dialog-title");
        title.setWrapText(true);
        Label sub = new Label(product == null ? "Fill in the details below" : "Update the product information");
        sub.getStyleClass().add("dialog-subtitle");
        VBox header = new VBox(4, title, sub);
        header.getStyleClass().add("dialog-header");

        // ── Separator ──
        Region sep = new Region();
        sep.getStyleClass().add("dialog-sep");
        sep.setPrefHeight(1); sep.setMaxWidth(Double.MAX_VALUE);

        // ── Form rows ──
        VBox form = new VBox(14,
                header, sep,
                fieldRow("Product name", name),
                fieldRow("Description", description),
                fieldRow("Image URL", imageUrl),
                twoColRow(
                        fieldRow("Price ($)", price),
                        fieldRow("Quantity", quantity)
                ),
                fieldRow("Category", category)
        );
        form.setPadding(new Insets(24, 24, 8, 24));


        // ── Footer ──
        Button save = new Button(product == null ? "Create product" : "Save changes");
        save.getStyleClass().add("btn-primary");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setPrefHeight(42);

        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-secondary");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setPrefHeight(42);
        cancel.setOnAction(e -> dialog.close());

        HBox footer = new HBox(10, cancel, save);
        footer.getStyleClass().add("dialog-footer");
        HBox.setHgrow(save, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.ALWAYS);

        save.setOnAction(e -> {
            Category sel = category.getValue();
            Product next = new Product(product == null ? 0 : product.getId(),
                    name.getText(), description.getText(), imageUrl.getText(),
                    new BigDecimal(price.getText()), Integer.parseInt(quantity.getText()),
                    sel == null ? 0 : sel.getId(), sel == null ? "" : sel.getName());
            runAction(() -> controller.saveProduct(next));
            dialog.close();
        });

        VBox root = new VBox(0, form, footer);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }

    // helpers pour le dialog
    private VBox fieldRow(String label, javafx.scene.Node input) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        return new VBox(5, lbl, input);
    }

    private HBox twoColRow(VBox left, VBox right) {
        HBox row = new HBox(12, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        return row;
    }
    private void stockDialog(Product product, boolean adjust, Runnable onSaved) {
        if (product == null) { infoDialog("Select a product", "Please select a product first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("confirm-dialog");
        dialog.getDialogPane().setPrefWidth(420);

        // ── Hero ──
        Label icon = new Label(adjust ? "⚖" : "📥");
        icon.setStyle("-fx-font-size: 30px; -fx-background-color: #dbeafe; -fx-background-radius: 999; -fx-min-width: 68; -fx-min-height: 68; -fx-max-width: 68; -fx-max-height: 68; -fx-alignment: CENTER;");

        Label lAction = new Label(adjust ? "Adjust quantity" : "Add stock");
        lAction.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label lName = new Label(product.getName());
        lName.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        lName.setWrapText(true);
        lName.setMaxWidth(360);



        VBox heroText = new VBox(6, lAction, lName);
        heroText.setAlignment(Pos.CENTER);

        VBox hero = new VBox(14, icon, heroText);
        hero.setAlignment(Pos.CENTER);
        hero.setStyle("-fx-background-color: white; -fx-padding: 32 28 20 28;");

        // ── Field ──
        Label fieldLabel = new Label(adjust ? "New quantity" : "Received quantity");
        fieldLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 900; -fx-text-fill: #64748b;");

        TextField qty = new TextField();
        qty.setPromptText(adjust ? "Enter new quantity..." : "Enter received quantity...");
        qty.getStyleClass().add("modern-field");
        qty.setMaxWidth(Double.MAX_VALUE);
        qty.setFocusTraversable(false);

        VBox fieldBox = new VBox(6, fieldLabel, qty);
        fieldBox.setStyle("-fx-background-color: white; -fx-padding: 0 28 20 28;");

        // ── Separator ──
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #e8eef5; -fx-pref-height: 1;");
        sep.setMaxWidth(Double.MAX_VALUE);

        // ── Buttons ──
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-border-width: 1.5; -fx-pref-height: 42;");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(e -> dialog.close());

        String btnColor = "linear-gradient(to right, #2563eb, #1d4ed8)";
        Button save = new Button(adjust ? "Adjust" : "Validate delivery");
        save.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 12; -fx-pref-height: 42;");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            int value = Integer.parseInt(qty.getText());
            runAction(() -> { if (adjust) controller.adjustStock(product, value);
            else controller.addStock(product, value); });
            onSaved.run();
            dialog.close();
        });

        HBox.setHgrow(cancel, Priority.ALWAYS);
        HBox.setHgrow(save, Priority.ALWAYS);
        HBox btnRow = new HBox(12, cancel, save);
        btnRow.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14 24 18 24; -fx-border-color: #e2e8f0 transparent transparent transparent;");

        VBox root = new VBox(0, hero, sep, fieldBox, btnRow);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }
    // ═══════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════

    private Button styledBtn(String text, String style) {
        Button btn = new Button(text); btn.getStyleClass().add(style); return btn;
    }

    private Button makeIconBtn(String text, String tooltip) {
        Button btn = new Button(text);
        btn.getStyleClass().add("icon-btn");
        btn.setTooltip(new Tooltip(tooltip));
        return btn;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        tf.getStyleClass().add("modern-field"); tf.setMaxWidth(Double.MAX_VALUE); return tf;
    }

    private <T> TableColumn<T, String> strCol(String title, Function<T, String> mapper) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d -> new SimpleStringProperty(mapper.apply(d.getValue())));
        return col;
    }

    private <T> TableCell<T, String> wrapTextCell() {
        return new TableCell<>() {
            private final Label label = new Label();
            {
                label.setWrapText(true);
                label.getStyleClass().add("wrap-cell-label");
                label.maxWidthProperty().bind(widthProperty().subtract(24));
            }

            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                label.setText(item);
                setGraphic(label);
                setText(null);
            }
        };
    }

    private boolean confirmDialog(String title, String msg) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("confirm-dialog");
        dialog.getDialogPane().setPrefWidth(420);

        Label icon = new Label("🗑");
        icon.getStyleClass().add("confirm-icon");

        Label lTitle = new Label(title);
        lTitle.getStyleClass().add("confirm-title");

        Label lMsg = new Label(msg);
        lMsg.getStyleClass().add("confirm-msg");
        lMsg.setWrapText(true);

        VBox texts = new VBox(6, lTitle, lMsg);
        texts.setAlignment(Pos.CENTER);

        VBox content = new VBox(16, icon, texts);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("confirm-content");

        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("confirm-btn-cancel");
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setPrefHeight(42);

        Button btnDelete = new Button("Yes, delete");
        btnDelete.getStyleClass().add("confirm-btn-delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setPrefHeight(42);

        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        HBox.setHgrow(btnDelete, Priority.ALWAYS);
        HBox btnRow = new HBox(12, btnCancel, btnDelete);
        btnRow.getStyleClass().add("confirm-btn-row");

        final boolean[] result = {false};
        btnCancel.setOnAction(e -> dialog.close());
        btnDelete.setOnAction(e -> { result[0] = true; dialog.close(); });

        VBox root = new VBox(0, content, btnRow);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
        return result[0];
    }



    private void runAction(Runnable action) {
        try {
            action.run();
            successDialog("Done", "Action completed successfully.");
        } catch (RuntimeException e) {
            errorDialog(e.getMessage());
        }
    }

    private void successDialog(String title, String msg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("confirm-dialog");
        dialog.getDialogPane().setPrefWidth(380);

        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: #16a34a; -fx-background-color: #dcfce7; -fx-background-radius: 999; -fx-min-width: 68; -fx-min-height: 68; -fx-max-width: 68; -fx-max-height: 68; -fx-alignment: CENTER;");

        Label lTitle = new Label(title);
        lTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label lMsg = new Label(msg);
        lMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox content = new VBox(14, icon, new VBox(6, lTitle, lMsg));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-padding: 34 28 20 28;");

        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: linear-gradient(to right, #16a34a, #15803d); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 12; -fx-pref-height: 42;");
        ok.setMaxWidth(Double.MAX_VALUE);
        ok.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(ok);
        btnRow.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14 24 18 24; -fx-border-color: #e2e8f0 transparent transparent transparent;");
        HBox.setHgrow(ok, Priority.ALWAYS);

        dialog.getDialogPane().setContent(new VBox(0, content, btnRow));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }

    private void errorDialog(String msg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("confirm-dialog");
        dialog.getDialogPane().setPrefWidth(380);

        Label icon = new Label("✕");
        icon.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #dc2626; -fx-background-color: #fee2e2; -fx-background-radius: 999; -fx-min-width: 68; -fx-min-height: 68; -fx-max-width: 68; -fx-max-height: 68; -fx-alignment: CENTER;");

        Label lTitle = new Label("Error");
        lTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label lMsg = new Label(msg);
        lMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        lMsg.setWrapText(true);

        VBox content = new VBox(14, icon, new VBox(6, lTitle, lMsg));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-padding: 34 28 20 28;");

        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 12; -fx-pref-height: 42;");
        ok.setMaxWidth(Double.MAX_VALUE);
        ok.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(ok);
        btnRow.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14 24 18 24; -fx-border-color: #e2e8f0 transparent transparent transparent;");
        HBox.setHgrow(ok, Priority.ALWAYS);

        dialog.getDialogPane().setContent(new VBox(0, content, btnRow));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }

    private void infoDialog(String title, String msg) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("confirm-dialog");
        dialog.getDialogPane().setPrefWidth(380);

        Label icon = new Label("ℹ");
        icon.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #2563eb; -fx-background-color: #dbeafe; -fx-background-radius: 999; -fx-min-width: 68; -fx-min-height: 68; -fx-max-width: 68; -fx-max-height: 68; -fx-alignment: CENTER;");

        Label lTitle = new Label(title);
        lTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label lMsg = new Label(msg);
        lMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        lMsg.setWrapText(true);

        VBox content = new VBox(14, icon, new VBox(6, lTitle, lMsg));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-padding: 34 28 20 28;");

        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #1d4ed8); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900; -fx-background-radius: 12; -fx-pref-height: 42;");
        ok.setMaxWidth(Double.MAX_VALUE);
        ok.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(ok);
        btnRow.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14 24 18 24; -fx-border-color: #e2e8f0 transparent transparent transparent;");
        HBox.setHgrow(ok, Priority.ALWAYS);

        dialog.getDialogPane().setContent(new VBox(0, content, btnRow));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dialog.showAndWait();
    }
}
