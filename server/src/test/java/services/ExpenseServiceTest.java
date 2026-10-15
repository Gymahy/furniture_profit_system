package services;

import models.entities.Expense;
import models.entities.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpenseServiceTest {
    private final ExpenseService service = new ExpenseService();

    @Test
    void RejectsBeforePeriod() {
        Expense expense = validExpense(LocalDate.of(2026, 4, 30));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(expense, actor(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)));
    }

    @Test
    void RejectsAfterPeriod() {
        Expense expense = validExpense(LocalDate.of(2026, 6, 1));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(expense, actor(), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31)));
    }

    @Test
    void RejectsInvalidPeriod() {
        Expense expense = validExpense(LocalDate.of(2026, 5, 10));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(expense, actor(), LocalDate.of(2026, 5, 31), LocalDate.of(2026, 5, 1)));
    }

    private Expense validExpense(LocalDate date) {
        Expense expense = new Expense();
        expense.setExpenseDate(date);
        expense.setAmount(new BigDecimal("100.00"));
        expense.setCategoryId(1);
        expense.setDescription("Expense validation");
        return expense;
    }

    private User actor() {
        User user = new User();
        user.setId(1);
        return user;
    }
}
