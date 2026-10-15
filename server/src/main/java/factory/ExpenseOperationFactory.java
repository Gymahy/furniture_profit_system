package factory;

import models.entities.Expense;
import java.math.BigDecimal;

public class ExpenseOperationFactory extends FinancialOperationFactory {
    private final Expense expense;

    public ExpenseOperationFactory(Expense expense) {
        this.expense = expense;
    }

    @Override
    public FinancialOperation create() {
        return () -> {
            if (expense == null || expense.getExpenseDate() == null) {
                throw new IllegalArgumentException("Укажите дату расхода");
            }
            if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Сумма расхода должна быть больше нуля");
            }
            if (expense.getCategoryId() == null) {
                throw new IllegalArgumentException("Выберите категорию расхода");
            }
        };
    }
}
