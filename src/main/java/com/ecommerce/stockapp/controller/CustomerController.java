package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.Product;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.service.CartService;
import com.ecommerce.stockapp.service.OrderService;
import com.ecommerce.stockapp.service.ProductService;
import com.ecommerce.stockapp.service.UserService; // On utilise le service plutôt que le DAO direct
import com.ecommerce.stockapp.view.AppShell;
import com.ecommerce.stockapp.model.OrderItem;
import javafx.scene.Node;
import javafx.scene.control.PasswordField; // Profites-en pour ajouter celui-ci aussi

import java.math.BigDecimal;
import java.util.List;

public class CustomerController {
    private User currentUser; // 'final' retiré pour permettre la mise à jour du profil
    private final AuthController auth;
    private final ProductService products;
    private final CartService cart;
    private final OrderService orders;
    private final UserService userService; // Utilisation du Service pour respecter l'architecture
    private AppShell appShell;

    public CustomerController(User currentUser, AuthController auth, ProductService products,
                              CartService cart, OrderService orders, UserService userService,
                              AppShell appShell) {
        this.currentUser = currentUser;
        this.auth = auth;
        this.products = products;
        this.cart = cart;
        this.orders = orders;
        this.userService = userService;
        this.appShell = appShell;
    }
    // Dans CustomerController.java
    public void navigateToCustomPage(String title, Node pageContent) {
        if (appShell != null) {
            appShell.setContent(title, pageContent);
        }
    }

    /**
     * Met à jour le profil via le service et rafraîchit la session locale
     */
    public void updateProfile(User user) {
        // On passe par le service (qui lui-même appelle le DAO et log l'action)
        userService.updateProfile(user);

        // On met à jour l'utilisateur en mémoire pour l'affichage
        this.currentUser = user;
    }

    public User currentUser() { return currentUser; }

    public void logout() { auth.logout(); }

    // --- Gestion des Produits ---
    public List<Product> products(String search) {
        return products.products(search);
    }

    // --- Gestion du Panier (avec mise à jour visuelle AppShell) ---
    public List<CartItem> cart() {
        return cart.items(currentUser.getId());
    }

    public BigDecimal cartTotal() {
        return cart.total(currentUser.getId());
    }

    public void addToCart(Product product, int quantity) {
        cart.add(currentUser.getId(), product, quantity);
        updateUI();
    }

    public void removeCart(int cartId) {
        cart.remove(cartId);
        updateUI();
    }

    private void updateUI() {
        if (appShell != null) {
            appShell.updateCartCount(cart().size());
        }
    }

    // --- Gestion des Commandes ---
    public void placeOrder() {
        orders.placeOrder(currentUser.getId());
        if (appShell != null) appShell.updateCartCount(0);
    }

    public List<Order> orders() {
        return orders.userOrders(currentUser.getId());
    }

    public List<OrderItem> getOrderItems(int orderId) {
        // Cette méthode doit appeler ton OrderService pour récupérer les items
        return orders.getOrderItems(orderId);
    }
    public Product getProductById(int productId) {
        // On demande au service de trouver le produit par son ID
        return products.products("").stream()
                .filter(p -> p.getId() == productId)
                .findFirst()
                .orElse(null);
    }

    public void setAppShell(AppShell appShell) {
        this.appShell = appShell;
    }

    public void removeFromCart(CartItem item) {
        cart.remove(item.getId());
    }

    public void updateCartQuantity(CartItem item, int quantity) {
        cart.update(item.getId(), quantity);
    }
    public int changerMotDePasse(String ancienMdp, String nouveauMdp) {
        // 1. Récupérer l'utilisateur en temps réel depuis MySQL
        String emailActuel = currentUser.getEmail();
        java.util.Optional<User> userInDb = userService.users().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(emailActuel))
                .findFirst();

        if (userInDb.isEmpty()) {
            return 0; // Utilisateur introuvable
        }

        String mdpHacheBDD = userInDb.get().getPassword();

        // 2. VÉRIFICATION DU MOT DE PASSE HACHÉ
        // NOTE : Si votre projet utilise une autre classe (ex: BCrypt.checkpw ou PasswordHasher),
        // remplacez simplement "PasswordUtil.verify" par votre fonction de Login.
        boolean correct = com.ecommerce.stockapp.util.PasswordUtil.verify(ancienMdp, mdpHacheBDD);

        if (!correct) {
            System.out.println("Échec : L'ancien mot de passe ne correspond pas au hachage MySQL.");
            return -1; // Retourne l'erreur à la vue (case rouge)
        }

        // 3. HACHAGE DU NOUVEAU MOT DE PASSE AVANT ENREGISTREMENT
        String nouveauMdpHache = com.ecommerce.stockapp.util.PasswordUtil.hash(nouveauMdp);

        // 4. Application de la modification dans la table `users`
        userService.updatePassword(userInDb.get().getId(), nouveauMdpHache);

        // 5. Synchronisation de la session de l'application
        this.currentUser = userInDb.get();
        this.currentUser.setPassword(nouveauMdpHache);

        return 1; // Tout est parfait !
    }



}