package factory;

import models.entities.Expense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpenseOperationFactoryTest {
    @Test
    void acceptsValidExpense() {
        Expense expense = expense(LocalDate.of(2026, 5, 10), BigDecimal.TEN, 1);

        assertDoesNotThrow(() -> FinancialOperationFactory.expense(expense).create().validate());
    }

    @Test
    void rejectsMissingDate() {
        Expense expense = expense(null, BigDecimal.TEN, 1);

        assertThrows(IllegalArgumentException.class,
                () -> FinancialOperationFactory.expense(expense).create().validate());
    }

    @Test
    void rejectsNonPositiveAmount() {
        Expense expense = expense(LocalDate.of(2026, 5, 10), BigDecimal.ZERO, 1);

        assertThrows(IllegalArgumentException.class,
                () -> FinancialOperationFactory.expense(expense).create().validate());
    }

    @Test
    void rejectsMissingCategory() {
        Expense expense = expense(LocalDate.of(2026, 5, 10), BigDecimal.TEN, null);

        assertThrows(IllegalArgumentException.class,
                () -> FinancialOperationFactory.expense(expense).create().validate());
    }

    private Expense expense(LocalDate date, BigDecimal amount, Integer categoryId) {
        Expense expense = new Expense();
        expense.setExpenseDate(date);
        expense.setAmount(amount);
        expense.setCategoryId(categoryId);
        return expense;
    }
}
