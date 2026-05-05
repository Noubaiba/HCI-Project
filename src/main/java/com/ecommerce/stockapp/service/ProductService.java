package com.ecommerce.stockapp.service;

import com.ecommerce.stockapp.dao.ActivityLogDao;
import com.ecommerce.stockapp.dao.CategoryDao;
import com.ecommerce.stockapp.dao.ProductDao;
import com.ecommerce.stockapp.dao.StockMovementDao;
import com.ecommerce.stockapp.model.Category;
import com.ecommerce.stockapp.model.Product;
import com.ecommerce.stockapp.model.StockMovementType;
import com.ecommerce.stockapp.util.ValidationUtil;

import java.util.List;

public class ProductService {
    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final StockMovementDao movements;
    private final ActivityLogDao logs;

    public ProductService(ProductDao productDao, CategoryDao categoryDao, StockMovementDao movements, ActivityLogDao logs) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
        this.movements = movements;
        this.logs = logs;
    }

    public List<Product> products(String search) { return productDao.findAll(search == null ? "" : search); }
    public List<Product> lowStock() { return productDao.lowStock(5); }
    public List<Category> categories() { return categoryDao.findAll(); }

    public void saveProduct(int userId, Product product) {
        ValidationUtil.requireText(product.getName(), "Product name");
        if (product.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Choose a category.");
        }
        productDao.save(product);
        logs.log(userId, "SAVE_PRODUCT", product.getName());
    }

    public void deleteProduct(int userId, Product product) {
        productDao.delete(product.getId());
        logs.log(userId, "DELETE_PRODUCT", product.getName());
    }

    public void createCategory(int userId, String name) {
        ValidationUtil.requireText(name, "Category name");
        categoryDao.create(name);
        logs.log(userId, "CREATE_CATEGORY", name);
    }

    public void addStock(int userId, Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        int next = product.getQuantity() + quantity;
        productDao.updateQuantity(product.getId(), next);
        movements.create(product.getId(), StockMovementType.IN, quantity);
        logs.log(userId, "STOCK_IN", product.getName() + " +" + quantity);
    }

    public void adjustStock(int userId, Product product, int quantity) {
        productDao.updateQuantity(product.getId(), quantity);
        movements.create(product.getId(), StockMovementType.ADJUSTMENT, quantity);
        logs.log(userId, "STOCK_ADJUSTMENT", product.getName() + " -> " + quantity);
    }
}