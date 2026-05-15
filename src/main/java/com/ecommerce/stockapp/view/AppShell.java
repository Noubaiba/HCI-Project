package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import com.ecommerce.stockapp.util.IconFactory;
import com.ecommerce.stockapp.view.NavItem;
import javafx.scene.Node;

public class AppShell {
	public record NavItem(Node icon, String label, Runnable action) {}

    private final User user;
    private final List<NavItem> navItems;
    private final Runnable logout;
    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(20);
    private final VBox navBox = new VBox(10);
    private final List<javafx.scene.Node> collapsibleNodes = new ArrayList<>();
    private VBox sidebar;
    private Label logoText;
    private Label sectionTitle;
    private HBox profile;
    private Button logoutButton;
    private Button activeButton;
    private boolean collapsed;
    private String currentTitle = "Dashboard";
    private String searchText = "";
    private Consumer<String> searchHandler;

    private Label cartBadge;
    public void updateCartCount(int count) {
        if (cartBadge != null) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }

    public AppShell(User user, List<NavItem> navItems, Runnable logout) {
        this.user = user;
        this.navItems = navItems;
        this.logout = logout;
        build();
    }

    public Parent render() {
        return root;
    }

    public void setContent(String title, javafx.scene.Node node) {
        currentTitle = title;
        content.getChildren().setAll(header(), node);
    }

    public void setSearchHandler(Consumer<String> searchHandler) {
        this.searchHandler = searchHandler;
        this.searchText = "";
    }

    private void build() {
        root.getStyleClass().add("shell-root");
        root.setLeft(sidebar());
        content.getStyleClass().add("shell-content");
        content.setPadding(new Insets(26, 30, 30, 30));
        root.setCenter(content);
    }

    private VBox sidebar() {
        sidebar = new VBox(26);
        sidebar.getStyleClass().add("shell-sidebar");
        sidebar.setPadding(new Insets(28, 24, 24, 24));

        Button menuButton = new Button();
        menuButton.setGraphic(hamburgerIcon());
        menuButton.getStyleClass().add("shell-menu-button");
        menuButton.setOnAction(e -> toggleSidebar());
        HBox menuPill = new HBox(menuButton);
        menuPill.getStyleClass().add("shell-menu-pill");
        menuPill.setAlignment(Pos.CENTER);

        sectionTitle = sectionLabel("NAVIGATION");
        VBox logoBlock = new VBox(10, logo(), sectionTitle);
        logoBlock.setAlignment(Pos.CENTER_LEFT);

        
        


        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        profile = profileBlock(); // Contient maintenant l'avatar, le texte et le logout

        // On ajoute seulement le profil à la fin
        sidebar.getChildren().addAll(menuPill, logoBlock, navBox, spacer, profile);
        return sidebar;
    }

    private HBox logo() {
        ImageView image = new ImageView(new Image(getClass().getResource("/images/Stockify.png").toExternalForm()));
        image.setFitWidth(56);
        image.setFitHeight(56);
        image.setPreserveRatio(true);
        logoText = new Label("Stockify");
        logoText.getStyleClass().add("shell-logo-text");
        collapsibleNodes.add(logoText);
        HBox logo = new HBox(18, image, logoText);
        logo.setPadding(new Insets(0, 0, 10, 0));
        logo.setAlignment(Pos.CENTER);
        return logo;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("shell-section-label");
        return label;
    }

    public void setNavItems(List<NavItem> items) {
        this.navBox.getChildren().clear(); // On vide l'ancienne liste
        this.collapsibleNodes.removeIf(node -> node instanceof Label && node.getStyleClass().contains("shell-nav-label")); // Nettoyage
        this.activeButton = null;

        for (NavItem item : items) {
            Button button = navButton(item);
            navBox.getChildren().add(button);

            // Optionnel : mettre le premier en "active" par défaut
            if (activeButton == null) {
                activeButton = button;
                button.getStyleClass().add("active");
            }
        }
    }

