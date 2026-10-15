package factory;

import models.dto.SaleRequestDTO;
import models.entities.Expense;

public abstract class FinancialOperationFactory {
    public abstract FinancialOperation create();

    public static FinancialOperationFactory sale(SaleRequestDTO request) {
        return new SaleOperationFactory(request);
    }

    public static FinancialOperationFactory expense(Expense expense) {
        return new ExpenseOperationFactory(expense);
    }
}
