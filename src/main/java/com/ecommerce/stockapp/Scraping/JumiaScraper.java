package com.ecommerce.stockapp.Scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JumiaScraper {

    // Paramètres de connexion
    private static final String DB_URL = "jdbc:mysql://localhost:3306/product_stock_management?useUnicode=true&characterEncoding=utf8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver(options);
        Random random = new Random();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("✅ Connexion base de données OK.");

            driver.get("https://www.jumia.ma/");
            Thread.sleep(5000); // Temps de chargement du menu JS

            // 1. Récupération des catégories
            List<CategoryData> categoriesToScrape = new ArrayList<>();
            List<WebElement> catElements = driver.findElements(By.cssSelector("a.itm[href*='/'], .flyout a, a[href*='/c-']"));

            for (WebElement el : catElements) {
                try {
                    String name = el.getText().trim();
                    String url = el.getAttribute("href");
                    if (!name.isEmpty() && url != null && url.contains("jumia.ma") && !url.equals("https://www.jumia.ma/")) {
                        if (categoriesToScrape.stream().noneMatch(c -> c.url.equals(url))) {
                            categoriesToScrape.add(new CategoryData(name, url));
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Fallback si menu vide
            if (categoriesToScrape.isEmpty()) {
                categoriesToScrape.add(new CategoryData("Téléphones & Tablettes", "https://www.jumia.ma/telephones-tablettes/"));
                categoriesToScrape.add(new CategoryData("Informatique", "https://www.jumia.ma/informatique/"));
            }

            System.out.println("📂 " + categoriesToScrape.size() + " catégories trouvées.");

            // 2. Scraping par catégorie
            for (CategoryData cat : categoriesToScrape) {
                // ÉTAPE CRUCIALE : On récupère l'ID de la catégorie dans la DB
                int categoryId = getOrInsertCategory(conn, cat.name);

                if (categoryId == 0) {
                    System.err.println("❌ Impossible de récupérer l'ID pour: " + cat.name);
                    continue;
                }

                System.out.println("\n🚀 Scraping catégorie: " + cat.name + " (ID DB: " + categoryId + ")");

                for (int page = 1; page <= 2; page++) {
                    String pageUrl = cat.url + (cat.url.contains("?") ? "&page=" : "?page=") + page;
                    driver.get(pageUrl);

                    // Scroll progressif
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("window.scrollTo(0, 1000);");
                    Thread.sleep(2000);
                    js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    Thread.sleep(2000);

                    List<WebElement> products = driver.findElements(By.cssSelector("article.prd"));
                    if (products.isEmpty()) break;

                    for (WebElement p : products) {
                        try {
                            String pName = p.findElement(By.cssSelector("h3.name")).getText();
                            String pPriceRaw = p.findElement(By.cssSelector("div.prc")).getText();

                            WebElement imgTag = p.findElement(By.cssSelector("img.img"));
                            String pImg = imgTag.getAttribute("data-src");
                            if (pImg == null) pImg = imgTag.getAttribute("src");

                            double price = formatPrice(pPriceRaw);
                            int qty = random.nextInt(100) + 1; // Quantité aléatoire
                            String description = "Article de la collection " + cat.name;

                            // INSERTION DU PRODUIT AVEC LE BON category_id
                            insertProduct(conn, pName, description, pImg, price, qty, categoryId);
                        } catch (Exception ignored) {}
                    }
                }
            }
            System.out.println("\n🎯 Scraping terminé avec succès !");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    // Méthode pour garantir la récupération de l'ID de la catégorie
    private static int getOrInsertCategory(Connection conn, String name) throws SQLException {
        // 1. Vérifier si elle existe
        String select = "SELECT id FROM categories WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        // 2. Sinon l'insérer et retourner l'ID généré
        String insert = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    private static void insertProduct(Connection conn, String name, String desc, String img, double price, int qty, int catId) throws SQLException {
        String sql = "INSERT INTO products (name, description, image_url, price, quantity, category_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, img);
            ps.setDouble(4, price);
            ps.setInt(5, qty);
            ps.setInt(6, catId);
            ps.executeUpdate();
        }
    }

    private static double formatPrice(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("[^0-9]", "");
        try { return Double.parseDouble(clean); } catch (Exception e) { return 0.0; }
    }

    static class CategoryData {
        String name, url;
        CategoryData(String n, String u) { this.name = n; this.url = u; }
    }
}