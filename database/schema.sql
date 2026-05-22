DROP DATABASE IF EXISTS product_stock_management;
CREATE DATABASE product_stock_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE product_stock_management;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password VARCHAR(255),
    role ENUM('ADMIN', 'CUSTOMER', 'STOCK_MANAGER') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    activation_token VARCHAR(120) UNIQUE,
    phone VARCHAR(40),
    delivery_address VARCHAR(255),
    profile_picture VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    category_id INT,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    UNIQUE KEY uq_cart_user_product (user_id, product_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE stock_movements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES products(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    status ENUM('UNREAD', 'READ') NOT NULL DEFAULT 'UNREAD',
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE system_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,
    action VARCHAR(80) NOT NULL,
    details VARCHAR(600),
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

INSERT INTO users(name, email, password, role, status, activation_token, phone, delivery_address, profile_picture) VALUES
('System Administrator', 'admin@shop.com', 'pbkdf2$120000$jAwWk81egd/Uewocrh7bQA==$ElQdobFhfymhsmU7eaxWsw/9UDL+GwsQDs0LYWZdKtk=', 'ADMIN', 'ACTIVE', NULL, '+212 600 000 001', 'Stockify HQ, Casablanca, Morocco', NULL),
('Demo Customer', 'customer@shop.com', 'pbkdf2$120000$5DHyXMouecKoWcq6aui1lg==$iP+KxCdjRj2I3l205YjvLsYQjkeRT5QAto124g26cSs=', 'CUSTOMER', 'ACTIVE', NULL, '+212 600 000 002', '12 Avenue Hassan II, Casablanca, Morocco', NULL);

INSERT INTO categories(name) VALUES
('Electronics'),
('Office'),
('Home'),
('Fashion'),
('Sports');

INSERT INTO products(name, description, price, quantity, category_id) VALUES
('Wireless Keyboard', 'Slim rechargeable keyboard with quiet keys.', 49.99, 22, 1),
('USB-C Dock', 'Nine-port aluminum docking station for modern laptops.', 119.00, 7, 1),
('Ergonomic Chair', 'Adjustable office chair with lumbar support.', 249.90, 4, 2),
('Desk Lamp', 'LED lamp with dimmer and warm/cool modes.', 34.50, 18, 3),
('Running Shoes', 'Lightweight daily trainer with breathable upper.', 89.99, 11, 4),
('Yoga Mat', 'Non-slip mat for home fitness and studio sessions.', 29.99, 0, 5);

INSERT INTO stock_movements(product_id, type, quantity, date) VALUES
(1, 'IN', 22, NOW()),
(2, 'IN', 7, NOW()),
(3, 'IN', 4, NOW()),
(6, 'ADJUSTMENT', 0, NOW());

INSERT INTO notifications(user_id, message, status, date) VALUES
(2, 'Welcome to your customer dashboard.', 'UNREAD', NOW());

INSERT INTO system_logs(user_id, action, details, date) VALUES
(1, 'SEED_DATABASE', 'Initial demo data installed', NOW());
