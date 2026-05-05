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

//    private void showCatalog() {
//        // 1. Petit Header Noir (Promo)
//        Label promoLabel = new Label("LIVRAISON GRATUITE DÈS 39€ D'ACHAT | RETOURS GRATUITS");
//        promoLabel.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 8px; -fx-font-weight: bold;");
//        promoLabel.setMaxWidth(Double.MAX_VALUE);
//        promoLabel.setAlignment(Pos.CENTER);
//
//        // 2. Barre de Catégories Horizontale (HBox)
//        HBox categoryBar = new HBox(30); // Espace entre catégories augmenté à 30
//        categoryBar.setAlignment(Pos.CENTER_LEFT);
//        categoryBar.setPadding(new Insets(0, 20, 0, 20));
//        categoryBar.setStyle("-fx-background-color: white;");
//
//        // 3. ScrollPane spécial pour la barre (Fixe en haut, scroll horizontal uniquement)
//        ScrollPane categoryScroll = new ScrollPane(categoryBar);
//        categoryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // INTERDIT le scroll haut/bas
//        categoryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Autorise gauche/droite
//        categoryScroll.setFitToHeight(true);
//        
//        // ON AUGMENTE LA HAUTEUR ICI (60px au lieu de automatique)
//        categoryScroll.setMinHeight(60); 
//        categoryScroll.setPrefHeight(60);
//        
//        categoryScroll.setStyle("-fx-background-color: white; -fx-background: white; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
//
//        FlowPane grid = new FlowPane(15, 15);
//        grid.setPadding(new Insets(20));
//        grid.setAlignment(Pos.TOP_CENTER);
//
//        java.util.function.Consumer<String> filterAction = (categoryName) -> {
//            grid.getChildren().clear();
//            controller.products("").stream()
//                    .filter(p -> categoryName.equals("TOUT VOIR") || 
//                            (p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(categoryName)))
//                    .forEach(product -> grid.getChildren().add(productCard(product)));
//            
//            categoryBar.getChildren().forEach(node -> {
//                if (node instanceof Button b) {
//                    // FORCE LE TEXTE A S'AFFICHER EN ENTIER
//                    b.setMinWidth(Region.USE_PREF_SIZE); 
//                    b.setMinHeight(50); // Boutons plus hauts pour cliquer facilement
//                    
//                    if (b.getText().equals(categoryName)) {
//                        b.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 0 0 3 0; -fx-cursor: hand;");
//                    } else {
//                        b.setStyle("-fx-background-color: transparent; -fx-font-weight: normal; -fx-text-fill: #666; -fx-cursor: hand; -fx-border-color: transparent;");
//                    }
//                }
//            });
//        };
//
//        // Remplissage
//        try {
//            List<String> cats = new java.util.ArrayList<>();
//            cats.add("TOUT VOIR");
//            cats.addAll(controller.products("").stream()
//                    .map(Product::getCategoryName)
//                    .filter(c -> c != null && !c.isBlank())
//                    .distinct().map(String::toUpperCase).toList());
//
//            for (String catName : cats) {
//                Button catBtn = new Button(catName);
//                catBtn.setMinWidth(Region.USE_PREF_SIZE); // Crucial pour éviter les "..."
//                catBtn.setOnAction(e -> filterAction.accept(catName));
//                categoryBar.getChildren().add(catBtn);
//            }
//        } catch (Exception e) { e.printStackTrace(); }
//
//        shell.setSearchHandler(query -> {
//            grid.getChildren().clear();
//            controller.products(query).forEach(p -> grid.getChildren().add(productCard(p)));
//        });
//
//        filterAction.accept("TOUT VOIR");
//
//        // Scroll principal pour les produits
//        ScrollPane mainProductScroll = new ScrollPane(grid);
//        mainProductScroll.setFitToWidth(true);
//        mainProductScroll.setStyle("-fx-background-color: white; -fx-background: white;");
//
//        // L'ASSEMBLAGE : promo + categoryScroll sont en dehors du scroll des produits
//        // donc ils restent fixes en haut.
//        VBox layout = new VBox(0, promoLabel, categoryScroll, mainProductScroll);
//        VBox.setVgrow(mainProductScroll, Priority.ALWAYS);
//
//        setContent("Product Catalog", layout);
//    }
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
        VBox layout = new VBox(0, promoLabel, categoryScroll, mainProductScroll);

        setContent("Product Catalog", layout);
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
        Label price = new Label(product.getPrice() + " MAD");
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
    
