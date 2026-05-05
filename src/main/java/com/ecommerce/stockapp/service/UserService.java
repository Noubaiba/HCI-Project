package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.dao.UserDao;
import com.ecommerce.stockapp.model.Role;
import com.ecommerce.stockapp.model.User;
import com.ecommerce.stockapp.model.UserStatus;
import com.ecommerce.stockapp.util.TokenUtil;
import com.ecommerce.stockapp.util.ValidationUtil;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private final UserDao userDao;
    private final EmailService emailService;
    private final ActivityLogDao logs;

    public UserService(UserDao userDao, EmailService emailService, ActivityLogDao logs) {
        this.userDao = userDao;
        this.emailService = emailService;
        this.logs = logs;
    }

    public List<User> users() {
        return userDao.findAll();
    }

    /**
     * Crée un gestionnaire de stock (Action Admin)
     */
    public String createStockManager(int adminId, String name, String email) throws MessagingException {
        ValidationUtil.requireText(name, "Name");
        ValidationUtil.requireEmail(email);

        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String token = TokenUtil.activationToken();

        // Utilisation du constructeur complet :
        // ID: 0, Name, Email, Password: null, Role: STOCK_MANAGER, Status: INACTIVE, Token, Date, Phone: null, Address: null, Img: null
        User user = new User(0, name, email, null, Role.STOCK_MANAGER,
                UserStatus.INACTIVE, token, LocalDateTime.now(),
                null, null, null);

        int id = userDao.create(user);

        try {
            emailService.sendActivationEmail(email, name, token);
        } catch (MessagingException e) {
            logs.log(adminId, "ACTIVATION_EMAIL_FAILED", email + " | " + e.getMessage());
            throw e;
        }

        logs.log(adminId, "CREATE_STOCK_MANAGER", "Created inactive stock manager " + email);
        logs.log(id, "EMAIL_SENT", "Activation email sent");
        return token;
    }

    public void updateStatus(int adminId, User user, UserStatus status) {
        userDao.updateStatus(user.getId(), status);
        logs.log(adminId, "UPDATE_USER_STATUS", user.getEmail() + " -> " + status);
    }

    public void updateRole(int adminId, User user, Role role) {
        userDao.updateRole(user.getId(), role);
        logs.log(adminId, "UPDATE_USER_ROLE", user.getEmail() + " -> " + role);
    }

    public boolean existsByEmail(String email) {
        return userDao.findByEmail(email).isPresent();
    }

    /**
     * Met à jour le profil de l'utilisateur (Action Utilisateur)
     */
    public void updateProfile(User user) {
        // 1. Validation de sécurité
        ValidationUtil.requireText(user.getName(), "Nom");

        // 2. Mise à jour en base de données via le DAO
        userDao.updateProfile(user);

        // 3. Enregistrement de l'activité
        logs.log(user.getId(), "UPDATE_PROFILE", "L'utilisateur a mis à jour ses informations personnelles.");
    }
}