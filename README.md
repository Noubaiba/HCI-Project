# Product & Stock Management System

JavaFX desktop application for role-based e-commerce inventory management.

## Stack

- Java 17
- JavaFX
- MySQL
- JDBC
- Jakarta Mail / JavaMail SMTP
- Maven / IntelliJ IDEA
- MVC architecture

## Run

1. Create the database:

   ```sql
   SOURCE database/schema.sql;
   ```

2. Configure database and SMTP through environment variables or JVM properties:

   ```text
   DB_URL=jdbc:mysql://localhost:3306/product_stock_management
   DB_USER=root
   DB_PASSWORD=your_password
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_USER=your_gmail@gmail.com
   SMTP_PASSWORD=your_app_password
   SMTP_FROM=your_gmail@gmail.com
   APP_ACTIVATION_BASE_URL=http://localhost:8080/activate
   ```

3. Run from IntelliJ using Maven:

   ```bash
   mvn javafx:run
   ```

## Demo Accounts

- Admin: `admin@shop.com` / `Admin@123`
- Customer: `customer@shop.com` / `Customer@123`
- Stock Manager: create one from the Admin dashboard. The app sends a real activation email when SMTP is configured.

Stock managers activate by clicking the email link: `http://localhost:8080/activate?token=...`. The desktop app starts a local activation listener on port `8080` and opens the password setup dialog automatically.
