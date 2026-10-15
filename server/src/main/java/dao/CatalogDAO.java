package dao;

import models.dto.StockCheckDTO;
import models.entities.Category;
import models.entities.Product;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CatalogDAO {
    List<Category> findCategories() throws Exception;

    Category saveCategory(Category category) throws Exception;

    void deleteCategory(Integer id) throws Exception;

    boolean isCategoryUsed(Integer id) throws Exception;

    List<Product> findProducts() throws Exception;

    Optional<Product> findProduct(Integer id) throws Exception;

    Product saveProduct(Product product) throws Exception;

    void deleteProduct(Integer id) throws Exception;

    boolean isProductUsed(Integer id) throws Exception;

    StockCheckDTO checkStock(Integer productId, int quantity) throws Exception;

    void changeStock(Connection connection, Integer productId, int delta) throws Exception;
}
