package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.CartDao;
import com.ecommerce.stockapp.dao.ProductDao;
import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public void add(int userId, Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        Product fresh = productDao.findById(product.getId()).orElseThrow(() -> new IllegalArgumentException("Product no longer exists."));
        if (fresh.getQuantity() < quantity) {
            throw new IllegalArgumentException("Only " + fresh.getQuantity() + " units are available.");
        }
        cartDao.addOrUpdate(userId, product.getId(), quantity);
    }

    public List<CartItem> items(int userId) { return cartDao.findByUser(userId); }
    public void update(int cartId, int quantity) { cartDao.updateQuantity(cartId, quantity); }
    public void remove(int cartId) { cartDao.remove(cartId); }

    public BigDecimal total(int userId) {
        return items(userId).stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