    private Button navButton(NavItem item) {

        Label label = new Label(item.label());
        label.getStyleClass().add("shell-nav-label");

        collapsibleNodes.add(label); // 👈 important

//        HBox content = new HBox(50, item.icon(), label);
        HBox content = new HBox(10, item.icon(), label);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMaxWidth(Double.MAX_VALUE);
        
        

        Button button = new Button();
        button.setGraphic(content);
        button.setUserData(content);

        button.getStyleClass().add("shell-nav-button");

        button.getProperties().put("navLabel", item.label());
        button.setMaxWidth(Double.MAX_VALUE);

        button.setOnAction(e -> {
            if (activeButton != null) {
                activeButton.getStyleClass().remove("active");
            }

            activeButton = button;
            button.getStyleClass().add("active");

            item.action().run();
        });

        return button;
    }

    private HBox header() {
        Label title = new Label(currentTitle);
        title.getStyleClass().add("shell-page-title");
        title.setMinWidth(200);

        TextField search = new TextField();
        search.setPromptText("Rechercher...");
        search.setText(searchText);
        search.getStyleClass().add("shell-search");
        search.setPrefWidth(300);
        search.setDisable(searchHandler == null);
        search.setOnAction(e -> triggerSearch(search.getText()));
        search.textProperty().addListener((obs, oldValue, newValue) -> {
            searchText = newValue == null ? "" : newValue;
            if (searchHandler != null) {
                triggerSearch(searchText);
            }
        });

        // --- LOGIQUE DU PANIER (CART) ---
        ImageView cartIcon = new ImageView(new Image(getClass().getResource("/images/icons/cart.png").toExternalForm()));
        cartIcon.setFitWidth(28);
        cartIcon.setFitHeight(28);

        // Le badge (le petit cercle rouge avec le nombre)
        cartBadge = new Label("0");
        cartBadge.getStyleClass().add("cart-badge");
        cartBadge.setVisible(false); // Caché par défaut si 0

        // Positionnement du badge en haut à droite de l'icône
        StackPane cartContainer = new StackPane(cartIcon, cartBadge);
        StackPane.setAlignment(cartBadge, Pos.TOP_RIGHT);
        cartBadge.setTranslateX(8);
        cartBadge.setTranslateY(-8);
        cartContainer.setCursor(javafx.scene.Cursor.HAND);
        cartContainer.setOnMouseClicked(e -> triggerNav("Cart"));
        // --------------------------------

        StackPane avatar = Ui.avatar(user.getName());
        avatar.getStyleClass().add("shell-avatar");

        Label name = new Label(user.getName());
        name.getStyleClass().add("shell-user-name");

        Label role = new Label(user.getRole().name().replace('_', ' '));
        role.getStyleClass().add("shell-user-role");

        VBox identity = new VBox(1, name, role);
        identity.setAlignment(Pos.CENTER_LEFT);
        identity.setCursor(javafx.scene.Cursor.HAND);
        avatar.setCursor(javafx.scene.Cursor.HAND);

        HBox profileShortcut = new HBox(10, avatar, identity);
        profileShortcut.setAlignment(Pos.CENTER_LEFT);
        profileShortcut.setCursor(javafx.scene.Cursor.HAND);
        profileShortcut.setOnMouseClicked(e -> triggerNav("Profile"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // On assemble : [Titre] [Spacer] [Search] [Espace] [Cart] [Espace] [Avatar/Identity]
        HBox header = new HBox(25, title, spacer, search, cartContainer, profileShortcut);
        header.getStyleClass().add("shell-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 0));

        return header;
    }

    private void triggerSearch(String value) {
        searchText = value == null ? "" : value;
        if (searchHandler != null) {
            searchHandler.accept(searchText);
        }
    }

    private void triggerNav(String label) {
        for (javafx.scene.Node node : navBox.getChildren()) {
            if (node instanceof Button button) {
                Object buttonLabel = button.getProperties().get("navLabel");
                if (Objects.equals(buttonLabel, label)) {
                    button.fire();
                    return;
                }
            }
        }
    }

    private HBox profileBlock() {
        StackPane avatar = Ui.avatar(user.getName());
        // L'avatar doit rester visible, donc on ne l'ajoute PAS à collapsibleNodes

        Label name = new Label(user.getName());
        name.getStyleClass().add("shell-profile-name");

        Label email = new Label(user.getEmail());
        email.getStyleClass().add("shell-profile-email");

        VBox text = new VBox(2, name, email);

        // On réutilise ton bouton logoutButton ici
        logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("shell-logout-button");
        logoutButton.setOnAction(e -> logout.run());

        // Ajout des textes et du bouton à la liste de réduction
        collapsibleNodes.add(text);
        collapsibleNodes.add(logoutButton);

        // Structure : [Avatar] [VBox Texte] [Region Spacer] [Logout]
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox profileContainer = new HBox(12, avatar, text, spacer, logoutButton);
        profileContainer.getStyleClass().add("shell-profile");
        profileContainer.setAlignment(Pos.CENTER_LEFT);

        return profileContainer;
    }

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

    private void toggleSidebar() {
        collapsed = !collapsed;

        double newWidth = collapsed ? 80 : 310;
        sidebar.setMinWidth(newWidth);
        sidebar.setMaxWidth(newWidth);
        sidebar.setPrefWidth(newWidth);

        if (collapsed) {
            sidebar.setPadding(new Insets(28, 0, 24, 0));
            navBox.setAlignment(Pos.TOP_CENTER);
            profile.setAlignment(Pos.CENTER); // Centre l'avatar quand réduit
            navBox.getChildren().forEach(node -> {
                if (node instanceof Button btn) {

                    HBox content = (HBox) btn.getUserData();

                    if (collapsed) {
                        btn.setAlignment(Pos.CENTER);
                        content.setAlignment(Pos.CENTER); // 🔥 centre l’icône
                    } else {
                        btn.setAlignment(Pos.CENTER_LEFT);
                        content.setAlignment(Pos.CENTER_LEFT);
                    }
                }
            });
        } else {
            sidebar.setPadding(new Insets(28, 24, 24, 24));
            navBox.setAlignment(Pos.TOP_LEFT);
            profile.setAlignment(Pos.CENTER_LEFT);
            navBox.getChildren().forEach(node -> {
                if (node instanceof Button btn) {

                    HBox content = (HBox) btn.getUserData();

                    if (collapsed) {
                        btn.setAlignment(Pos.CENTER);
                        content.setAlignment(Pos.CENTER);
                    } else {
                        btn.setAlignment(Pos.CENTER_LEFT);
                        content.setAlignment(Pos.CENTER_LEFT); // 🔥 ça manquait
                    }
                }
            });
        }

        // Masquer les labels de section et logo
        sectionTitle.setVisible(!collapsed);
        sectionTitle.setManaged(!collapsed);
        
        navBox.setFillWidth(true);

        // Boucle sur les nœuds collapsibles (qui inclut maintenant le texte et le bouton logout)
        for (javafx.scene.Node node : collapsibleNodes) {
            node.setVisible(!collapsed);
            node.setManaged(!collapsed);
        }

        // 4. Masquer les éléments (Managed false retire l'espace occupé)
        sectionTitle.setVisible(!collapsed);
        sectionTitle.setManaged(!collapsed);

        profile.setVisible(!collapsed);
        profile.setManaged(!collapsed);

        logoutButton.setVisible(!collapsed);
        logoutButton.setManaged(!collapsed);

        for (javafx.scene.Node node : collapsibleNodes) {
            node.setVisible(!collapsed);
            node.setManaged(!collapsed);
        }
    }
}
