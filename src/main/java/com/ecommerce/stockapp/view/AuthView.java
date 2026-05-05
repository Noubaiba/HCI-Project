package com.ecommerce.stockapp.view;

import com.ecommerce.stockapp.controller.AuthController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class AuthView {
    private final AuthController controller;
    private VBox formPanel;
    private StackPane mainContainer; // Nécessaire pour superposer les popups

    public AuthView(AuthController controller) {
        this.controller = controller;
    }

    public Parent render() {
        mainContainer = new StackPane(); // Conteneur racine pour gérer les couches
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("stockify-auth-root");

        HBox shell = new HBox();
        shell.getStyleClass().add("stockify-auth-shell");
        shell.setMaxWidth(1120);
        shell.setMaxHeight(650);

        StackPane formSide = formSide();
        StackPane brandSide = brandSide();
        HBox.setHgrow(formSide, Priority.ALWAYS);
        HBox.setHgrow(brandSide, Priority.ALWAYS);
        shell.getChildren().addAll(formSide, brandSide);

        StackPane center = new StackPane(shell);
        center.setPadding(new Insets(42));
        root.setCenter(center);
        
        mainContainer.getChildren().add(root);
        return mainContainer;
    }

    // --- SYSTÈME DE POPUP DESIGNÉE ---
    private void showPopup(String title, String message, boolean isError) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 30;");
        
        VBox dialog = new VBox(20);
        dialog.setMaxSize(380, 220);
        dialog.setAlignment(Pos.CENTER);
        dialog.setPadding(new Insets(30));
        dialog.getStyleClass().add("stockify-popup-card");
        dialog.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 8);");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + (isError ? "#e74c3c;" : "#2ecc71;"));

        Label lblMsg = new Label(message);
        lblMsg.setWrapText(true);
        lblMsg.setAlignment(Pos.CENTER);
        lblMsg.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

        Button btnClose = new Button("Ok");
        btnClose.getStyleClass().add("stockify-primary-button");
        btnClose.setPrefWidth(120);
        btnClose.setOnAction(e -> mainContainer.getChildren().remove(overlay));

        dialog.getChildren().addAll(lblTitle, lblMsg, btnClose);
        overlay.getChildren().add(dialog);

        // Animation d'entrée
        ScaleTransition st = new ScaleTransition(Duration.millis(200), dialog);
        st.setFromX(0.7); st.setFromY(0.7);
        st.setToX(1); st.setToY(1);
        
        mainContainer.getChildren().add(overlay);
        st.play();
    }

    private void showRegister() {
        formPanel.getChildren().clear();
        
        Label title = new Label("Create your account");
        title.getStyleClass().add("stockify-form-title");

        TextField name = stockifyField("Enter your name.");
        TextField email = stockifyField("Enter your mail.");
        PasswordField password = stockifyPassword("Enter your password.");
        
        CheckBox terms = new CheckBox("By Signing Up, I agree with Terms & Conditions");
        terms.getStyleClass().add("stockify-check");

        Button signUp = Ui.primary("Sign Up");
        signUp.getStyleClass().add("stockify-primary-button");
        
        signUp.setOnAction(e -> {
            // 1. Validations locales (champs vides, format)
            if (name.getText().trim().isEmpty()) {
                showPopup("Name Required", "Please enter your full name to personalize your experience.", true);
            } 
            else if (email.getText().trim().isEmpty()) {
                showPopup("Email Required", "An email address is needed to secure your account.", true);
            } 
            else if (!email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showPopup("Invalid Email", "Please provide a valid email address (e.g., name@example.com).", true);
            } 
            else if (password.getText().isEmpty()) {
                showPopup("Password Required", "For security reasons, you must create a password.", true);
            } 
            else if (password.getText().length() < 8) {
                showPopup("Weak Password", "Your password must be at least 8 characters long to ensure safety.", true);
            } 
            else if (!terms.isSelected()) {
                showPopup("Terms of Service", "Please review and accept the Terms & Conditions to proceed.", true);
            } 
            else {
                // 2. Appel au contrôleur et vérification du succès
                // IMPORTANT : Votre contrôleur doit retourner un boolean (true = succès, false = email existe déjà)
                boolean isRegistered = controller.registerCustomer(
                    name.getText().trim(), 
                    email.getText().trim(), 
                    password.getText()
                );

                if (isRegistered) {
                    // Succès : Popup verte et redirection vers le login
                    showPopup("Account Created!", "Welcome to Stockify! Your account is now active. You can sign in.", false);
                    showLogin();
                } else {
                    // Échec (Email existant) : Popup rouge et ON RESTE sur la page actuelle
                    showPopup("Registration Failed", "This email address is already registered. Please use another one or sign in.", true);
                    
                    // Optionnel : Mettre en évidence le champ email en rouge
                    email.setStyle("-fx-border-color: #ff4757; -fx-border-width: 0 0 2 0;");
                }
            }
        });

        Button signIn = Ui.secondary("Sign In");
        signIn.getStyleClass().add("stockify-outline-button");
        signIn.setOnAction(e -> showLogin());

        HBox actions = new HBox(28, signUp, signIn);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Ajout des éléments au panel
        formPanel.getChildren().setAll(
                title, spacer(42),
                fieldBlock("Name", name),
                fieldBlock("E-mail Address", email),
                fieldBlock("Password", password),
                terms, spacer(28),
                actions, spacer(16)
        );
    }
    
  
    private void showLogin() {
        formPanel.getChildren().clear();

        Label title = new Label("Sign in to your account");
        title.getStyleClass().add("stockify-form-title");

        TextField email = stockifyField("Enter your mail.");
        PasswordField password = stockifyPassword("Enter your password.");

        Button signIn = Ui.primary("Sign In");
        signIn.getStyleClass().add("stockify-primary-button");
        
        signIn.setOnAction(e -> {
            String emailTxt = email.getText().trim();
            String passTxt = password.getText();

            // 1. Vérification locale simple
            if (emailTxt.isEmpty() || passTxt.isEmpty()) {
                showPopup("Accès Refusé", "Veuillez remplir tous les champs de connexion.", true);
            } else {
                // 2. Appel au contrôleur
                // Si le login réussit, le contrôleur change de vue (Dashboard)
                // Si le login échoue, il renvoie false
                boolean isAuthenticated = controller.login(emailTxt, passTxt);

                if (!isAuthenticated) {
                    // ÉCHEC : On reste sur la page et on affiche la popup personnalisée
                    showPopup("Erreur d'authentification", "Email ou mot de passe incorrect.", true);
                    
                    // On efface le mot de passe par sécurité
                    password.clear();
                }
                // Note : Si isAuthenticated est vrai, le contrôleur a déjà fait le switch de vue
            }
        });

        Button signUp = Ui.secondary("Sign Up");
        signUp.getStyleClass().add("stockify-outline-button");
        signUp.setOnAction(e -> showRegister());

        HBox actions = new HBox(28, signIn, signUp);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Construction du formulaire
        formPanel.getChildren().setAll(
                title, spacer(58),
                fieldBlock("E-mail Address", email),
                fieldBlock("Password", password),
                spacer(34),
                actions, spacer(16)
        );
    }

    // --- MÉTHODES UTILITAIRES CONSERVÉES ---
    
    private StackPane formSide() {
        StackPane side = new StackPane();
        side.getStyleClass().add("stockify-blue-form-side");
        side.setMinWidth(590);

        Pane background = new Pane();
        background.setMouseTransparent(true);
        Circle glowOne = new Circle(360);
        glowOne.getStyleClass().add("stockify-blue-glow-a");
        glowOne.setCenterX(130); glowOne.setCenterY(140);
        Circle glowTwo = new Circle(520);
        glowTwo.getStyleClass().add("stockify-blue-glow-b");
        glowTwo.setCenterX(520); glowTwo.setCenterY(520);
        background.getChildren().addAll(glowOne, glowTwo);

        Pane wave = new Pane();
        wave.setMouseTransparent(true);
        wave.getStyleClass().add("stockify-center-wave");
        double[] yPositions = {-30, 70, 170, 270, 370, 470, 570, 670};
        for (double y : yPositions) {
            Circle circle = new Circle(72);
            circle.getStyleClass().add("stockify-wave-white");
            circle.setCenterX(610); circle.setCenterY(y);
            wave.getChildren().add(circle);
        }

        formPanel = new VBox();
        formPanel.getStyleClass().add("stockify-form-panel");
        formPanel.setAlignment(Pos.CENTER_LEFT);
        formPanel.setMaxWidth(430);
        StackPane.setAlignment(formPanel, Pos.CENTER_LEFT);
        StackPane.setMargin(formPanel, new Insets(0, 0, 0, 88));
        
        showLogin(); // Initialisation

        side.getChildren().addAll(background, wave, formPanel);
        return side;
    }

    private StackPane brandSide() {
        StackPane side = new StackPane();
        side.getStyleClass().add("stockify-white-brand-side");
        side.setMinWidth(470);

        VBox content = new VBox(26);
        content.setAlignment(Pos.CENTER);
        
        // 1. Récupération de la ressource de manière sécurisée
        java.net.URL logoUrl = getClass().getResource("/images/Stockify.png");

        if (logoUrl != null) {
            try {
                // Si l'URL existe, on crée l'image
                ImageView logo = new ImageView(new Image(logoUrl.toExternalForm()));
                logo.setFitWidth(255);
                logo.setFitHeight(255);
                logo.setPreserveRatio(true);
                
                // Animation optionnelle pour le logo (FadeIn)
                FadeTransition ft = new FadeTransition(Duration.millis(800), logo);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();

                content.getChildren().add(logo);
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
                addPlaceholderLogo(content);
            }
        } else {
            // 2. Si le fichier n'est pas trouvé (null), on affiche un texte de secours
            System.err.println("ALERTE : Fichier /images/Stockify.png introuvable dans les ressources.");
            addPlaceholderLogo(content);
        }

        Label slogan = new Label("Everything you need for a smooth shopping experience");
        slogan.getStyleClass().add("stockify-brand-slogan");
        slogan.setWrapText(true);
        slogan.setMaxWidth(390);
        slogan.setAlignment(Pos.CENTER);
        
        content.getChildren().add(slogan);
        side.getChildren().add(content);
        
        return side;
    }

    /**
     * Affiche un texte stylisé si le logo PNG est introuvable
     */
    private void addPlaceholderLogo(VBox container) {
        Label logoText = new Label("STOCKIFY");
        logoText.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-letter-spacing: 2px;");
        container.getChildren().add(logoText);
    }

    private VBox fieldBlock(String label, javafx.scene.Node field) {
        Label text = new Label(label);
        text.getStyleClass().add("stockify-field-label");
        VBox box = new VBox(8, text, field);
        box.getStyleClass().add("stockify-field-block");
        return box;
    }

    private TextField stockifyField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("stockify-line-input");
        return field;
    }

    private PasswordField stockifyPassword(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("stockify-line-input");
        return field;
    }

    private Pane spacer(double height) {
        Pane pane = new Pane();
        pane.setMinHeight(height);
        pane.setPrefHeight(height);
        return pane;
    }
}