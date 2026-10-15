package factory;

import models.dto.SaleRequestDTO;

public class SaleOperationFactory extends FinancialOperationFactory {
    private final SaleRequestDTO request;

    public SaleOperationFactory(SaleRequestDTO request) {
        this.request = request;
    }

    @Override
    public FinancialOperation create() {
        return () -> {
            if (request == null || request.getCustomerId() == null) {
                throw new IllegalArgumentException("Выберите клиента");
            }
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new IllegalArgumentException("Добавьте хотя бы одну позицию продажи");
            }
        };
    }
}
