package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.CustomerController;
import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class CustomerDashboardView {
    private final CustomerController controller;
    private final AppShell shell;

    public CustomerDashboardView(CustomerController controller, AppShell shell) {
        this.controller = controller;
        this.shell = shell;
    }

    public Parent render() {
        // On injecte les actions (Catalog, Cart, etc.) dans le shell
        shell.setNavItems(navItems());

        // On affiche le premier écran
        showCatalog();

        return shell.render();
    }

    private void showCatalog() {
        // --- Système de filtre de ta collègue ---
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("All Categories");
        try {
            categoryCombo.getItems().addAll(controller.products("").stream()
                    .map(Product::getCategoryName)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct().toList());
        } catch (Exception e) { System.err.println("Error loading categories"); }
        categoryCombo.setValue("All Categories");

        FlowPane grid = new FlowPane(15, 15);
        grid.setPadding(new Insets(10));

        Runnable reload = () -> {
            grid.getChildren().clear();
            String selectedCat = categoryCombo.getValue();
            controller.products("").stream()
                    .filter(p -> selectedCat.equals("All Categories") || 
                            (p.getCategoryName() != null && p.getCategoryName().equals(selectedCat)))
                    .forEach(product -> grid.getChildren().add(productCard(product)));
        };

        categoryCombo.valueProperty().addListener((o, old, val) -> reload.run());
        
        // Ton SearchHandler (barre de recherche du header)
        shell.setSearchHandler(query -> {
            grid.getChildren().clear();
            controller.products(query).forEach(p -> grid.getChildren().add(productCard(p)));
        });

        reload.run();
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);

        VBox layout = new VBox(15, Ui.toolbar(new Label("Filter by: "), categoryCombo), scroll);
        setContent("Product Catalog", layout);
    }

    private VBox productCard(Product product) {
        // --- Design "Amazon" de ta collègue ---
        StackPane visual = productVisual(product);
        visual.setStyle("-fx-background-color: #F7F7F7; -fx-background-radius: 8;");

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setMinHeight(35);

        // Formatage du prix (MAD)
        BigDecimal priceVal = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        Label priceLabel = new Label(priceVal + " MAD");
        priceLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #B12704; -fx-font-weight: bold;");

        Label stockInfo = new Label(product.getQuantity() > 0 ? "In Stock (" + product.getQuantity() + ")" : "Out of stock");
        stockInfo.setStyle("-fx-font-size: 11; -fx-text-fill: " + (product.getQuantity() > 0 ? "#007600" : "#B12704"));

        Spinner<Integer> qty = new Spinner<>(1, Math.max(1, product.getQuantity()), 1);
        qty.setMaxWidth(80);

        Button add = new Button("Add to cart");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(product.getQuantity() <= 0);
        add.setStyle("-fx-background-color: #2D8E2D; -fx-text-fill: white; -fx-background-radius: 15;");
        
        add.setOnAction(e -> run(() -> {
            controller.addToCart(product, qty.getValue());
            shell.updateCartCount(controller.cart().size());
        }));

        VBox card = new VBox(8, visual, name, priceLabel, stockInfo, new Label("Qty:"), qty, add);
        card.setPadding(new Insets(10));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-border-color: #DDD; -fx-border-radius: 5;");
        
        return card;
    }

    private StackPane productVisual(Product product) {
        StackPane container = new StackPane();
        container.setPrefHeight(120);
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                ImageView iv = new ImageView(new Image(resolveImage(product.getImageUrl()), true));
                iv.setFitHeight(100); iv.setFitWidth(150); iv.setPreserveRatio(true);
                container.getChildren().add(iv);
                return container;
            } catch (Exception ignored) {}
        }
        Label glyph = new Label(productGlyph(product.getCategoryName()));
        glyph.setStyle("-fx-font-size: 40;");
        container.getChildren().add(glyph);
        return container;
    }

    private String resolveImage(String url) {
        if (url.startsWith("http") || url.startsWith("file:")) return url;
        File f = new File(url);
        return f.exists() ? f.toURI().toString() : url;
    }

    private String productGlyph(String cat) {
        if (cat == null) return "📦";
        return switch (cat.toLowerCase()) {
            case "electronics" -> "🔌";
            case "fashion" -> "👕";
            case "home" -> "🏠";
            default -> "📦";
        };
    }

    private void showCart() {
        shell.setSearchHandler(null);
        TableView<CartItem> table = new TableView<>(FXCollections.observableArrayList(controller.cart()));
        table.getColumns().addAll(col("Product", i -> i.getProduct().getName()), 
                                  col("Qty", i -> String.valueOf(i.getQuantity())), 
                                  col("Total", i -> i.getSubtotal() + " MAD"));
        
        Button pay = Ui.primary("Checkout");
        pay.setOnAction(e -> { run(controller::placeOrder); showCart(); });
        
        setContent("Your Cart", new VBox(10, table, Ui.subtitle("Total: " + controller.cartTotal() + " MAD"), pay));
    }

    private void showOrders() {
        shell.setSearchHandler(null);
        TableView<Order> table = new TableView<>(FXCollections.observableArrayList(controller.orders()));
        table.getColumns().addAll(col("ID", o -> "#" + o.getId()), col("Date", o -> o.getDate().toString()), col("Total", o -> o.getTotalPrice() + " MAD"));
        setContent("Order History", table);
    }

    private void showProfile() {
        shell.setSearchHandler(null);
        VBox profile = Ui.card(Ui.title(controller.currentUser().getName()), new Label("Email: " + controller.currentUser().getEmail()));
        setContent("My Profile", profile);
    }

    private void setContent(String title, javafx.scene.Node content) {
        shell.setContent(title, content);
    }

    private <T> TableColumn<T, String> col(String t, java.util.function.Function<T, String> m) {
        TableColumn<T, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(m.apply(d.getValue())));
        return c;
    }

    private void run(Runnable a) {
        try { a.run(); } catch (Exception e) { Ui.error(e); }
    }
    
    public List<AppShell.NavItem> navItems() {
        return List.of(
            new AppShell.NavItem("/images/icons/catalog.jpeg", "Catalog", this::showCatalog),
            new AppShell.NavItem("/images/icons/cart.png", "Cart", this::showCart),
            new AppShell.NavItem("/images/icons/orders.png", "Orders", this::showOrders),
            new AppShell.NavItem("/images/icons/profil.png", "Profile", this::showProfile)
        );
    }
}