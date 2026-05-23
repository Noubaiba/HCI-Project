package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.*;
import com.ecommerce.stockapp.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.ecommerce.stockapp.model.OrderItem;

public class OrderService {
    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final StockMovementDao movements;
    private final NotificationDao notifications;
    private final ActivityLogDao logs;

    public OrderService(OrderDao orderDao, CartDao cartDao, ProductDao productDao, StockMovementDao movements, NotificationDao notifications, ActivityLogDao logs) {
        this.orderDao = orderDao;
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.movements = movements;
        this.notifications = notifications;
        this.logs = logs;
    }

    public List<Order> allOrders() { return orderDao.findAll(); }
    public List<Order> userOrders(int userId) { return orderDao.findByUser(userId); }

    public void placeOrder(int userId) {
        placeOrder(userId, OrderStatus.PAID);
    }

    public void placeOrder(int userId, OrderStatus status) {
        List<CartItem> items = cartDao.findByUser(userId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        BigDecimal total = items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        try (Connection connection = orderDao.getDatabase().getConnection()) {
            connection.setAutoCommit(false);
            try {
                int orderId = orderDao.createOrder(connection, userId, total, status);
                for (CartItem item : items) {
                    Product product = productDao.findById(item.getProduct().getId()).orElseThrow();
                    if (product.getQuantity() < item.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for " + product.getName());
                    }
                    orderDao.createItem(connection, orderId, item);
                    productDao.updateQuantity(connection, product.getId(), product.getQuantity() - item.getQuantity());
                    movements.create(connection, product.getId(), StockMovementType.OUT, item.getQuantity());
                }
                cartDao.clear(userId, connection);
                connection.commit();
                notifications.create(userId, "Order #" + orderId + " was placed successfully.");
                logs.log(userId, "PLACE_ORDER", "Order #" + orderId + " total " + total);
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new DaoException("Unable to place order", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to place order", e);
        }
    }

    public void updateOrderStatus(int userId, Order order, OrderStatus status) {
        orderDao.updateStatus(order.getId(), status);
        logs.log(userId, "UPDATE_ORDER_STATUS", "Order #" + order.getId() + " -> " + status);
    }
    public List<OrderItem> getOrderItems(int orderId) {
        // Logique pour récupérer les items (produit, quantité, prix) depuis la table order_item
        return orderDao.findItemsByOrderId(orderId);
    }
    
}
