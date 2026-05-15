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
import com.ecommerce.stockapp.util.IconFactory;
import com.ecommerce.stockapp.view.NavItem;
import javafx.scene.Node;
import javafx.scene.control.PasswordField; // Profites-en pour ajouter celui-ci aussi


public class CustomerDashboardView {
    private final CustomerController controller;
    private final AppShell shell;

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
                createCompatibleRow("Contact & Support", contactSubtitle, "📱",
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

        Label icon = new Label(iconEmoji);
        icon.setStyle("-fx-font-size: 16px;");

        input.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 12 0; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        HBox.setHgrow(input, Priority.ALWAYS);

        inputWrapper.getChildren().addAll(icon, input);

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

        VBox card = new VBox(20);
        card.setMaxWidth(500);

        Label title = new Label("Adresse de livraison");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0f172a; -fx-padding: 0 0 10 0;");

        // Séparation de l'adresse
        String fullAddr = controller.currentUser().getDeliveryAddress() != null ? controller.currentUser().getDeliveryAddress() : "";
        String[] parts = fullAddr.split(", ");

        TextField streetField = new TextField(parts.length > 0 ? parts[0] : "");
        TextField cityField = new TextField(parts.length > 1 ? parts[1] : "");
        TextField zipField = new TextField(parts.length > 2 ? parts[2] : "");

        // Utilisation de la version sans icône (Option 1)
        VBox streetGroup = createModernField("RUE ET NUMÉRO", streetField, "Ex: 123 Rue des Fleurs");

        HBox cityZipRow = new HBox(15);
        VBox cityGroup = createModernField("VILLE", cityField, "Casablanca");
        VBox zipGroup = createModernField("CODE POSTAL", zipField, "20000");
        HBox.setHgrow(cityGroup, Priority.ALWAYS);
        HBox.setHgrow(zipGroup, Priority.ALWAYS);
        cityZipRow.getChildren().addAll(cityGroup, zipGroup);

        Button saveBtn = new Button("Enregistrer l'adresse");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 16; -fx-background-radius: 10; -fx-cursor: hand;");

        // Ajout d'une petite marge au dessus du bouton
        VBox.setMargin(saveBtn, new Insets(15, 0, 0, 0));

        saveBtn.setOnAction(e -> {
            String combined = streetField.getText() + ", " + cityField.getText() + ", " + zipField.getText();
            controller.currentUser().setDeliveryAddress(combined);
            controller.updateProfile(controller.currentUser());
            showProfile();
        });

        card.getChildren().addAll(title, streetGroup, cityZipRow, saveBtn);
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

        VBox card = new VBox(35);
        card.setMaxWidth(550);

        Label title = new Label("Coordonnées");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #0f172a; -fx-letter-spacing: -1px;");

        TextField nameField = new TextField(controller.currentUser().getName());
        TextField emailField = new TextField(controller.currentUser().getEmail());
        TextField phoneField = new TextField(controller.currentUser().getPhone());
        phoneField.setPromptText("+212 600 000 000");

        Button saveBtn = new Button("Mettre à jour le profil");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 16; -fx-background-radius: 12; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            User u = controller.currentUser();
            u.setName(nameField.getText());
            u.setEmail(emailField.getText());
            u.setPhone(phoneField.getText());
            controller.updateProfile(u);
            showProfile();
        });

        card.getChildren().addAll(
                title,
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

        VBox card = new VBox(35);
        card.setMaxWidth(550);

        Label title = new Label("Sécurité du compte");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #0f172a; -fx-letter-spacing: -1px;");

        PasswordField currentPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        PasswordField confirmPass = new PasswordField();

        Button saveBtn = new Button("Changer le mot de passe");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 800; -fx-padding: 16; -fx-background-radius: 12; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            if (newPass.getText().isEmpty() || !newPass.getText().equals(confirmPass.getText())) {
                confirmPass.getParent().setStyle("-fx-background-color: #fff1f2; -fx-border-color: #ef4444; -fx-border-radius: 12; -fx-background-radius: 12;");
            } else {
                // Ici tu appelles ton controller pour changer le password
                showProfile();
            }
        });

        card.getChildren().addAll(
                title,
                createModernField("MOT DE PASSE ACTUEL", currentPass, "🔒"),
                createModernField("NOUVEAU MOT DE PASSE", newPass, "🔑"),
                createModernField("CONFIRMER LE MOT DE PASSE", confirmPass, "✅"),
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

    public List<AppShell.NavItem> navItems() {
        return List.of(
            new AppShell.NavItem(IconFactory.catalogIcon(), "Catalog", this::showCatalog),
            new AppShell.NavItem(IconFactory.cartIcon(), "Cart", this::showCart),
            new AppShell.NavItem(IconFactory.ordersIcon(), "Orders", this::showOrders),
            new AppShell.NavItem(IconFactory.profileIcon(), "Profile", this::showProfile)
        );
    }
}
