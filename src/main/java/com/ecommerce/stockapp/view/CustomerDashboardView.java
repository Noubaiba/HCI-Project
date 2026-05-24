package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.CustomerController;
import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.Product;
import javafx.application.Platform;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.ecommerce.stockapp.controller.CustomerController;
import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.ecommerce.stockapp.model.User; // Important : pour reconnaître la classe User
import javafx.geometry.Pos;               // Pour Pos.CENTER_LEFT
import javafx.scene.shape.Circle;         // Pour l'avatar circulaire
import java.time.format.DateTimeFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import com.ecommerce.stockapp.model.OrderItem;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import com.ecommerce.stockapp.model.OrderStatus;
import com.ecommerce.stockapp.util.IconFactory;
import com.ecommerce.stockapp.view.NavItem;
import javafx.scene.Node;
import javafx.scene.control.PasswordField; // Profites-en pour ajouter celui-ci aussi
import netscape.javascript.JSObject;


public class CustomerDashboardView {
    private final CustomerController controller;
    private final AppShell shell;
    private boolean guestBannerDismissed;

    public CustomerDashboardView(CustomerController controller, AppShell shell) {
        this.controller = controller;
        this.shell = shell;
    }

    public Parent render() {

        if (shell == null) {
            throw new IllegalStateException("Shell non initialisé");
        }

        if (controller == null || controller.cart() == null) {
            throw new IllegalStateException("Controller ou cart null");
        }

        shell.setNavItems(navItems());
        shell.updateCartCount(controller.cart().size());

        showCatalog();

        return shell.render();
    }

    private boolean isGuestMode() {
        return controller.isGuest();
    }

    private void promptGuestCheckout() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox card = new VBox(22);
        card.setPadding(new Insets(28));
        card.setMaxWidth(460);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-border-color: #dbe7f5; -fx-border-radius: 24;");
        card.setEffect(new DropShadow(30, Color.rgb(15, 23, 42, 0.18)));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(6);
        Label eyebrow = new Label("CHECKOUT");
        eyebrow.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: 900; -fx-letter-spacing: 1px;");
        Label title = new Label("Create an account to place your order");
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 24px; -fx-font-weight: 900;");
        titleBox.getChildren().addAll(eyebrow, title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("×");
        close.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 22px; -fx-cursor: hand; -fx-padding: 0 4;");
        close.setOnAction(e -> dialog.close());

        header.getChildren().addAll(titleBox, spacer, close);

        Label message = new Label("You can browse the catalog and build your cart as a guest. To continue to checkout, please sign in or create an account.");
        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-line-spacing: 3px;");

        HBox infoStrip = new HBox(12);
        infoStrip.setAlignment(Pos.CENTER_LEFT);
        infoStrip.setPadding(new Insets(14, 16, 14, 16));
        infoStrip.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 16; -fx-border-color: #dbeafe; -fx-border-radius: 16;");
        Label infoIcon = new Label("i");
        infoIcon.setAlignment(Pos.CENTER);
        infoIcon.setMinSize(28, 28);
        infoIcon.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 999;");
        Label infoText = new Label("Your cart stays available for this visit.");
        infoText.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 13px; -fx-font-weight: 600;");
        infoStrip.getChildren().addAll(infoIcon, infoText);

        Button signIn = new Button("Sign In");
        signIn.setStyle("-fx-background-color: white; -fx-text-fill: #1e3a5f; -fx-border-color: #1e3a5f; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12 22; -fx-font-weight: bold; -fx-cursor: hand;");
        signIn.setMinWidth(120);
        signIn.setOnAction(e -> {
            dialog.close();
            controller.showLoginScreen();
        });

        Button signUp = new Button("Sign Up");
        signUp.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 22; -fx-font-weight: bold; -fx-cursor: hand;");
        signUp.setMinWidth(120);
        signUp.setOnAction(e -> {
            dialog.close();
            controller.showRegisterScreen();
        });

        Button cancel = new Button("Maybe later");
        cancel.setStyle("-fx-background-color: #eef2f7; -fx-text-fill: #475569; -fx-background-radius: 12; -fx-padding: 12 22; -fx-font-weight: bold; -fx-cursor: hand;");
        cancel.setMinWidth(120);
        cancel.setOnAction(e -> dialog.close());

        HBox actions = new HBox(12, signIn, signUp, cancel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(header, message, infoStrip, actions);

        StackPane overlay = new StackPane(card);
        overlay.setPadding(new Insets(26));
        overlay.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showCatalog() {
        // 1. Header promo
        Label promoLabel = new Label("LIVRAISON GRATUITE DÈS 39€ D'ACHAT | RETOURS GRATUITS");
        promoLabel.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 8px; -fx-font-weight: bold;");
        promoLabel.setMaxWidth(Double.MAX_VALUE);
        promoLabel.setAlignment(Pos.CENTER);

        // 2. Categories bar
        HBox categoryBar = new HBox(30);
        categoryBar.setAlignment(Pos.CENTER_LEFT);
        categoryBar.setPadding(new Insets(0, 20, 0, 20));
        categoryBar.setStyle("-fx-background-color: white;");

        ScrollPane categoryScroll = new ScrollPane(categoryBar);
        categoryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScroll.setFitToHeight(true);
        categoryScroll.setPrefHeight(60);
        categoryScroll.setMinHeight(60);
        categoryScroll.setStyle("-fx-background-color: white; -fx-background: white; -fx-border-color: #eee; -fx-border-width: 0 0 1 0; -fx-padding: 0;");

        // 3. GRID PRODUITS
        FlowPane grid = new FlowPane(15, 15);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setPrefWrapLength(900);
        grid.setMaxWidth(Double.MAX_VALUE);

        // 🔥 SCROLL CONTAINER (IMPORTANT)
        ScrollPane mainProductScroll = new ScrollPane(grid);
        mainProductScroll.setFitToWidth(true);
        mainProductScroll.setFitToHeight(true);

        // cacher scrollbar MAIS garder scroll
        mainProductScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainProductScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // permettre scroll touchpad / mouse
        mainProductScroll.setPannable(true);

        mainProductScroll.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background: white;" +
                        "-fx-border-color: transparent;"
        );

        // FILTER LOGIC
        java.util.function.Consumer<String> filterAction = (categoryName) -> {
            grid.getChildren().clear();

            controller.products("")
                    .stream()
                    .filter(p -> categoryName.equals("TOUT VOIR") ||
                            (p.getCategoryName() != null &&
                                    p.getCategoryName().equalsIgnoreCase(categoryName)))
                    .forEach(p -> grid.getChildren().add(productCard(p)));

            categoryBar.getChildren().forEach(node -> {
                if (node instanceof Button b) {
                    b.setMinWidth(Region.USE_PREF_SIZE);
                    b.setMinHeight(60);

                    if (b.getText().equals(categoryName)) {
                        b.setStyle(
                                "-fx-background-color: transparent;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-text-fill: black;" +
                                        "-fx-border-color: black;" +
                                        "-fx-border-width: 0 0 3 0;" +
                                        "-fx-cursor: hand;"
                        );
                    } else {
                        b.setStyle(
                                "-fx-background-color: transparent;" +
                                        "-fx-font-weight: normal;" +
                                        "-fx-text-fill: #666;" +
                                        "-fx-border-color: transparent;" +
                                        "-fx-cursor: hand;"
                        );
                    }
                }
            });
        };

        // CATEGORIES
        try {
            List<String> cats = new java.util.ArrayList<>();
            cats.add("TOUT VOIR");

            cats.addAll(controller.products("")
                    .stream()
                    .map(Product::getCategoryName)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .map(String::toUpperCase)
                    .toList());

            for (String catName : cats) {
                Button catBtn = new Button(catName);
                catBtn.setMinWidth(Region.USE_PREF_SIZE);
                catBtn.setOnAction(e -> filterAction.accept(catName));
                categoryBar.getChildren().add(catBtn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // SEARCH
        shell.setSearchHandler(query -> {
            grid.getChildren().clear();
            controller.products(query)
                    .forEach(p -> grid.getChildren().add(productCard(p)));
        });

        filterAction.accept("TOUT VOIR");

        // FINAL LAYOUT
        VBox layout = new VBox(0);
        layout.getChildren().addAll(promoLabel, categoryScroll);
        if (isGuestMode() && !guestBannerDismissed) {
            layout.getChildren().add(guestCatalogBanner());
        }
        layout.getChildren().add(mainProductScroll);

        setContent("Product Catalog", layout);
    }

    private Node guestCatalogBanner() {
        HBox banner = new HBox(16);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(18, 24, 18, 24));
        banner.setStyle("-fx-background-color: linear-gradient(to right, #eff6ff, #f8fafc); -fx-border-color: #bfdbfe; -fx-border-width: 0 0 1 0;");

        VBox copy = new VBox(4);
        Label title = new Label("New here?");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Browse freely and add items to your cart. Create an account when you're ready to place the order.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        subtitle.setWrapText(true);
        copy.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button signIn = new Button("Sign In");
        signIn.setStyle("-fx-background-color: white; -fx-text-fill: #1e3a5f; -fx-border-color: #1e3a5f; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 18; -fx-font-weight: bold; -fx-cursor: hand;");
        signIn.setOnAction(e -> controller.showLoginScreen());

        Button signUp = new Button("Sign Up");
        signUp.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 18; -fx-font-weight: bold; -fx-cursor: hand;");
        signUp.setOnAction(e -> controller.showRegisterScreen());

        Button close = new Button("×");
        close.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8;");
        close.setOnAction(e -> {
            guestBannerDismissed = true;
            showCatalog();
        });

        banner.getChildren().addAll(copy, spacer, signIn, signUp, close);
        return banner;
    }

    private VBox productCard(Product product) {
        // 1. Visuel sans marges
        StackPane visual = productVisual(product);

        // 2. Conteneur pour le texte (lui a besoin de padding)
        VBox detailsBox = new VBox(8);
        detailsBox.setPadding(new Insets(10)); // On met le padding ICI, pas sur la carte entière

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-size: 13; -fx-text-fill: #333; -fx-font-family: 'Arial';");
        name.setWrapText(true);
        name.setPrefHeight(35);

        BigDecimal priceVal = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        Label priceLabel = new Label(priceVal + " €");
        priceLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #000;");

        // Bouton panier à côté du prix
        ImageView addIcon = new ImageView(new Image(getClass().getResource("/images/icons/addtocart.png").toExternalForm()));
        addIcon.setFitWidth(24); addIcon.setFitHeight(24);
        addIcon.setCursor(javafx.scene.Cursor.HAND);

//        addIcon.setOnMouseClicked(e -> {
//            if (product.getQuantity() > 0) {
//                run(() -> {
//                    controller.addToCart(product, 1);
//                    shell.updateCartCount(controller.cart().size());
//                });
//            }
//        });
        addIcon.setOnMouseClicked(e -> showProductDetails(product));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox priceLine = new HBox(priceLabel, spacer, addIcon);
        priceLine.setAlignment(Pos.CENTER_LEFT);

        detailsBox.getChildren().addAll(name, priceLine);

        // 3. Assemblage de la carte
        VBox card = new VBox(0, visual, detailsBox); // Spacing à 0 entre l'image et le texte
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #eee; -fx-border-radius: 8;");

        // Effet d'ombre au survol
        card.setOnMouseEntered(e -> card.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.1))));
        card.setOnMouseExited(e -> card.setEffect(null));

        return card;
    }