//    private StackPane productVisual(Product product) {
//        StackPane container = new StackPane();
//        container.setPrefHeight(220); // Hauteur plus grande pour le style SHEIN
//        container.setMaxWidth(Double.MAX_VALUE);
//        
//        // Empêche le StackPane de limiter la taille de l'image
//        container.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 8 8 0 0; -fx-overflow: hidden;");
//
//        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
//            try {
//                Image img = new Image(resolveImage(product.getImageUrl()), true);
//                ImageView iv = new ImageView(img);
//                
//                // --- CONFIGURATION STYLE SHEIN ---
//                iv.setPreserveRatio(true);
//                // On lie la largeur de l'image à celle du container pour qu'elle prenne toute la place
//                iv.fitWidthProperty().bind(container.widthProperty());
//                iv.setFitHeight(220); 
//                
//                container.getChildren().add(iv);
//                return container;
//            } catch (Exception ignored) {}
//        }
//        
//        Label glyph = new Label(productGlyph(product.getCategoryName()));
//        glyph.setStyle("-fx-font-size: 50;");
//        container.getChildren().add(glyph);
//        return container;
//    }
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
            run(controller::placeOrder);
            showModernSuccess("Votre commande a été passée avec succès !");
            shell.updateCartCount(0);
            showCart();
        });

        rightColumn.getChildren().addAll(lblResume, sep, subtotalRow, deliveryRow, btnOrder);

        mainLayout.getChildren().addAll(leftColumn, rightColumn);
        setContent("Panier", mainLayout);
    }


//    private void showOrders() {
//        shell.setSearchHandler(null);
//        
//        VBox container = new VBox(20);
//        container.setPadding(new Insets(30));
//        container.setStyle("-fx-background-color: #f8f9fa;"); // Gris très clair moderne
//        container.setAlignment(Pos.TOP_CENTER);
//
//        List<Order> orders = controller.orders();
//        
//        if (orders.isEmpty()) {
//            Label emptyLabel = new Label("VOUS N'AVEZ PAS ENCORE DE COMMANDES");
//            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-font-weight: bold;");
//            container.getChildren().add(emptyLabel);
//        } else {
//            for (Order order : orders) {
//                container.getChildren().add(createOrderCard(order));
//            }
//        }
//
//        ScrollPane scroll = new ScrollPane(container);
//        scroll.setFitToWidth(true);
//        scroll.setStyle("-fx-background-color: transparent; -fx-background: #f8f9fa;");
//        
//        setContent("MES COMMANDES", scroll);
//    }
 // Assure-toi que les 4 paramètres sont bien là
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
            createStatCard("Total dépensé", totalMoney + " MAD", "Toutes périodes"),
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
//    private HBox createOrderCard(Order order) {
//        HBox card = new HBox(20);
//        card.setAlignment(Pos.CENTER_LEFT);
//        card.setPadding(new Insets(20));
//        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #eee; -fx-border-width: 1;");
//        
//        // Ombre légère
//        card.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.05)));
//
//        // Infos Commande
//        VBox info = new VBox(5);
//        Label idLabel = new Label("Commande #" + order.getId());
//        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//        
//        Label dateLabel = new Label(order.getDate().toString());
//        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
//        info.getChildren().addAll(idLabel, dateLabel);
//
//        // Statut (Style Badge)
//        Label statusLabel = new Label("PAYÉ"); // Tu peux utiliser order.getStatus()
//        statusLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");
//
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//
//        // Prix
//        Label totalLabel = new Label(order.getTotalPrice() + " MAD");
//        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000;");
//
//        // Bouton Détails
//        Button btnDetails = new Button("VOIR DÉTAILS");
//        btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 1; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11px;");
//        btnDetails.setOnAction(e -> showOrderDetails(order)); // On passe à la vue des order_items
//
//        card.getChildren().addAll(info, statusLabel, spacer, totalLabel, btnDetails);
//        
//        // Animation au survol
//        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1;"));
//        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #eee; -fx-border-width: 1;"));
//
//        return card;
//    }
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

        Label price = new Label(order.getTotalPrice() + " MAD");
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
        Button backBtn = new Button("← Retour aux commandes");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a237e; -fx-font-weight: bold; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showOrders());

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
        
        // On récupère l'image réelle via l'URL stockée dans le produit
