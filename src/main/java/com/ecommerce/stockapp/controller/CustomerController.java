package com.ecommerce.stockapp.controller;

import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.Product;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.service.CartService;
import com.ecommerce.stockapp.service.OrderService;
import com.ecommerce.stockapp.service.ProductService;
import com.ecommerce.stockapp.view.AppShell;

import java.math.BigDecimal;
import java.util.List;

public class CustomerController {
    private final User currentUser;
    private final AuthController auth;
    private final ProductService products;
    private final CartService cart;
    private final OrderService orders;
    private AppShell appShell;

    public CustomerController(User currentUser, AuthController auth, ProductService products, 
                              CartService cart, OrderService orders, AppShell appShell) {
        this.currentUser = currentUser;
        this.auth = auth;
        this.products = products;
        this.cart = cart;
        this.orders = orders;
        this.appShell = appShell;
    }

    public User currentUser() { return currentUser; }
    
    public void logout() { auth.logout(); }

    public List<Product> products(String search) { 
        return products.products(search); 
    }

    public List<CartItem> cart() { 
        return cart.items(currentUser.getId()); 
    }

    public BigDecimal cartTotal() { 
        return cart.total(currentUser.getId()); 
    }

    public void addToCart(Product product, int quantity) {
        cart.add(currentUser.getId(), product, quantity);
        if (appShell != null) {
            int count = cart.items(currentUser.getId()).size();
            appShell.updateCartCount(count);
        }
    }

    public void updateCart(int cartId, int quantity) { 
        cart.update(cartId, quantity); 
    }

    public void removeCart(int cartId) { 
        cart.remove(cartId); 
        if (appShell != null) {
            appShell.updateCartCount(cart().size());
        }
    }

    public void placeOrder() { 
        orders.placeOrder(currentUser.getId()); 
        if (appShell != null) appShell.updateCartCount(0);
    }

    public void setAppShell(AppShell appShell) {
        this.appShell = appShell;
    }

    public List<Order> orders() {
        // Vérifie bien que l'objet 'orders' (le service) est bien initialisé dans le constructeur
        return orders.userOrders(currentUser.getId());
    }
}