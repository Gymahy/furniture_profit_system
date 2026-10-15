package factory;

import models.dto.SaleItemDTO;
import models.dto.SaleRequestDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleOperationFactoryTest {
    @Test
    void acceptsValidSale() {
        SaleRequestDTO request = saleRequest(1, List.of(new SaleItemDTO(1, "Table", 1, BigDecimal.TEN)));

        assertDoesNotThrow(() -> FinancialOperationFactory.sale(request).create().validate());
    }

    @Test
    void rejectsMissingCustomer() {
        SaleRequestDTO request = saleRequest(null, List.of(new SaleItemDTO(1, "Table", 1, BigDecimal.TEN)));

        assertThrows(IllegalArgumentException.class,
                () -> FinancialOperationFactory.sale(request).create().validate());
    }

    @Test
    void rejectsEmptyItems() {
        SaleRequestDTO request = saleRequest(1, List.of());

        assertThrows(IllegalArgumentException.class,
                () -> FinancialOperationFactory.sale(request).create().validate());
    }

    private SaleRequestDTO saleRequest(Integer customerId, List<SaleItemDTO> items) {
        SaleRequestDTO request = new SaleRequestDTO();
        request.setCustomerId(customerId);
        request.setItems(items);
        return request;
    }
}