    private void showProductDetails(Product product) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        // Conteneur principal
        HBox root = new HBox(30);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        root.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.2)));
        root.setPrefSize(700, 450);
        root.setAlignment(Pos.CENTER_LEFT);

        // --- GAUCHE : IMAGE ---
        VBox imageBox = new VBox(10);
        imageBox.setAlignment(Pos.CENTER);
        ImageView mainImg = new ImageView();
        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            mainImg.setImage(new Image(resolveImage(product.getImageUrl()), true));
        }
        mainImg.setFitWidth(300);
        mainImg.setFitHeight(380);
        mainImg.setPreserveRatio(true);
        imageBox.getChildren().add(mainImg);

        // --- DROITE : INFOS ---
        VBox infoBox = new VBox(15);
        infoBox.setPadding(new Insets(10, 0, 0, 0));
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Header
        HBox header = new HBox();
        Label title = new Label(product.getName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(300);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());
        header.getChildren().addAll(title, headerSpacer, btnClose);

        // Prix
        Label price = new Label(product.getPrice() + " €");
        price.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #000;");

        // --- SÉLECTEUR DE QUANTITÉ PERSONNALISÉ (+ / -) ---
        Label lblQty = new Label("Quantité(s):");
        lblQty.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label qtyDisplay = new Label("1");
        qtyDisplay.setPrefWidth(40);
        qtyDisplay.setAlignment(Pos.CENTER);
        qtyDisplay.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #ddd; -fx-border-width: 1 0 1 0; -fx-min-height: 35;");

        Button btnMinus = new Button("-");
        btnMinus.setPrefSize(35, 35);
        btnMinus.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 0; -fx-cursor: hand;");
        btnMinus.setOnAction(e -> {
            int current = Integer.parseInt(qtyDisplay.getText());
            if (current > 1) qtyDisplay.setText(String.valueOf(current - 1));
        });

        Button btnPlus = new Button("+");
        btnPlus.setPrefSize(35, 35);
        btnPlus.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 0; -fx-cursor: hand;");
        btnPlus.setOnAction(e -> {
            int current = Integer.parseInt(qtyDisplay.getText());
            if (current < product.getQuantity()) qtyDisplay.setText(String.valueOf(current + 1));
        });

        HBox qtySelector = new HBox(0, btnMinus, qtyDisplay, btnPlus);
        qtySelector.setAlignment(Pos.CENTER_LEFT);

        // Bouton Ajouter
        Button btnAdd = new Button("AJOUTER AU PANIER");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setPrefHeight(45);
        btnAdd.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            int finalQty = Integer.parseInt(qtyDisplay.getText());
            run(() -> {
                controller.addToCart(product, finalQty);
                shell.updateCartCount(controller.cart().size());
                dialog.close();
            });
        });

        Label stock = new Label("Stock disponible: " + product.getQuantity());
        stock.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // CRITIQUE : Ici on ajoute bien qtySelector et PAS qtySpinner
        infoBox.getChildren().addAll(header, price, new Separator(), lblQty, qtySelector, stock, btnAdd);

        root.getChildren().addAll(imageBox, infoBox);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private StackPane productVisual(Product product) {
        StackPane container = new StackPane();
        container.setPrefHeight(220);
        container.setStyle("-fx-background-color: #f7f7f7;");

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                ImageView iv = new ImageView(new Image(resolveImage(product.getImageUrl()), true));
                iv.setPreserveRatio(true);
                iv.fitWidthProperty().bind(container.widthProperty()); // L'image s'adapte à la largeur
                iv.setFitHeight(220);
                container.getChildren().add(iv);
            } catch (Exception ignored) {}
        } else {
            container.getChildren().add(new Label("📦"));
        }
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
        List<CartItem> items = controller.cart();
        BigDecimal total = controller.cartTotal();

        // --- PANIER VIDE ---
        if (items.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));

            StackPane iconContainer = new StackPane();
            Circle circle = new Circle(50, Color.web("#e8f0fe"));
            Label cartIcon = new Label("🛒");
            cartIcon.setStyle("-fx-font-size: 40px;");
            iconContainer.getChildren().addAll(circle, cartIcon);

            Label title = new Label("Votre panier est vide!");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");

            Label subtitle = new Label("Parcourez nos catégories et découvrez nos meilleures offres!");
            subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

            Button btnShop = new Button("Commencez vos achats");
            btnShop.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 12 30; -fx-cursor: hand;");
            btnShop.setOnAction(e -> showCatalog());

            emptyBox.getChildren().addAll(iconContainer, title, subtitle, btnShop);
            setContent("Panier", emptyBox);
            return;
        }

        // --- PANIER AVEC ARTICLES ---
        HBox mainLayout = new HBox(25);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // === COLONNE GAUCHE : LISTE DES ARTICLES (SCROLLABLE SANS BARRE) ===
        VBox leftColumn = new VBox(15);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        leftColumn.setMaxWidth(750);

        Label lblTitle = new Label("Panier (" + items.size() + ")");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");

        // Container des articles
        VBox itemsContainer = new VBox(0);
        itemsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        for (CartItem item : items) {
            Product p = item.getProduct();

            HBox itemRow = new HBox(15);
            itemRow.setPadding(new Insets(15));
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

            // Image du produit
            ImageView imgView = new ImageView();
            if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                try {
                    imgView.setImage(new Image(resolveImage(p.getImageUrl()), true));
                } catch (Exception ignored) {}
            }
            imgView.setFitWidth(80);
            imgView.setFitHeight(80);
            imgView.setPreserveRatio(true);

            // Infos produit (milieu)
            VBox infoBox = new VBox(5);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label name = new Label(p.getName());
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            name.setWrapText(true);
            name.setMaxWidth(300);

            Label stock = new Label("Quelques articles restants");
            stock.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 11px;");

            // Bouton Supprimer
            Button btnDelete = new Button("🗑 Supprimer");
            btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e3a5f; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 12px;");
            btnDelete.setOnAction(e -> {
                run(() -> {
                    controller.removeFromCart(item);
                    shell.updateCartCount(controller.cart().size());
                    showCart();
                });
            });

            infoBox.getChildren().addAll(name, stock, btnDelete);

            // === PRIX + QUANTITÉ ALIGNÉS ===
            VBox priceQtyBox = new VBox(8);
            priceQtyBox.setAlignment(Pos.TOP_RIGHT);
            priceQtyBox.setMinWidth(120);

            // Prix
            Label price = new Label(String.format("%,.2f €", p.getPrice()));
            price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            price.setAlignment(Pos.CENTER_RIGHT);

            // === SÉLECTEUR QUANTITÉ COLLÉ (-|2|+) ===
            HBox qtyBox = new HBox(0);
            qtyBox.setAlignment(Pos.CENTER_RIGHT);

            Button btnMinus = new Button("−");
            btnMinus.setPrefSize(36, 36);
            btnMinus.setMinSize(36, 36);
            btnMinus.setMaxSize(36, 36);
            btnMinus.setStyle(
                    "-fx-background-color: #cbd5e1;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 6 0 0 6;" +
                            "-fx-border-radius: 6 0 0 6;" +
                            "-fx-border-color: #cbd5e1;" +
                            "-fx-border-width: 1;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );

            Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
            qtyLabel.setPrefSize(40, 36);
            qtyLabel.setMinSize(40, 36);
            qtyLabel.setMaxSize(40, 36);
            qtyLabel.setAlignment(Pos.CENTER);
            qtyLabel.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #cbd5e1;" +
                            "-fx-border-width: 1 0;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-text-fill: #1e293b;"
            );

            Button btnPlus = new Button("+");
            btnPlus.setPrefSize(36, 36);
            btnPlus.setMinSize(36, 36);
            btnPlus.setMaxSize(36, 36);
            btnPlus.setStyle(
                    "-fx-background-color: #1e3a5f;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-background-radius: 0 6 6 0;" +
                            "-fx-border-radius: 0 6 6 0;" +
                            "-fx-border-color: #1e3a5f;" +
                            "-fx-border-width: 1;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;"
            );

            btnMinus.setOnAction(e -> {
                if (item.getQuantity() > 1) {
                    run(() -> {
                        controller.updateCartQuantity(item, item.getQuantity() - 1);
                        showCart();
                    });
                }
            });

            btnPlus.setOnAction(e -> {
                if (item.getQuantity() < p.getQuantity()) {
                    run(() -> {
                        controller.updateCartQuantity(item, item.getQuantity() + 1);
                        showCart();
                    });
                }
            });

            qtyBox.getChildren().addAll(btnMinus, qtyLabel, btnPlus);
            priceQtyBox.getChildren().addAll(price, qtyBox);

            itemRow.getChildren().addAll(imgView, infoBox, priceQtyBox);
            itemsContainer.getChildren().add(itemRow);
        }

        // ScrollPane SANS barres de scroll visibles
        ScrollPane itemsScroll = new ScrollPane(itemsContainer);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-padding: 0;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 0;"
        );
        itemsScroll.setPrefHeight(500);
        VBox.setVgrow(itemsScroll, Priority.ALWAYS);

        // CACHER LES BARRES DE SCROLL
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        leftColumn.getChildren().addAll(lblTitle, itemsScroll);

        // === COLONNE DROITE : RÉSUMÉ (FIXE, TAILLE AUTO) ===
        VBox rightColumn = new VBox(15);
        rightColumn.setPrefWidth(280);
        rightColumn.setMinWidth(280);
        rightColumn.setMaxWidth(280);
        rightColumn.setPadding(new Insets(25));
        rightColumn.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 8;"
        );
        rightColumn.setAlignment(Pos.TOP_CENTER);

        Label lblResume = new Label("RÉSUMÉ DU PANIER");
        lblResume.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0;");

        HBox subtotalRow = new HBox();
        subtotalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblSub = new Label("Sous-total");
        lblSub.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblSubVal = new Label(String.format("%,.2f €", total));
        lblSubVal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        subtotalRow.getChildren().addAll(lblSub, spacer, lblSubVal);

        HBox deliveryRow = new HBox(5);
        deliveryRow.setAlignment(Pos.CENTER_LEFT);
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 14px;");
        Label lblDelivery = new Label("Livraison gratuite disponible");
        lblDelivery.setStyle("-fx-text-fill: #1e3a5f; -fx-font-size: 12px;");
        deliveryRow.getChildren().addAll(check, lblDelivery);

        Button btnOrder = new Button(String.format("Commander (%,.2f €)", total));
        btnOrder.setMaxWidth(Double.MAX_VALUE);
        btnOrder.setPrefHeight(48);
        btnOrder.setStyle(
                "-fx-background-color: #1e3a5f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14px;"
        );
        btnOrder.setOnAction(e -> {
            if (isGuestMode()) {
                promptGuestCheckout();
                return;
            }
            showPayment(); // ← Redirige vers la page paiement
        });

        rightColumn.getChildren().addAll(lblResume, sep, subtotalRow, deliveryRow, btnOrder);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        setContent("Panier", mainLayout);
    }
    private void showPayment() {
        if (isGuestMode()) {
            promptGuestCheckout();
            return;
        }
        shell.setSearchHandler(null);
        BigDecimal total = controller.cartTotal();
        List<CartItem> items = controller.cart();

        // ========== LAYOUT PRINCIPAL ==========
        HBox mainLayout = new HBox(24);
        mainLayout.setPadding(new Insets(24));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");
        mainLayout.setAlignment(Pos.TOP_LEFT);

        // ========== COLONNE GAUCHE ==========
        VBox leftCol = new VBox(16);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Bouton retour
        Button backBtn = createModernBackButton(this::showCart);
        HBox backWrapper = new HBox(backBtn);
        backWrapper.setMaxWidth(500);

        // ===== BLOC 1 : MÉTHODE DE PAIEMENT =====
        VBox paymentBlock = new VBox(16);
        paymentBlock.setPadding(new Insets(24));
        paymentBlock.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        HBox stepHeader1 = new HBox(10);
        stepHeader1.setAlignment(Pos.CENTER_LEFT);
        Label step1Badge = new Label("1");
        step1Badge.setPrefSize(26, 26);
        step1Badge.setAlignment(Pos.CENTER);
        step1Badge.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50;");
        Label step1Title = new Label("Méthode de paiement");
        step1Title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        stepHeader1.getChildren().addAll(step1Badge, step1Title);

        // --- MÉTHODES DE PAIEMENT ---
        ToggleGroup group = new ToggleGroup();

        // Carte bancaire
        VBox cardMethodBox = buildMethodCard(group, true,
                buildCardIcon(),
                "Carte bancaire", "Visa · Mastercard · Amex"
        );

        // À la livraison
        VBox cashMethodBox = buildMethodCard(group, false,
                buildCashIcon(),
                "À la livraison", "Paiement en espèces"
        );

        // PayPal
        VBox paypalMethodBox = buildMethodCard(group, false,
                buildPaypalIcon(),
                "PayPal", "Paiement en ligne sécurisé"
        );

        HBox methodGrid = new HBox(12, cardMethodBox, cashMethodBox, paypalMethodBox);
        HBox.setHgrow(cardMethodBox, Priority.ALWAYS);
        HBox.setHgrow(cashMethodBox, Priority.ALWAYS);
        HBox.setHgrow(paypalMethodBox, Priority.ALWAYS);
        cardMethodBox.setMaxWidth(Double.MAX_VALUE);
        cashMethodBox.setMaxWidth(Double.MAX_VALUE);
        paypalMethodBox.setMaxWidth(Double.MAX_VALUE);

        Label cardInfo = new Label("Les informations de carte seront saisies dans la page de paiement securisee.");
        cardInfo.setWrapText(true);
        cardInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 14;");
        cardInfo.setVisible(true);
        cardInfo.setManaged(true);

        // Afficher/cacher selon méthode
        group.selectedToggleProperty().addListener((obs, old, newVal) -> {
            Object data = ((Toggle) newVal).getUserData();
            cardInfo.setVisible("card".equals(data));
            cardInfo.setManaged("card".equals(data));

            // Mettre à jour le style visuel des cartes
            updateMethodCardStyle(cardMethodBox, "card".equals(data));
            updateMethodCardStyle(cashMethodBox, "cash".equals(data));
            updateMethodCardStyle(paypalMethodBox, "paypal".equals(data));
        });

        // Lier les userData aux toggles
        group.getToggles().get(0).setUserData("card");
        group.getToggles().get(1).setUserData("cash");
        group.getToggles().get(2).setUserData("paypal");

        paymentBlock.getChildren().addAll(stepHeader1, methodGrid, cardInfo);

        // ===== BLOC 2 : ADRESSE =====
        VBox addressBlock = new VBox(16);
        addressBlock.setPadding(new Insets(24));
        addressBlock.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        HBox stepHeader2 = new HBox(10);
        stepHeader2.setAlignment(Pos.CENTER_LEFT);
        Label step2Badge = new Label("2");
        step2Badge.setPrefSize(26, 26);
        step2Badge.setAlignment(Pos.CENTER);
        step2Badge.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50;");
        Label step2Title = new Label("Adresse de livraison");
        step2Title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        stepHeader2.getChildren().addAll(step2Badge, step2Title);

        TextField street = new TextField();
        street.setPromptText("123 Rue Mohammed V");
        TextField city = new TextField();
        city.setPromptText("Casablanca");
        TextField zip = new TextField();
        zip.setPromptText("20000");

        // LISTE DÉROULANTE POUR LE PAYS (Design synchronisé avec showProfile)
        ComboBox<String> countryCombo = new ComboBox<>();
        countryCombo.getItems().addAll("Maroc", "France", "Belgique", "Canada", "Espagne", "États-Unis");
        countryCombo.setValue("Maroc"); // Valeur par défaut initiale
        countryCombo.setMaxWidth(Double.MAX_VALUE);
        countryCombo.setPrefHeight(38);
        countryCombo.setStyle(
                "-fx-background-color: #f8fafc; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-size: 13px; " +
                        "-fx-cursor: hand;"
        );

        // Construction du conteneur de titre pour le ComboBox
        VBox countryGroup = new VBox(6);
        Label countryLabel = new Label("PAYS");
        countryLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        countryGroup.getChildren().addAll(countryLabel, countryCombo);

        // Pré-remplir l'adresse depuis le profil si existante
        String addr = controller.currentUser().getDeliveryAddress();
        if (addr != null && !addr.isBlank()) {
            String[] parts = addr.split(", ");
            if (parts.length > 0) street.setText(parts[0]);
            if (parts.length > 1) city.setText(parts[1]);
            if (parts.length > 2) zip.setText(parts[2]);
            if (parts.length > 3) {
                String savedCountry = parts[3].trim();
                // Si le pays de la BDD est dans notre liste, on le sélectionne
                if (countryCombo.getItems().contains(savedCountry)) {
                    countryCombo.setValue(savedCountry);
                }
            }
        }

        HBox cityZip = new HBox(12);
        VBox cityGroup = createModernField("VILLE", city, "");
        VBox zipGroup = createModernField("CODE POSTAL", zip, "");
        HBox.setHgrow(cityGroup, Priority.ALWAYS);
        HBox.setHgrow(zipGroup, Priority.ALWAYS);
        cityZip.getChildren().addAll(cityGroup, zipGroup);

        addressBlock.getChildren().addAll(
                stepHeader2,
                createModernField("RUE ET NUMÉRO", street, ""),
                cityZip,
                countryGroup // Utilisation du composant liste déroulante
        );

        leftCol.getChildren().addAll(backBtn, paymentBlock, addressBlock);

        // ========== COLONNE DROITE : RÉSUMÉ ==========
        VBox rightCol = new VBox(0);
        rightCol.setPrefWidth(340);
        rightCol.setMinWidth(340);
        rightCol.setMaxWidth(340);
        rightCol.setPadding(new Insets(20));
        rightCol.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label summaryTitle = new Label("Résumé de la commande");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 0 0 14 0;");

        // Articles
        VBox articlesList = new VBox(0);
        for (CartItem item : items) {
            Product p = item.getProduct();

            HBox row = new HBox(12);
            row.setPadding(new Insets(10, 0, 10, 0));
            row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
            row.setAlignment(Pos.CENTER_LEFT);

            // Miniature
            StackPane thumb = new StackPane();
            thumb.setPrefSize(54, 54);
            thumb.setMinSize(54, 54);
            thumb.setMaxSize(54, 54);
            thumb.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
            if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                try {
                    ImageView iv = new ImageView(new Image(resolveImage(p.getImageUrl()), true));
                    iv.setFitWidth(54);
                    iv.setFitHeight(54);
                    iv.setPreserveRatio(true);
                    thumb.getChildren().add(iv);
                } catch (Exception ignored) {
                    thumb.getChildren().add(new Label("📦"));
                }
            } else {
                thumb.getChildren().add(new Label("📦"));
            }

            // Badge quantité
            Label qtyBadge = new Label(String.valueOf(item.getQuantity()));
            qtyBadge.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 1 5;");
            StackPane.setAlignment(qtyBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(qtyBadge, new Insets(-6, -6, 0, 0));
            thumb.getChildren().add(qtyBadge);

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label pName = new Label(p.getName());
            pName.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            pName.setWrapText(true);
            pName.setMaxWidth(160);
            info.getChildren().add(pName);

            Label pPrice = new Label(String.format("%,.2f €", p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            pPrice.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

            row.getChildren().addAll(thumb, info, pPrice);
            articlesList.getChildren().add(row);
        }

        // Code promo
        HBox promoRow = new HBox(8);
        promoRow.setPadding(new Insets(16, 0, 8, 0));
        TextField promoField = new TextField();
        promoField.setPromptText("Code promo");
        promoField.setPrefHeight(38);
        promoField.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 12; -fx-font-size: 13px; -fx-background-color: #f8fafc;");
        HBox.setHgrow(promoField, Priority.ALWAYS);

        Button promoBtn = new Button("Appliquer");
        promoBtn.setPrefHeight(38);
        promoBtn.setStyle("-fx-background-color: white; -fx-border-color: #1e3a5f; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 14; -fx-font-size: 13px; -fx-text-fill: #1e3a5f; -fx-font-weight: bold; -fx-cursor: hand;");
        promoRow.getChildren().addAll(promoField, promoBtn);

        // Separator
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(6, 0, 6, 0));

        // Lignes de prix
        HBox subRow = buildPriceLine("Sous-total", String.format("%,.2f €", total), false);
        HBox delivRow = buildPriceLine("Livraison", "Gratuite", false);

        // Badge "Gratuit" en vert
        Label freeBadge = new Label("✓ Gratuite");
        freeBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 2 8;");
        delivRow.getChildren().remove(delivRow.getChildren().size() - 1);
        delivRow.getChildren().add(freeBadge);

        HBox totalRow = buildPriceLine("Total", String.format("%,.2f €", total), true);

        // Bouton confirmer
        Button btnConfirm = new Button("🔒   Confirmer la commande");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setPrefHeight(50);
        btnConfirm.setStyle(
                "-fx-background-color: #1e3a5f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        VBox.setMargin(btnConfirm, new Insets(14, 0, 0, 0));

        btnConfirm.setOnMouseEntered(e -> btnConfirm.setStyle(
                "-fx-background-color: #162d4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand;"
        ));
        btnConfirm.setOnMouseExited(e -> btnConfirm.setStyle(
                "-fx-background-color: #1e3a5f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 10; -fx-cursor: hand;"
        ));

        // ==========================================
        // LOGIQUE ET SÉCURITÉ DU BOUTON
        // ==========================================
        btnConfirm.setOnAction(e -> {
            // Validation complète de l'adresse de livraison (y compris la sélection du ComboBox)
            if (street.getText().isBlank() || city.getText().isBlank() || zip.getText().isBlank() || countryCombo.getValue() == null) {
                showModernError("Veuillez renseigner complètement l'adresse de livraison.");
                return;
            }

            String deliveryAddress = String.join(", ",
                    street.getText().trim(),
                    city.getText().trim(),
                    zip.getText().trim(),
                    countryCombo.getValue().trim()
            );

            try {
                if (!deliveryAddress.equals(controller.currentUser().getDeliveryAddress())) {
                    controller.updateDeliveryAddress(deliveryAddress);
                }
            } catch (Exception ex) {
                showModernError("Impossible d'enregistrer l'adresse de livraison. Veuillez réessayer.");
                return;
            }

            // Récupérer le mode de paiement choisi
            String selectedMethod = (String) group.getSelectedToggle().getUserData();
            if ("card".equals(selectedMethod)) {
                // 1. Appel au contrôleur pour obtenir le clientSecret de Stripe
                String clientSecret = controller.getPaymentIntentSecret();
                if (clientSecret == null || clientSecret.isBlank()) {
                    showModernError("Impossible d'initialiser le paiement par carte. Veuillez réessayer.");
                    return;
                }

                // 2. Ouvrir la fenêtre de paiement (Webview ou nouvelle fenêtre JavaFX)
                // Passez le clientSecret à votre page payment.html
                openStripePaymentWindow(clientSecret, () -> {
                    // CE CALLBACK EST EXÉCUTÉ SI LE PAIEMENT RÉUSSIT
                    run(() -> {
                        controller.placeOrder(OrderStatus.PAID); // On valide la commande seulement si Stripe dit OK
                        finaliseOrderUI();
                    });
                });

            } else if ("paypal".equals(selectedMethod)) {
                // Logique PayPal similaire à Stripe...
                run(() -> controller.placeOrder(OrderStatus.PAID));
                finaliseOrderUI();

            } else {
                // Mode "cash" (À la livraison)
                run(() -> controller.placeOrder(OrderStatus.PENDING));
                finaliseOrderUI();
            }

        });


        // Note de sécurité
        HBox secureNote = new HBox(6);
        secureNote.setAlignment(Pos.CENTER);
        secureNote.setPadding(new Insets(10, 0, 0, 0));
        Label lockIcon = new Label("🔒");
        lockIcon.setStyle("-fx-font-size: 11px;");
        Label secureLabel = new Label("Paiement 100% sécurisé · SSL");
        secureLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        secureNote.getChildren().addAll(lockIcon, secureLabel);

        rightCol.getChildren().addAll(
                summaryTitle,
                articlesList,
                promoRow,
                sep,
                subRow,
                delivRow,
                totalRow,
                btnConfirm,
                secureNote
        );

        mainLayout.getChildren().addAll(leftCol, rightCol);

        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setContent("PAIEMENT", scroll);
    }

    private void finaliseOrderUI() {
        shell.updateCartCount(0);
        showModernSuccess("🎉 Votre commande a été confirmée avec succès !");
        showOrders();
    }

    private void openStripePaymentWindow(String clientSecret, Runnable onPaymentSuccess) {
        Stage paymentStage = new Stage();
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        final boolean[] paymentHandled = {false};

        Runnable handlePaymentSuccess = () -> {
            if (paymentHandled[0]) {
                return;
            }
            paymentHandled[0] = true;
            paymentStage.close();
            onPaymentSuccess.run();
        };

        // 1. Chargement de la page locale
        // Assurez-vous que payment.html est bien dans le classpath (src/main/resources)
        String publishableKey = controller.getStripePublishableKey();
        if (publishableKey == null || publishableKey.isBlank()) {
            showModernError("La cle publique Stripe est introuvable dans le fichier .env.");
            return;
        }
        String url = getClass().getResource("/payment.html").toExternalForm()
                + "?secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&pk=" + URLEncoder.encode(publishableKey, StandardCharsets.UTF_8);
        webEngine.load(url);

        webEngine.setOnAlert(event -> {
            if ("PAYMENT_SUCCESS".equals(event.getData())) {
                Platform.runLater(handlePaymentSuccess);
            }
        });

        // 2. Création du pont JS -> Java
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");

                // On crée un objet "appInterface" accessible depuis le JS
                window.setMember("appInterface", new Object() {
                    public void onPaymentSuccess() {
                        Platform.runLater(() -> {
                            handlePaymentSuccess.run();
                        });
                    }
                });
                webEngine.executeScript("window.javaBridgeReady = true;");
            }
        });

        paymentStage.setTitle("Paiement Sécurisé");
        paymentStage.setScene(new Scene(webView, 450, 600));
        paymentStage.show();
    }

    private void showModernError(String message) {
        // Exemple d'implémentation : une alerte JavaFX stylisée en rouge
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // On applique un style CSS rapide pour rendre le texte ou le design plus moderne
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        alert.showAndWait();
    }

    // ========== NOUVEAU HELPER : Charger l'image Carte bancaire ==========
    private StackPane buildCardIcon() {
        StackPane pane = new StackPane();
        pane.setPrefSize(52, 34);
        try {
            // Charge l'image depuis tes ressources
            Image img = new Image(getClass().getResourceAsStream("/images/card.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(50);  // Ajuste la largeur de l'image
            iv.setFitHeight(36); // Ajuste la hauteur de l'image
            iv.setPreserveRatio(true);
            pane.getChildren().add(iv);
        } catch (Exception e) {
            // En cas de problème (image introuvable), on met un carré de secours
            pane.getChildren().add(new Label("💳"));
        }
        return pane;
    }

    // ========== NOUVEAU HELPER : Charger l'image Livraison ==========
    private StackPane buildCashIcon() {
        StackPane pane = new StackPane();
        pane.setPrefSize(52, 34);
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/cash.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(50);
            iv.setFitHeight(36);
            iv.setPreserveRatio(true);
            pane.getChildren().add(iv);
        } catch (Exception e) {
            pane.getChildren().add(new Label("🚚"));
        }
        return pane;
    }

    // ========== NOUVEAU HELPER : Charger l'image PayPal ==========
    private StackPane buildPaypalIcon() {
        StackPane pane = new StackPane();
        pane.setPrefSize(52, 34);
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/paypal.png"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(50);
            iv.setFitHeight(36);
            iv.setPreserveRatio(true);
            pane.getChildren().add(iv);
        } catch (Exception e) {
            pane.getChildren().add(new Label("🅿️"));
        }
        return pane;
    }

    // ========== HELPER : Construire une carte méthode ==========
    private VBox buildMethodCard(ToggleGroup group, boolean selected, StackPane icon, String label, String subtitle) {
        RadioButton rb = new RadioButton();
        rb.setToggleGroup(group);
        rb.setSelected(selected);
        rb.setVisible(false);
        rb.setManaged(false);

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setStyle(selected
                ? "-fx-background-color: #f0f5ff; -fx-background-radius: 10; -fx-border-color: #1e3a5f; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;"
                : "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-cursor: hand;"
        );

        Label lblMain = new Label(label);
        lblMain.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        // Indicateur de sélection
        Circle dot = new Circle(5);
        dot.setFill(selected ? Color.web("#1e3a5f") : Color.TRANSPARENT);
        dot.setStroke(Color.web(selected ? "#1e3a5f" : "#cbd5e1"));
        dot.setStrokeWidth(1.5);

        card.getChildren().addAll(icon, lblMain, lblSub, dot);
        card.setUserData(rb);

        card.setOnMouseClicked(e -> {
            rb.setSelected(true);
            group.selectToggle(rb);
        });

        rb.selectedProperty().addListener((obs, old, isSelected) -> {
            dot.setFill(isSelected ? Color.web("#1e3a5f") : Color.TRANSPARENT);
            dot.setStroke(Color.web(isSelected ? "#1e3a5f" : "#cbd5e1"));
            card.setStyle(isSelected
                    ? "-fx-background-color: #f0f5ff; -fx-background-radius: 10; -fx-border-color: #1e3a5f; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;"
                    : "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-cursor: hand;"
            );
        });

        return card;
    }

    // ========== HELPER : Mettre à jour style méthode ==========
    private void updateMethodCardStyle(VBox card, boolean selected) {
        card.setStyle(selected
                ? "-fx-background-color: #f0f5ff; -fx-background-radius: 10; -fx-border-color: #1e3a5f; -fx-border-radius: 10; -fx-border-width: 2; -fx-cursor: hand;"
                : "-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1.5; -fx-cursor: hand;"
        );
    }

    // ========== HELPER : Ligne de prix ==========
    private HBox buildPriceLine(String label, String value, boolean isTotal) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(isTotal ? 12 : 5, 0, 5, 0));
        if (isTotal) {
            row.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");
        }

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: " + (isTotal ? "15" : "13") + "px; -fx-text-fill: "
                + (isTotal ? "#1e293b" : "#64748b") + "; -fx-font-weight: " + (isTotal ? "bold" : "normal") + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle("-fx-font-size: " + (isTotal ? "17" : "13") + "px; -fx-font-weight: "
                + (isTotal ? "bold" : "normal") + "; -fx-text-fill: " + (isTotal ? "#1e293b" : "#64748b") + ";");

        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }

    private Button createFilterButton(String text, boolean active, List<Order> allOrders, VBox container, HBox filterParent) {
        Button b = new Button(text);

        // On définit les styles dans des variables pour plus de clarté
        String activeStyle = "-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #1a237e; -fx-border-color: #1a237e; -fx-background-radius: 20; -fx-border-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;";

        // État initial au chargement
        b.setStyle(active ? activeStyle : inactiveStyle);

        b.setOnAction(e -> {
            // 1. RÉINITIALISER tous les autres boutons du menu
            filterParent.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    ((Button) node).setStyle(inactiveStyle);
                }
            });

            // 2. COLORER le bouton cliqué (celui-ci)
            b.setStyle(activeStyle);

            // 3. FILTRER les données
            container.getChildren().clear();
            for (Order o : allOrders) {
                if (text.equals("Toutes") || o.getStatus().toString().equalsIgnoreCase(text)) {
                    container.getChildren().add(createOrderRow(o));
                }
            }
        });

        return b;
    }
    private void showOrders() {
        if (isGuestMode()) {
            promptGuestCheckout();
            return;
        }
        shell.setSearchHandler(null);

        // 1. RÉCUPÉRATION DES DONNÉES
        List<Order> orders = controller.orders();

        // 2. CALCULS DYNAMIQUES
        int totalCount = orders.size();
        BigDecimal totalMoney = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long deliveredCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.PAID).count();

        // 3. CRÉATION DU CONTENEUR DE LISTE (Une seule fois !)
        VBox ordersList = new VBox(15);
        if (orders.isEmpty()) {
            ordersList.getChildren().add(new Label("Aucune commande dans votre historique."));
        } else {
            for (Order order : orders) {
                ordersList.getChildren().add(createOrderRow(order));
            }
        }

        // 4. CRÉATION DES FILTRES (En utilisant le ordersList créé juste au-dessus)
        HBox filterBox = new HBox(15);
        filterBox.getChildren().addAll(
                createFilterButton("Toutes", true, orders, ordersList, filterBox),
                createFilterButton("PAID", false, orders, ordersList, filterBox),
                createFilterButton("PENDING", false, orders, ordersList, filterBox),
                createFilterButton("DELIVERED", false, orders, ordersList, filterBox),
                createFilterButton("CANCELLED", false, orders, ordersList, filterBox)
        );

        // 5. SECTION STATS (En Bleu Marin pour la visibilité)
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("Total commandes", String.valueOf(totalCount), "Historique complet"),
                createStatCard("Total dépensé", totalMoney + " €", "Toutes périodes"),
                createStatCard("En attente", String.valueOf(pendingCount), "À traiter"),
                createStatCard("Livrées", String.valueOf(deliveredCount), "Compte client")
        );

        // 6. ASSEMBLAGE FINAL
        VBox mainLayout = new VBox(30, filterBox, statsBox, ordersList);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");

        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setContent("MES COMMANDES", scroll);
    }

    private HBox createOrderRow(Order order) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #d1d9e6; -fx-border-width: 1;");

        // Icône en Bleu Marin
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #1a237e;");
        StackPane iconBg = new StackPane(iconLabel);
        iconBg.setPrefSize(50, 50);
        iconBg.setStyle("-fx-background-color: #e8eaf6; -fx-background-radius: 12;");

        VBox info = new VBox(5);
        Label id = new Label("Commande #" + order.getId());
        // On force le NOIR ou MARINE ici pour que ça apparaisse !
        id.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1a237e;");

        Label date = new Label(order.getDate().toString());
        date.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");

        info.getChildren().addAll(id, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label price = new Label(order.getTotalPrice() + " €");
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1a237e;");

        Button btn = new Button("Voir détails >");
        btn.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setOnAction(e -> showOrderDetails(order));

        row.getChildren().addAll(iconBg, info, spacer, price, btn);
        return row;
    }


    private void showOrderDetails(Order order) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        // En-tête avec bouton retour
        Button backBtn = createModernBackButton(this::showOrders);
        HBox backWrapper = new HBox(backBtn);
        backWrapper.setMaxWidth(500);

        Label title = new Label("Détails de la Commande #" + order.getId());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        // Liste des produits
        VBox itemsContainer = new VBox(15);
        List<OrderItem> items = controller.getOrderItems(order.getId());

        for (OrderItem item : items) {
            itemsContainer.getChildren().add(createProductRow(item));
        }

        // ScrollPane pour la liste
        ScrollPane scroll = new ScrollPane(itemsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: white;");

        root.getChildren().addAll(backBtn, title, scroll);
        setContent("DÉTAILS COMMANDE", root);
    }
    private HBox createProductRow(OrderItem item) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #f0f2f5; -fx-border-width: 0 0 1 0; -fx-background-color: white;");

        // --- 1. IMAGE RÉELLE ---
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(100, 100);
        imageContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        // 1. On récupère le produit complet via son ID pour avoir accès à getImageUrl()
        Product p = controller.getProductById(item.getProductId());

        String imageUrl = null;
        if (p != null && p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
            imageUrl = p.getImageUrl();
        }

        // 2. Ensuite, on applique ton test qui marche très bien
        if (imageUrl != null) {
            try {
                // Utilisation de resolveImage si tu as des fichiers locaux, sinon Image direct
                ImageView iv = new ImageView(new Image(imageUrl, true));
                iv.setFitWidth(100);
                iv.setFitHeight(100);
                iv.setPreserveRatio(true);
                imageContainer.getChildren().add(iv);
            } catch (Exception e) {
                imageContainer.getChildren().add(new Label("⚠️"));
            }
        } else {
            Label placeholder = new Label("📦");
            placeholder.setStyle("-fx-font-size: 30px;");
            imageContainer.getChildren().add(placeholder);
        }

        // --- 2. NOM DU PRODUIT (Avec retour à la ligne) ---
        VBox details = new VBox(5);
        details.setPrefWidth(400); // On limite la largeur pour forcer le retour à la ligne

        Label name = new Label(item.getProductName());
        name.setWrapText(true); // <--- TRÈS IMPORTANT : active le retour à la ligne
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a237e;");

        details.getChildren().add(name);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- 3. PRIX ET QUANTITÉ (Alignés à droite) ---
        VBox priceTag = new VBox(5);
        priceTag.setAlignment(Pos.CENTER_RIGHT);

        Label price = new Label(item.getPrice() + " €");
        price.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #e74c3c;");

        Label qty = new Label("Qté: " + item.getQuantity());
        qty.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");

        priceTag.getChildren().addAll(price, qty);

        row.getChildren().addAll(imageContainer, details, spacer, priceTag);
        return row;
    }
    private VBox createStatCard(String title, String value, String subText) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPadding(new Insets(20));
        // Fond bleu très léger pour faire ressortir le texte marine
        card.setStyle("-fx-background-color: #f0f2f9; -fx-background-radius: 12; -fx-border-color: #e0e0e0;");

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #5c6bc0; -fx-font-size: 13px; -fx-font-weight: bold;"); // Bleu gris

        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-font-size: 24px;"); // BLEU MARINE

        Label s = new Label(subText);
        s.setStyle("-fx-text-fill: #7986cb; -fx-font-size: 11px;");

        card.getChildren().addAll(t, v, s);
        return card;
    }


    private void showProfile() {
        if (isGuestMode()) {
            promptGuestCheckout();
            return;
        }
        shell.setSearchHandler(null);
        User user = controller.currentUser();
        if (user == null) return;

        // 1. Conteneur principal
        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: #f8fafc;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(10, 0, 40, 0)); // Padding réduit en haut car le titre est déjà présent

        // --- 2. CADRE BLANC DU PROFIL ---
        VBox profileCard = new VBox(0);
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 24;");
        profileCard.setMaxWidth(900);
        profileCard.setEffect(new DropShadow(25, Color.rgb(0,0,0,0.06)));

        // Hero Section (Avatar + Infos)
        HBox hero = new HBox(30);
        hero.setPadding(new Insets(60, 50, 45, 50));
        hero.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarFrame = new StackPane();
        Circle innerCircle = new Circle(45, Color.web("#f1f5f9"));
        Label initial = new Label(user.getName().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size: 36px; -fx-font-weight: 900; -fx-text-fill: #3b82f6;");
        avatarFrame.getChildren().addAll(innerCircle, initial);

        VBox textSection = new VBox(2);
        Label nameLabel = new Label(user.getName().toUpperCase());
        nameLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: 900; -fx-text-fill: #0f172a; -fx-letter-spacing: -1.2px;");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
        textSection.getChildren().addAll(nameLabel, emailLabel);

        hero.getChildren().addAll(avatarFrame, textSection);

        // Options Container
        VBox optionsContainer = new VBox(12);
        optionsContainer.setPadding(new Insets(0, 50, 50, 50));
        String contactSubtitle = (user.getPhone() != null && !user.getPhone().isEmpty())
                ? user.getPhone() : "Aucun téléphone enregistré";

        optionsContainer.getChildren().addAll(
                createCompatibleRow("Historique des commandes", "Consulter vos achats passés", "📦", this::showOrders),
                createCompatibleRow("Adresse de livraison", user.getDeliveryAddress(), "📍",
                        () -> setContent("ADRESSE", createAddressPage())),
                createCompatibleRow("Sécurité du compte", "Changer votre mot de passe", "🔒",
                        () -> setContent("SÉCURITÉ", createSecurityPage())),
                createCompatibleRow("Contact & Support", "Gérer vos informations de contact et d'assistance", "📱",
                        () -> setContent("CONTACT", createContactPage()))
        );

        profileCard.getChildren().addAll(hero, optionsContainer);

        // --- 3. LOGOUT (Bouton discret en bas) ---
        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-padding: 35; -fx-font-size: 13px;");
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(btnLogout.getStyle() + "-fx-text-fill: #ef4444;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(btnLogout.getStyle().replace("-fx-text-fill: #ef4444;", "-fx-text-fill: #94a3b8;")));
        btnLogout.setOnAction(e -> controller.logout());

        // On assemble sans le bouton retour
        mainContainer.getChildren().addAll(profileCard, btnLogout);

        // --- 4. SCROLLPANE ---
        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc; -fx-border-color: transparent;");

        // On utilise le titre standard du Shell
        setContent("VOTRE PROFIL", scroll);
    }
    private HBox createCompatibleRow(String title, String subtitle, String icon, Runnable action) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(20, 25, 20, 25));
        row.setAlignment(Pos.CENTER_LEFT);

        // Fond gris très léger pour rester dans les tons de ton menu à gauche
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 12;");

        Label lblIcon = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 22px;");

        VBox texts = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");

        Label lblSub = new Label(subtitle != null ? subtitle : "Non défini");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        texts.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("→");
        // On utilise le bleu de ton logo "Stockify" pour l'accent
        arrow.setStyle("-fx-font-size: 18px; -fx-text-fill: #0ea5e9; -fx-font-weight: bold;");

        row.getChildren().addAll(lblIcon, texts, spacer, arrow);

        // --- INTERACTION ---
        row.setOnMouseEntered(e -> {
            // Effet de survol blanc pur avec petite ombre
            row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #0ea5e9; -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand;");
            row.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.05)));
        });

        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 12;");
            row.setEffect(null);
        });

        row.setOnMouseClicked(e -> { if (action != null) action.run(); });

        return row;
    }

    private VBox createFormField(String labelText, Node input) {
        VBox group = new VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        // Style commun pour les champs
        String inputStyle = "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 15; -fx-font-size: 14px;";
        input.setStyle(inputStyle);

        // Effet au focus (changement de bordure)
        input.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) input.setStyle(inputStyle + "-fx-border-color: #3b82f6; -fx-border-width: 1.5;");
            else input.setStyle(inputStyle);
        });

        group.getChildren().addAll(label, input);
        return group;
    }

    private VBox createBaseCard() {
        VBox card = new VBox(25);
        card.setMaxWidth(650);
        card.setPadding(new Insets(35));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #f1f5f9; -fx-border-width: 1;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.04)));
        return card;
    }
    private Button createModernBackButton(Runnable action) {
        Button btn = new Button("←");
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #1e293b; -fx-font-size: 18px; " +
                "-fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 50; -fx-cursor: hand;");

        // Effet d'élévation et de mouvement au survol
        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle() + "-fx-border-color: #3b82f6; -fx-text-fill: #3b82f6;");
            btn.setTranslateX(-3); // Petit décalage vers la gauche
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle().replace("-fx-border-color: #3b82f6; -fx-text-fill: #3b82f6;", "-fx-border-color: #e2e8f0; -fx-text-fill: #1e293b;"));
            btn.setTranslateX(0);
        });
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private VBox createModernField(String labelText, Node input, String iconEmoji) {
        VBox group = new VBox(10);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #64748b; -fx-letter-spacing: 0.8px;");

        HBox inputWrapper = new HBox(12);
        inputWrapper.setAlignment(Pos.CENTER_LEFT);
        inputWrapper.setPadding(new Insets(0, 15, 0, 15));
        inputWrapper.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");

        input.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 12 0; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        HBox.setHgrow(input, Priority.ALWAYS);

        if (iconEmoji != null && !iconEmoji.isBlank()) {
            Label icon = new Label(iconEmoji);
            icon.setStyle("-fx-font-size: 16px;");
            inputWrapper.getChildren().add(icon);
        }
        inputWrapper.getChildren().add(input);

        // Effet Focus sur le wrapper complet
        input.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) {
                inputWrapper.setStyle("-fx-background-color: white; -fx-border-color: #3b82f6; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-background-radius: 12;");
                inputWrapper.setEffect(new DropShadow(10, Color.rgb(59, 130, 246, 0.1)));
            } else {
                inputWrapper.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12;");
                inputWrapper.setEffect(null);
            }
        });

        group.getChildren().addAll(label, inputWrapper);
        return group;
    }
    private ScrollPane wrapInScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        // Supprime la bordure et met le fond en blanc
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white; -fx-border-color: transparent;");

        // Pour masquer totalement la barre visuellement tout en gardant le scroll actif :
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // La barre ne s'affichera JAMAIS
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }
    private Node createAddressPage() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(60));
        container.setStyle("-fx-background-color: white;");
        container.setAlignment(Pos.TOP_CENTER);

        Button backBtn = createModernBackButton(this::showProfile);
        HBox backWrapper = new HBox(backBtn);
        backWrapper.setMaxWidth(500);

        VBox card = new VBox(20); // Espacement homogène à 20
        card.setMaxWidth(500);

        Label title = new Label("Adresse de livraison");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a; -fx-padding: 0 0 10 0;");

        // UI/UX : Bandeau de succès vert (Masqué par défaut)
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #15803d; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14; -fx-font-weight: 600; -fx-font-size: 14px; -fx-alignment: center;");
        successLabel.setMaxWidth(Double.MAX_VALUE);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        // Séparation de l'adresse (Gère les 4 parties : Rue, Ville, Code Postal, Pays)
        String fullAddr = controller.currentUser().getDeliveryAddress() != null ? controller.currentUser().getDeliveryAddress() : "";
        String[] parts = fullAddr.split(", ");

        TextField streetField = new TextField(parts.length > 0 ? parts[0] : "");
        TextField cityField = new TextField(parts.length > 1 ? parts[1] : "");
        TextField zipField = new TextField(parts.length > 2 ? parts[2] : "");
        streetField.setPromptText("Ex: 123 Rue des Fleurs");
        cityField.setPromptText("Casablanca");
        zipField.setPromptText("20000");

        // 1. CRÉATION DE LA LISTE DÉROULANTE (COMBOBOX) POUR LE PAYS
        ComboBox<String> countryComboBox = new ComboBox<>();
        countryComboBox.getItems().addAll("Maroc", "France", "Belgique", "Canada", "Sénégal", "Algérie", "Tunisie");
        countryComboBox.setMaxWidth(Double.MAX_VALUE);

        countryComboBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e2e8f0; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 8 12; " +
                        "-fx-font-size: 14px;"
        );

        if (parts.length > 3 && !parts[3].isEmpty() && countryComboBox.getItems().contains(parts[3])) {
            countryComboBox.setValue(parts[3]);
        } else {
            countryComboBox.setValue("Maroc");
        }

        VBox countryGroup = new VBox(8);
        Label countryLabel = new Label("PAYS");
        countryLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748b; -fx-letter-spacing: 1px;");
        countryGroup.getChildren().addAll(countryLabel, countryComboBox);

        VBox streetGroup = createModernField("RUE ET NUMÉRO", streetField, "");

        HBox cityZipRow = new HBox(15);
        VBox cityGroup = createModernField("VILLE", cityField, "");
        VBox zipGroup = createModernField("CODE POSTAL", zipField, "");
        HBox.setHgrow(cityGroup, Priority.ALWAYS);
        HBox.setHgrow(zipGroup, Priority.ALWAYS);
        cityZipRow.getChildren().addAll(cityGroup, zipGroup);

        Button saveBtn = new Button("Enregistrer l'adresse");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 16; -fx-background-radius: 10; -fx-cursor: hand;");

        VBox.setMargin(saveBtn, new Insets(15, 0, 0, 0));

        // 2. ACTION DE SAUVEGARDER
        saveBtn.setOnAction(e -> {
            String selectedCountry = countryComboBox.getValue() != null ? countryComboBox.getValue() : "";

            String combined = streetField.getText().trim() + ", " +
                    cityField.getText().trim() + ", " +
                    zipField.getText().trim() + ", " +
                    selectedCountry;

            // Mise à jour locale et en base de données
            controller.currentUser().setDeliveryAddress(combined);
            controller.updateProfile(controller.currentUser());

            // UI/UX : Afficher le bandeau de confirmation
            successLabel.setText("✅ Adresse de livraison enregistrée !");
            successLabel.setVisible(true);
            successLabel.setManaged(true);

            // Désactive le bouton pour éviter les écritures SQL multiples involontaires
            saveBtn.setDisable(true);

            // Petite transition de 1.5s avant de retourner à l'écran de profil
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                saveBtn.setDisable(false);
                showProfile();
            });
            delay.play();
        });

        // Assemblage final
        card.getChildren().addAll(title, successLabel, streetGroup, cityZipRow, countryGroup, saveBtn);
        container.getChildren().addAll(backWrapper, card);

        return wrapInScrollPane(container);
    }
    private Node createContactPage() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(40, 60, 60, 60));
        container.setStyle("-fx-background-color: white;");
        container.setAlignment(Pos.TOP_CENTER);

        Button backBtn = createModernBackButton(this::showProfile);
        HBox backWrapper = new HBox(backBtn);
        backWrapper.setMaxWidth(550);

        VBox card = new VBox(20); // Espacement ajusté à 20 pour intégrer le bandeau proprement
        card.setMaxWidth(550);

        Label title = new Label("Coordonnées");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #0f172a; -fx-letter-spacing: -1px;");

        // UI/UX : Bandeau de succès vert (Masqué par défaut)
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #15803d; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14; -fx-font-weight: 600; -fx-font-size: 14px; -fx-alignment: center;");
        successLabel.setMaxWidth(Double.MAX_VALUE);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        TextField nameField = new TextField(controller.currentUser().getName());
        TextField emailField = new TextField(controller.currentUser().getEmail());
        TextField phoneField = new TextField(controller.currentUser().getPhone());
        nameField.setPromptText("Nom complet");
        emailField.setPromptText("email@exemple.com");
        phoneField.setPromptText("+212 600 000 000");

        Button saveBtn = new Button("Mettre à jour le profil");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 16; -fx-background-radius: 12; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            // 1. Mise à jour de l'objet utilisateur
            User u = controller.currentUser();
            u.setName(nameField.getText());
            u.setEmail(emailField.getText());
            u.setPhone(phoneField.getText());

            // 2. Envoi dans la base de données via le controller
            controller.updateProfile(u);

            // 3. UI/UX : Afficher visuellement le message de succès vert
            successLabel.setText("✅ Profil mis à jour avec succès !");
            successLabel.setVisible(true);
            successLabel.setManaged(true);

            // Désactiver temporairement le bouton pour éviter les double-clics
            saveBtn.setDisable(true);

            // 4. Petite pause de 1.5 seconde pour apprécier l'effet visuel avant redirection
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                saveBtn.setDisable(false);
                showProfile(); // Retour à la vue profil
            });
            delay.play();
        });

        // Assemblage avec inclusion du message de succès juste sous le titre
        card.getChildren().addAll(
                title,
                successLabel,
                createModernField("NOM COMPLET", nameField, "👤"),
                createModernField("ADRESSE EMAIL", emailField, "📧"),
                createModernField("TÉLÉPHONE", phoneField, "📱"),
                saveBtn
        );

        container.getChildren().addAll(backWrapper, card);
        return wrapInScrollPane(container);
    }
    private Node createSecurityPage() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(40, 60, 60, 60));
        container.setStyle("-fx-background-color: white;");
        container.setAlignment(Pos.TOP_CENTER);

        Button backBtn = createModernBackButton(this::showProfile);
        HBox backWrapper = new HBox(backBtn);
        backWrapper.setMaxWidth(550);

        VBox card = new VBox(20);
        card.setMaxWidth(550);

        Label title = new Label("Sécurité du compte");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #0f172a; -fx-letter-spacing: -1px;");

        // Message de succès (Vert UI/UX) - Masqué au début
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: #15803d; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14; -fx-font-weight: 600; -fx-font-size: 14px; -fx-alignment: center;");
        successLabel.setMaxWidth(Double.MAX_VALUE);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        PasswordField currentPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();

        // 1. Création des messages d'erreur textuels en rouge (masqués par défaut)
        Label errorCurrent = new Label();
        errorCurrent.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: -10 0 10 5;");
        errorCurrent.setVisible(false);
        errorCurrent.setManaged(false);

        Label errorNew = new Label();
        errorNew.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: -10 0 10 5;");
        errorNew.setVisible(false);
        errorNew.setManaged(false);

        Label errorConfirm = new Label();
        errorConfirm.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: -10 0 10 5;");
        errorConfirm.setVisible(false);
        errorConfirm.setManaged(false);

        Button saveBtn = new Button("Changer le mot de passe");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 800; -fx-padding: 16; -fx-background-radius: 12; -fx-cursor: hand;");

        // Styles des cases d'origine
        String styleNormal = "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-background-radius: 12;";
        String styleErreur = "-fx-background-color: #fff1f2; -fx-border-color: #ef4444; -fx-border-radius: 12; -fx-background-radius: 12;";

        saveBtn.setOnAction(e -> {
            // RÉINITIALISATION DES ERREURS, DU SUCCÈS ET DES STYLES VISUELS
            successLabel.setVisible(false); successLabel.setManaged(false);
            errorCurrent.setVisible(false); errorCurrent.setManaged(false);
            errorNew.setVisible(false);     errorNew.setManaged(false);
            errorConfirm.setVisible(false); errorConfirm.setManaged(false);

            currentPass.getParent().setStyle(styleNormal);
            newPass.getParent().setStyle(styleNormal);
            confirmPass.getParent().setStyle(styleNormal);

            String ancienMdp = currentPass.getText();
            String nouveauMdp = newPass.getText();
            String confirmationMdp = confirmPass.getText();

            // ÉTAPE A : Validation des champs vides
            if (ancienMdp.isEmpty()) {
                errorCurrent.setText("Veuillez saisir votre mot de passe actuel.");
                errorCurrent.setVisible(true); errorCurrent.setManaged(true);
                currentPass.getParent().setStyle(styleErreur);
                return;
            }
            if (nouveauMdp.isEmpty()) {
                errorNew.setText("Veuillez saisir un nouveau mot de passe.");
                errorNew.setVisible(true); errorNew.setManaged(true);
                newPass.getParent().setStyle(styleErreur);
                return;
            }

            // ÉTAPE B : Validation de la correspondance Nouveau == Confirmation
            if (!nouveauMdp.equals(confirmationMdp)) {
                errorConfirm.setText("Les mots de passe ne correspondent pas.");
                errorConfirm.setVisible(true); errorConfirm.setManaged(true);
                confirmPass.getParent().setStyle(styleErreur);
                return;
            }

            // ÉTAPE C : APPEL AU CONTROLEUR MVC
            int resultat = controller.changerMotDePasse(ancienMdp, nouveauMdp);

            if (resultat == 1) {
                // UI/UX : Afficher le message de succès vert
                successLabel.setText("✅ Mot de passe mis à jour avec succès !");
                successLabel.setVisible(true);
                successLabel.setManaged(true);

                // Vider les champs après le succès
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();

                // Petite pause de 1.5 seconde pour laisser l'utilisateur lire le succès avant redirection
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                delay.setOnFinished(event -> showProfile());
                delay.play();

            } else if (resultat == -1) {
                errorCurrent.setText("Le mot de passe actuel est incorrect.");
                errorCurrent.setVisible(true); errorCurrent.setManaged(true);
                currentPass.getParent().setStyle(styleErreur);
            } else {
                errorCurrent.setText("Une erreur est survenue lors de la mise à jour.");
                errorCurrent.setVisible(true); errorCurrent.setManaged(true);
            }
        });

        // Assemblage avec l'insertion du bandeau de succès vert tout en haut de la carte
        card.getChildren().addAll(
                title,
                successLabel, // Placé directement sous le titre pour être bien visible
                createModernField("MOT DE PASSE ACTUEL", currentPass, "🔒"),
                errorCurrent,
                createModernField("NOUVEAU MOT DE PASSE", newPass, "🔑"),
                errorNew,
                createModernField("CONFIRMER LE MOT DE PASSE", confirmPass, "✅"),
                errorConfirm,
                saveBtn
        );

        container.getChildren().addAll(backWrapper, card);
        return wrapInScrollPane(container);
    }

    public void showModernSuccess(String message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #e2e8f0; -fx-border-radius: 16; -fx-border-width: 1;");
        root.setEffect(new DropShadow(25, Color.rgb(0, 0, 0, 0.15)));

        // Icône de validation animée (Statique ici pour la simplicité)
        StackPane iconContainer = new StackPane();
        Circle circle = new Circle(30, Color.web("#dcfce7"));
        Label check = new Label("✓");
        check.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 28px; -fx-font-weight: bold;");
        iconContainer.getChildren().addAll(circle, check);

        Label title = new Label("Succès");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        msg.setWrapText(true);
        msg.setAlignment(Pos.CENTER);

        Button btnOk = new Button("Fermer");
        btnOk.setPrefWidth(120);
        btnOk.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10; -fx-font-weight: bold; -fx-cursor: hand;");
        btnOk.setOnAction(e -> dialog.close());

        root.getChildren().addAll(iconContainer, title, msg, btnOk);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private TextField createEditField(VBox container, String labelText, String value) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        TextField field = new TextField(value);
        field.setPrefHeight(40);
        field.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 5; -fx-background-radius: 5;");
        box.getChildren().addAll(lbl, field);
        container.getChildren().add(box);
        return field;
    }

    private PasswordField createPasswordField(VBox container, String labelText) {
        VBox box = new VBox(5);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        PasswordField field = new PasswordField();
        field.setPrefHeight(40);
        field.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 5; -fx-background-radius: 5;");
        box.getChildren().addAll(lbl, field);
        container.getChildren().add(box);
        return field;
    }
    // Méthode utilitaire pour créer les blocs de texte
    private VBox createInfoField(String labelText, String valueText) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        Label val = new Label(valueText);
        val.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 14px;");
        return new VBox(5, lbl, val);
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

    public List<NavItem> navItems() {
        return List.of(
                new NavItem(IconFactory.catalogIcon(), "Catalog", this::showCatalog),
                new NavItem(IconFactory.cartIcon(), "Cart", this::showCart),
                new NavItem(IconFactory.ordersIcon(), "Orders", this::showOrders),
                new NavItem(IconFactory.profileIcon(), "Profile", this::showProfile)
        );
    }
}
