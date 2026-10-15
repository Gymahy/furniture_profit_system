package services;

import enums.RoleType;
import models.entities.Category;
import models.entities.Product;
import models.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import support.TestDatabaseSupport;

import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class CatalogServiceIntegrationTest {
    private final CatalogService service = new CatalogService();
    private Integer actorId;
    private Integer categoryId;
    private Integer productId;

    @AfterEach
    void cleanup() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            if (productId != null) {
                TestDatabaseSupport.deleteProduct(connection, productId);
            }
            if (categoryId != null) {
                TestDatabaseSupport.deleteCategory(connection, categoryId);
            }
            if (actorId != null) {
                TestDatabaseSupport.deleteUser(connection, actorId);
            }
        }
    }

    @Test
    void savesCatalogData() throws Exception {
        setupActor();
        Category category = new Category();
        category.setName(TestDatabaseSupport.unique("Service catalog category"));
        category.setDescription("Catalog service integration category");
        categoryId = service.saveCategory(category, actor()).getId();

        Product product = product(categoryId, TestDatabaseSupport.unique("Service product"));
        productId = service.saveProduct(product, actor()).getId();
        Product duplicate = product(categoryId, product.getName().toUpperCase());

        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(duplicate, actor()));
        assertTrue(service.findProducts().stream().anyMatch(row -> productId.equals(row.getId())));
        assertEquals(productId, service.checkStock(new models.dto.StockCheckDTO(productId, 2)).getProductId());
    }

    private void setupActor() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            actorId = TestDatabaseSupport.insertUser(connection, RoleType.ADMIN, "catalog_actor");
        }
    }

    private Product product(Integer categoryId, String name) {
        Product product = new Product();
        product.setName(name);
        product.setArticle(TestDatabaseSupport.unique("SERVICE-ART"));
        product.setStockQuantity(5);
        product.setCategoryId(categoryId);
        product.setPurchasePrice(new BigDecimal("20.00"));
        product.setRetailPrice(new BigDecimal("35.00"));
        return product;
    }

    private User actor() {
        User user = new User();
        user.setId(actorId);
        return user;
    }
}
