package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.dao.UserDao;
import com.ecommerce.stockapp.model.Role;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.model.UserStatus;
import com.ecommerce.stockapp.util.PasswordUtil;
import com.ecommerce.stockapp.util.ValidationUtil;

public class AuthService {
    private final UserDao userDao;
    private final ActivityLogDao logs;

    public AuthService(UserDao userDao, ActivityLogDao logs) {
        this.userDao = userDao;
        this.logs = logs;
    }

    public User login(String email, String password) {
        ValidationUtil.requireEmail(email);
        ValidationUtil.requireText(password, "Password");
        User user = userDao.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("This account is not active.");
        }
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        logs.log(user.getId(), "LOGIN", "Successful login");
        return user;
    }

    public void registerCustomer(String name, String email, String password) {
        ValidationUtil.requireText(name, "Name");
        ValidationUtil.requireEmail(email);
        ValidationUtil.requirePassword(password);
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        User user = new User(0, name, email, PasswordUtil.hash(password), Role.CUSTOMER, UserStatus.ACTIVE, null);
        int id = userDao.create(user);
        logs.log(id, "REGISTER", "Customer account created");
    }

    public void activateStockManager(String token, String password) {
        ValidationUtil.requireText(token, "Activation token");
        ValidationUtil.requirePassword(password);
        User user = userDao.findByActivationToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid activation token."));
        if (user.getRole() != Role.STOCK_MANAGER) {
            throw new IllegalArgumentException("This token does not belong to a stock manager account.");
        }
        userDao.activate(token, PasswordUtil.hash(password));
        logs.log(user.getId(), "ACTIVATE", "Stock manager activated account");
    }
}
