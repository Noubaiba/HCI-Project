USE product_stock_management;

ALTER TABLE users
    ADD COLUMN phone VARCHAR(40) NULL AFTER activation_token,
    ADD COLUMN delivery_address VARCHAR(255) NULL AFTER phone,
    ADD COLUMN profile_picture VARCHAR(500) NULL AFTER delivery_address;

UPDATE users
SET phone = '+212 600 000 001',
    delivery_address = 'Stockify HQ, Casablanca, Morocco'
WHERE email = 'admin@shop.com'
  AND phone IS NULL;
