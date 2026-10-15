package services;

import dao.AuditDAO;
import dao.CatalogDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.CatalogDAOImpl;
import models.dto.StockCheckDTO;
import models.entities.Category;
import models.entities.Product;
import models.entities.User;

import java.math.BigDecimal;
import java.util.List;

public class CatalogService {
    private final CatalogDAO catalogDAO = new CatalogDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public List<Category> findCategories() throws Exception {
        return catalogDAO.findCategories();
    }

    public Category saveCategory(Category category, User actor) throws Exception {
        validateName(category.getName(), "Название категории");
        validateDescription(category.getDescription(), "Описание категории товаров");
        ensureUniqueCategoryName(category);
        Category saved = catalogDAO.saveCategory(category);
        auditDAO.log(actor.getId(), "SAVE_CATEGORY", "categories", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteCategory(Integer id, User actor) throws Exception {
        if (catalogDAO.isCategoryUsed(id)) {
            throw new IllegalArgumentException("Нельзя удалить категорию товаров: она используется в товарах");
        }
        catalogDAO.deleteCategory(id);
        auditDAO.log(actor.getId(), "DELETE_CATEGORY", "categories", id, null);
    }

    public List<Product> findProducts() throws Exception {
        return catalogDAO.findProducts();
    }

    public Product saveProduct(Product product, User actor) throws Exception {
        validateName(product.getName(), "Название товара");
        if (product.getPurchasePrice() == null || product.getRetailPrice() == null) {
            throw new IllegalArgumentException("Укажите закупочную и розничную цену товара");
        }
        if (product.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0 || product.getRetailPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цены не могут быть отрицательными");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Остаток не может быть отрицательным");
        }
        ensureUniqueProductName(product);
        Product saved = catalogDAO.saveProduct(product);
        auditDAO.log(actor.getId(), "SAVE_PRODUCT", "products", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteProduct(Integer id, User actor) throws Exception {
        if (catalogDAO.isProductUsed(id)) {
            throw new IllegalArgumentException("Нельзя удалить товар: он используется в продажах");
        }
        catalogDAO.deleteProduct(id);
        auditDAO.log(actor.getId(), "DELETE_PRODUCT", "products", id, null);
    }

    public StockCheckDTO checkStock(StockCheckDTO request) throws Exception {
        return catalogDAO.checkStock(request.getProductId(), request.getQuantity());
    }

    private void validateName(String value, String title) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(title + " не может быть пустым");
        }
    }

    private void validateDescription(String value, String title) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(title + " не может быть пустым");
        }
    }

    private void ensureUniqueCategoryName(Category category) throws Exception {
        boolean duplicate = catalogDAO.findCategories().stream()
                .anyMatch(existing -> sameName(existing.getName(), category.getName())
                        && !sameId(existing.getId(), category.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("Категория товаров с таким названием уже существует");
        }
    }

    private void ensureUniqueProductName(Product product) throws Exception {
        boolean duplicate = catalogDAO.findProducts().stream()
                .anyMatch(existing -> sameName(existing.getName(), product.getName())
                        && !sameId(existing.getId(), product.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("Товар с таким названием уже существует");
        }
    }

    private boolean sameName(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private boolean sameId(Integer first, Integer second) {
        return first != null && first.equals(second);
    }
}
