package com.ecommerce.stockapp.Scraping;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SheinFullScraper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/product_stock_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        // --- LISTE COMPLÈTE DES CATÉGORIES (Basée sur tes images) ---
        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("Robes", "https://fr.shein.com/style/Dresses-sc-00102142.html");
        categories.put("Tops & T-shirts", "https://fr.shein.com/style/T-Shirts-sc-00102436.html");
        categories.put("Ensembles", "https://fr.shein.com/style/Two-piece-Sets-sc-00102453.html");
        categories.put("Lingerie", "https://fr.shein.com/style/Lingerie-Lounge-sc-00102482.html");
        categories.put("Pantalons", "https://fr.shein.com/style/Pants-sc-00102447.html");
        categories.put("Accessoires", "https://fr.shein.com/style/Jewelry-Accessories-sc-00103133.html");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("[DB] Connexion réussie.");

            for (Map.Entry<String, String> entry : categories.entrySet()) {
                System.out.println("\n========================================");
                System.out.println("SCRAPING CATÉGORIE : " + entry.getKey());
                System.out.println("========================================");

                driver.get(entry.getValue());
                Thread.sleep(6000); // Temps pour laisser passer les pop-ups

                // --- SCROLL PROFOND POUR TOUT CHARGER ---
                JavascriptExecutor js = (JavascriptExecutor) driver;
                for (int i = 0; i < 25; i++) { // 25 scrolls pour descendre très bas
                    js.executeScript("window.scrollBy(0, 1000)");
                    Thread.sleep(1800); // On laisse 1.8s pour que les images s'affichent (Lazy loading)
                }

                int catId = getOrCreateCategory(conn, entry.getKey());
                Document doc = Jsoup.parse(driver.getPageSource());
                Elements items = doc.select(".S-product-item, .product-card, .g-goods-item");

                System.out.println("[!] Éléments trouvés : " + items.size());

                int added = 0;
                String sql = "INSERT INTO products (name, description, price, quantity, category_id, image_url) VALUES (?, ?, ?, ?, ?, ?)";

                for (Element item : items) {
                    try {
                        String name = item.select("a[class*='name'], .goods-name").text().trim();
                        if (name.isEmpty()) name = item.select("img").attr("alt");

                        // Nettoyage et sécurité taille
                        name = name.replaceAll("(?i)nouveau|économiser.*|\\d+%", "").trim();
                        if (name.length() > 250) name = name.substring(0, 247) + "...";

                        String priceStr = extractPrice(item.text());

                        // Capture de l'image (obligatoire)
                        Element imgTag = item.select("img").first();
                        String imageUrl = "";
                        if (imgTag != null) {
                            imageUrl = imgTag.attr("data-src").isEmpty() ? imgTag.attr("src") : imgTag.attr("data-src");
                            if (imageUrl.startsWith("//")) imageUrl = "https:" + imageUrl;
                        }

                        if (!name.isEmpty() && !priceStr.isEmpty() && !imageUrl.isEmpty()) {
                            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                                pstmt.setString(1, name);
                                pstmt.setString(2, "Importé de Shein - Collection " + entry.getKey());
                                pstmt.setDouble(3, Double.parseDouble(priceStr));
                                pstmt.setInt(4, (int) (Math.random() * 100) + 10); // Quantité aléatoire
                                pstmt.setInt(5, catId);
                                pstmt.setString(6, imageUrl);

                                pstmt.executeUpdate();
                                added++;
                            }
                        }
                    } catch (Exception e) {
                        // On continue au prochain produit en cas d'erreur sur un seul item
                    }
                }
                System.out.println(">>> SUCCÈS : " + added + " produits ajoutés pour " + entry.getKey());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            System.out.println("\n[FIN] Toutes les catégories ont été traitées.");
        }
    }

    private static String extractPrice(String text) {
        Pattern p = Pattern.compile("(\\d+[.,]\\d+)");
        Matcher m = p.matcher(text.replace(" ", ""));
        if (m.find()) return m.group(1).replace(",", ".");
        return "";
    }

    private static int getOrCreateCategory(Connection conn, String name) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM categories WHERE name = ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("id");

        PreparedStatement psIns = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        psIns.setString(1, name);
        psIns.executeUpdate();
        ResultSet keys = psIns.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }
}