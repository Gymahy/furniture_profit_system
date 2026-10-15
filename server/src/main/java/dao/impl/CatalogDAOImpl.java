package dao.impl;

import dao.CatalogDAO;
import models.dto.StockCheckDTO;
import models.entities.Category;
import models.entities.Product;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CatalogDAOImpl implements CatalogDAO {
    @Override
    public List<Category> findCategories() throws Exception {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new Category(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description")));
            }
        }
        return categories;
    }

    @Override
    public Category saveCategory(Category category) throws Exception {
        if (category.isNew()) {
            String sql = "INSERT INTO categories(name, description) VALUES (?, ?)";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, category.getName());
                statement.setString(2, category.getDescription());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        category.setId(keys.getInt(1));
                    }
                }
            }
        } else {
            String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, category.getName());
                statement.setString(2, category.getDescription());
                statement.setInt(3, category.getId());
                statement.executeUpdate();
            }
        }
        return category;
    }

    @Override
    public void deleteCategory(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Нельзя удалить категорию товаров: она используется в товарах");
        }
    }

    @Override
    public boolean isCategoryUsed(Integer id) throws Exception {
        return exists("SELECT 1 FROM products WHERE category_id = ? LIMIT 1", id);
    }

    @Override
    public List<Product> findProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name category_name FROM products p LEFT JOIN categories c ON c.id = p.category_id ORDER BY p.name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        }
        return products;
    }

    @Override
    public Optional<Product> findProduct(Integer id) throws Exception {
        String sql = "SELECT p.*, c.name category_name FROM products p LEFT JOIN categories c ON c.id = p.category_id WHERE p.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProduct(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Product saveProduct(Product product) throws Exception {
        if (product.isNew()) {
            return insertProduct(product);
        }
        updateProduct(product);
        return product;
    }

    @Override
    public void deleteProduct(Integer id) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Нельзя удалить товар: он используется в продажах");
        }
    }

    @Override
    public boolean isProductUsed(Integer id) throws Exception {
        return exists("SELECT 1 FROM sale_items WHERE product_id = ? LIMIT 1", id);
    }

    @Override
    public StockCheckDTO checkStock(Integer productId, int quantity) throws Exception {
        Product product = findProduct(productId).orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        StockCheckDTO dto = new StockCheckDTO(productId, quantity);
        dto.setProductName(product.getName());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setAvailable(product.getStockQuantity() >= quantity);
        return dto;
    }

    @Override
    public void changeStock(Connection connection, Integer productId, int delta) throws Exception {
        String selectSql = "SELECT stock_quantity FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setInt(1, productId);
            try (ResultSet resultSet = select.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Товар не найден");
                }
                int current = resultSet.getInt("stock_quantity");
                int next = current + delta;
                if (next < 0) {
                    throw new IllegalStateException("Недостаточно товара на складе");
                }
            }
        }

        String updateSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        try (PreparedStatement update = connection.prepareStatement(updateSql)) {
            update.setInt(1, delta);
            update.setInt(2, productId);
            update.executeUpdate();
        }
    }

    private Product insertProduct(Product product) throws Exception {
        String sql = "INSERT INTO products(name, article, stock_quantity, category_id, purchase_price, retail_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillProductStatement(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        }
        return product;
    }

    private void updateProduct(Product product) throws Exception {
        String sql = "UPDATE products SET name = ?, article = ?, stock_quantity = ?, category_id = ?, purchase_price = ?, retail_price = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillProductStatement(statement, product);
            statement.setInt(7, product.getId());
            statement.executeUpdate();
        }
    }

    private void fillProductStatement(PreparedStatement statement, Product product) throws Exception {
        statement.setString(1, product.getName());
        statement.setString(2, product.getArticle());
        statement.setInt(3, product.getStockQuantity());
        if (product.getCategoryId() == null) {
            statement.setNull(4, java.sql.Types.INTEGER);
        } else {
            statement.setInt(4, product.getCategoryId());
        }
        statement.setBigDecimal(5, product.getPurchasePrice());
        statement.setBigDecimal(6, product.getRetailPrice());
    }

    private Product mapProduct(ResultSet resultSet) throws Exception {
        Product product = new Product();
        product.setId(resultSet.getInt("id"));
        product.setName(resultSet.getString("name"));
        product.setArticle(resultSet.getString("article"));
        product.setStockQuantity(resultSet.getInt("stock_quantity"));
        int categoryId = resultSet.getInt("category_id");
        product.setCategoryId(resultSet.wasNull() ? null : categoryId);
        product.setCategoryName(resultSet.getString("category_name"));
        product.setPurchasePrice(resultSet.getBigDecimal("purchase_price"));
        product.setRetailPrice(resultSet.getBigDecimal("retail_price"));
        return product;
    }

    private boolean exists(String sql, Integer id) throws Exception {
        if (id == null) {
            return false;
        }
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