//        String imageUrl = controller.getImageUrl(item.getProductId()); 
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            ImageView iv = new ImageView(new Image(imageUrl, true)); // 'true' pour chargement en arrière-plan
//            iv.setFitWidth(100);
//            iv.setFitHeight(100);
//            iv.setPreserveRatio(true);
//            imageContainer.getChildren().add(iv);
//        } else {
//            Label placeholder = new Label("📦");
//            placeholder.setStyle("-fx-font-size: 30px;");
//            imageContainer.getChildren().add(placeholder);
//        }
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
        
        Label price = new Label(item.getPrice() + " MAD");
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
        // CRITIQUE : On récupère l'utilisateur à jour depuis le controller
        User user = controller.currentUser();

        // --- SECTION 1: PROFIL RÉSUMÉ (Header) ---
        HBox profileHeader = new HBox(20);
        profileHeader.getStyleClass().add("profile-card-container");
        profileHeader.setPadding(new Insets(25));
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        profileHeader.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        // Avatar (Cercle avec Initiales ou Image)
        StackPane avatar = new StackPane();
        Circle circle = new Circle(40, javafx.scene.paint.Color.web("#3b82f6"));

        // On recalcule les initiales (au cas où le nom a changé aussi)
        String userInitials = (user.getName() != null && user.getName().length() >= 2)
                ? user.getName().substring(0, 2).toUpperCase()
                : "??";
        Label initials = new Label(userInitials);
        initials.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // LOGIQUE DE MISE À JOUR DE L'IMAGE
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                // On charge l'image (le String contient l'URI du fichier ou l'URL)
                Image img = new Image(user.getProfilePicture(), true); // 'true' pour chargement en arrière-plan
                ImageView iv = new ImageView(img);
                iv.setFitWidth(80);
                iv.setFitHeight(80);
                iv.setPreserveRatio(false); // On force le remplissage du cercle

                // On crée un clip circulaire parfait pour l'image
                Circle clip = new Circle(40, 40, 40);
                iv.setClip(clip);

                avatar.getChildren().addAll(circle, iv);
            } catch (Exception e) {
                // Si l'image ne peut pas être chargée, on remet les initiales
                avatar.getChildren().addAll(circle, initials);
                System.err.println("Erreur chargement image showProfile: " + e.getMessage());
            }
        } else {
            // Pas de photo : on met les initiales
            avatar.getChildren().addAll(circle, initials);
        }

        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");

        Label statusBadge = new Label(user.getStatus().toString());
        statusBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 2 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
        nameBox.getChildren().addAll(nameLabel, roleLabel, statusBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnModifier1 = new Button("Modifier");
        btnModifier1.getStyleClass().add("btn-outline");
        btnModifier1.setOnAction(event -> showEditProfile());

        // Icône du bouton Modifier
        try {
            Image iconImage = new Image(getClass().getResourceAsStream("/images/img.png"));
            if (iconImage != null) {
                ImageView iconView = new ImageView(iconImage);
                iconView.setFitWidth(16); iconView.setFitHeight(16);
                btnModifier1.setGraphic(iconView);
                btnModifier1.setGraphicTextGap(10);
            }
        } catch (Exception e) {
            // On ignore si l'icône ne charge pas
        }

        profileHeader.getChildren().addAll(avatar, nameBox, spacer, btnModifier1);

        // --- SECTION 2: INFORMATIONS PERSONNELLES ---
        VBox infoCard = new VBox(20);
        infoCard.setPadding(new Insets(25));
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        Label sectionTitle = new Label("Informations Personnelles");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane grid = new GridPane();
        grid.setHgap(40); grid.setVgap(20);
        grid.add(createInfoField("Nom complet", user.getName()), 0, 0);
        grid.add(createInfoField("Adresse Email", user.getEmail()), 0, 1);
        grid.add(createInfoField("Téléphone", (user.getPhone() != null && !user.getPhone().isEmpty()) ? user.getPhone() : "Non renseigné"), 0, 2);
        grid.add(createInfoField("Adresse de livraison", (user.getDeliveryAddress() != null && !user.getDeliveryAddress().isEmpty()) ? user.getDeliveryAddress() : "Non renseignée"), 0, 3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        grid.add(createInfoField("Membre depuis", (user.getCreatedAt() != null) ? user.getCreatedAt().format(formatter) : "Date inconnue"), 0, 4);

        infoCard.getChildren().addAll(sectionTitle, grid);

        VBox profileContainer = new VBox(25, profileHeader, infoCard);
        profileContainer.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(profileContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        setContent("Profil", scrollPane);
    }

    private void showEditProfile() {
        User user = controller.currentUser();
        // Variable pour stocker le chemin de l'image (utilisée dans les événements)
        final String[] currentPath = {user.getProfilePicture()};

        VBox editForm = new VBox(25);
        editForm.setPadding(new Insets(25));
        editForm.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label title = new Label("Informations personnelles");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // --- SECTION PHOTO DE PROFIL DYNAMIQUE ---
        VBox photoSection = new VBox(10);
        Label lblPhoto = new Label("Photo de profil");
        lblPhoto.setStyle("-fx-text-fill: #64748b;");

        HBox photoRow = new HBox(15);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarContainer = new StackPane();
        Circle circle = new Circle(30, javafx.scene.paint.Color.web("#3b82f6"));

        // Initiales par défaut
        Label initials = new Label(user.getName().substring(0, Math.min(2, user.getName().length())).toUpperCase());
        initials.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Bouton Modifier / Choisir
        Button btnAction = new Button();
        btnAction.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #3b82f6; -fx-background-radius: 20; -fx-font-weight: bold;");

        // Bouton Supprimer (Rouge)
        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-weight: bold;");

        Label lblFileHint = new Label("JPG, PNG, GIF jusqu'à 2MB");
        lblFileHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        // --- FONCTION DE MISE À JOUR DE L'APPARENCE DE LA PHOTO ---
        Runnable refreshPhotoUI = () -> {
            avatarContainer.getChildren().clear();
            photoRow.getChildren().clear();
            photoRow.getChildren().add(avatarContainer);

            if (currentPath[0] != null && !currentPath[0].isEmpty()) {
                // État avec Image
                try {
                    Image img = new Image(currentPath[0]);
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(60); iv.setFitHeight(60);
                    iv.setClip(new Circle(30, 30, 30));
                    avatarContainer.getChildren().addAll(circle, iv);

                    btnAction.setText("Modifier l'image");
                    photoRow.getChildren().addAll(btnAction, btnDelete);
                } catch (Exception e) {
                    avatarContainer.getChildren().addAll(circle, initials);
                    btnAction.setText("Choisir un fichier");
                    photoRow.getChildren().addAll(btnAction, lblFileHint);
                }
            } else {
                // État avec Initiales (Supprimé ou Vide)
                avatarContainer.getChildren().addAll(circle, initials);
                btnAction.setText("Choisir un fichier");
                photoRow.getChildren().addAll(btnAction, lblFileHint);
            }
        };

        // Actions des boutons photo
        btnAction.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"));
            java.io.File selectedFile = fileChooser.showOpenDialog(editForm.getScene().getWindow());
            if (selectedFile != null) {
                currentPath[0] = selectedFile.toURI().toString();
                refreshPhotoUI.run();
            }
        });

        btnDelete.setOnAction(e -> {
            currentPath[0] = null;
            refreshPhotoUI.run();
        });

        // Initialisation de la vue photo
        refreshPhotoUI.run();
        photoSection.getChildren().addAll(lblPhoto, photoRow);

        // --- FORMULAIRE ---
        VBox fieldsBox = new VBox(15);

        TextField txtNom = createEditField(fieldsBox, "Nom complet *", user.getName());
        TextField txtEmail = createEditField(fieldsBox, "Adresse e-mail *", user.getEmail());
        TextField txtPhone = createEditField(fieldsBox, "Téléphone", (user.getPhone() != null) ? user.getPhone() : "");
        TextField txtAdresse = createEditField(fieldsBox, "Adresse de livraison", (user.getDeliveryAddress() != null) ? user.getDeliveryAddress() : "");

        // --- CHAMPS DE MOT DE PASSE ---
        PasswordField txtCurrentPass = createPasswordField(fieldsBox, "Mot de passe actuel ");
        PasswordField txtNewPass = createPasswordField(fieldsBox, "Nouveau mot de passe");
        PasswordField txtConfirmPass = createPasswordField(fieldsBox, "Confirmer le mot de passe");

        // --- BOUTONS D'ACTION ---
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 0, 0, 0));

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-text-fill: #64748b;");
        btnCancel.setOnAction(e -> showProfile());

        Button btnSave = new Button("Enregistrer les modifications");
        btnSave.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnSave.setPrefHeight(40);
        btnSave.setMinWidth(220);

        btnSave.setOnAction(e -> {
            // Mise à jour des données
            user.setName(txtNom.getText());
            user.setPhone(txtPhone.getText());
            user.setDeliveryAddress(txtAdresse.getText());
            user.setProfilePicture(currentPath[0]);

            try {
                controller.updateProfile(user);
                showModernSuccess("Votre profil a été mis à jour avec succès !");
                showProfile();
            } catch (Exception ex) {
                Ui.error(ex);
            }
        });

        actions.getChildren().addAll(btnCancel, btnSave);
        editForm.getChildren().addAll(title, photoSection, fieldsBox, actions);

        ScrollPane scroll = new ScrollPane(editForm);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        setContent("Modifier le profil", scroll);
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
    
    public List<AppShell.NavItem> navItems() {
        return List.of(
            new AppShell.NavItem("/images/icons/catalog.jpeg", "Catalog", this::showCatalog),
            new AppShell.NavItem("/images/icons/cart.png", "Cart", this::showCart),
            new AppShell.NavItem("/images/icons/orders.png", "Orders", this::showOrders),
            new AppShell.NavItem("/images/icons/profil.png", "Profile", this::showProfile)
        );
    }
}