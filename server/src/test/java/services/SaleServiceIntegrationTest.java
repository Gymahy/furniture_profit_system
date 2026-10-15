package services;

import enums.RoleType;
import enums.SaleStatus;
import models.dto.SaleItemDTO;
import models.dto.SaleRequestDTO;
import models.entities.Sale;
import models.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import support.TestDatabaseSupport;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class SaleServiceIntegrationTest {
    private final SaleService service = new SaleService();
    private Integer managerId;
    private Integer categoryId;
    private Integer productId;
    private Integer customerId;
    private Integer saleId;

    @AfterEach
    void cleanup() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            if (saleId != null) {
                TestDatabaseSupport.deleteSaleGraph(connection, saleId);
            }
            if (productId != null) {
                TestDatabaseSupport.deleteProduct(connection, productId);
            }
            if (categoryId != null) {
                TestDatabaseSupport.deleteCategory(connection, categoryId);
            }
            if (customerId != null) {
                TestDatabaseSupport.deleteCustomer(connection, customerId);
            }
            if (managerId != null) {
                TestDatabaseSupport.deleteUser(connection, managerId);
            }
        }
    }

    @Test
    void handlesSaleLifecycle() throws Exception {
        setupFixture();
        SaleRequestDTO request = new SaleRequestDTO();
        request.setCustomerId(customerId);
        request.setInitialStatus(SaleStatus.CONFIRMED);
        request.setItems(List.of(new SaleItemDTO(productId, "product", 2, new BigDecimal("150.00"))));

        Sale sale = service.createSale(request, manager());
        saleId = sale.getId();
        service.changeStatus(saleId, SaleStatus.PAID, manager());

        List<Sale> sales = service.findSales(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        boolean commissionCreated = service.findCommissions(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)).stream()
                .anyMatch(commission -> saleId.equals(commission.getSaleId()));
        int stockAfterCreate;
        try (Connection connection = TestDatabaseSupport.connection()) {
            stockAfterCreate = TestDatabaseSupport.productStock(connection, productId);
        }

        service.deleteSale(saleId, manager());
        int stockAfterDelete;
        try (Connection connection = TestDatabaseSupport.connection()) {
            stockAfterDelete = TestDatabaseSupport.productStock(connection, productId);
        }

        assertTrue(sales.stream().anyMatch(row -> saleId.equals(row.getId())));
        assertTrue(commissionCreated);
        assertEquals(8, stockAfterCreate);
        assertEquals(10, stockAfterDelete);
    }

    private void setupFixture() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            managerId = TestDatabaseSupport.insertUser(connection, RoleType.SALES_MANAGER, "sale_manager");
            categoryId = TestDatabaseSupport.insertCategory(connection, "sale_category");
            productId = TestDatabaseSupport.insertProduct(connection, categoryId, 10, "sale_product");
            customerId = TestDatabaseSupport.insertCustomer(connection, "sale_customer");
        }
    }

    private User manager() {
        User user = new User();
        user.setId(managerId);
        return user;
    }
}
