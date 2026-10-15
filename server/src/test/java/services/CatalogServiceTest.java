package services;

import models.entities.Category;
import models.entities.Product;
import models.entities.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogServiceTest {
    private final CatalogService service = new CatalogService();

    @Test
    void rejectsBlankCategoryName() {
        Category category = new Category();
        category.setName(" ");
        category.setDescription("Description");

        assertThrows(IllegalArgumentException.class, () -> service.saveCategory(category, actor()));
    }

    @Test
    void rejectsBlankCategoryDescription() {
        Category category = new Category();
        category.setName("Sofa");
        category.setDescription(" ");

        assertThrows(IllegalArgumentException.class, () -> service.saveCategory(category, actor()));
    }

    @Test
    void rejectsNegativeStock() {
        Product product = validProduct();
        product.setStockQuantity(-1);

        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(product, actor()));
    }

    @Test
    void rejectsNegativePrice() {
        Product product = validProduct();
        product.setPurchasePrice(new BigDecimal("-1.00"));

        assertThrows(IllegalArgumentException.class, () -> service.saveProduct(product, actor()));
    }

    private Product validProduct() {
        Product product = new Product();
        product.setName("Product");
        product.setStockQuantity(1);
        product.setPurchasePrice(BigDecimal.ONE);
        product.setRetailPrice(BigDecimal.TEN);
        return product;
    }

    private User actor() {
        User user = new User();
        user.setId(1);
        return user;
    }
}
