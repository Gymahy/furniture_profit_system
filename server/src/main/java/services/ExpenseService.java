package services;

import dao.AuditDAO;
import dao.ExpenseDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.ExpenseDAOImpl;
import factory.FinancialOperationFactory;
import models.entities.Expense;
import models.entities.ExpenseCategory;
import models.entities.User;

import java.time.LocalDate;
import java.util.List;

public class ExpenseService {
    private final ExpenseDAO expenseDAO = new ExpenseDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public List<ExpenseCategory> findCategories() throws Exception {
        return expenseDAO.findCategories();
    }

    public ExpenseCategory saveCategory(ExpenseCategory category, User actor) throws Exception {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Укажите категорию расхода");
        }
        if (category.getDescription() == null || category.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание категории расходов не может быть пустым");
        }
        ensureUniqueCategoryName(category);
        ExpenseCategory saved = expenseDAO.saveCategory(category);
        auditDAO.log(actor.getId(), "SAVE_EXPENSE_CATEGORY", "expense_categories", saved.getId(), saved.getName());
        return saved;
    }

    public void deleteCategory(Integer id, User actor) throws Exception {
        if (expenseDAO.isCategoryUsed(id)) {
            throw new IllegalArgumentException("Нельзя удалить категорию расходов: она используется в расходах");
        }
        expenseDAO.deleteCategory(id);
        auditDAO.log(actor.getId(), "DELETE_EXPENSE_CATEGORY", "expense_categories", id, null);
    }

    public List<Expense> findByPeriod(LocalDate startDate, LocalDate endDate) throws Exception {
        validatePeriod(startDate, endDate);
        return expenseDAO.findByPeriod(startDate, endDate);
    }

    public Expense save(Expense expense, User actor) throws Exception {
        FinancialOperationFactory.expense(expense).create().validate();
        if (expense.getDescription() == null || expense.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание расхода не может быть пустым");
        }
        expense.setUserId(actor.getId());
        Expense saved = expenseDAO.save(expense);
        auditDAO.log(actor.getId(), "SAVE_EXPENSE", "expenses", saved.getId(), saved.getDescription());
        return saved;
    }

    public Expense save(Expense expense, User actor, LocalDate startDate, LocalDate endDate) throws Exception {
        validatePeriod(startDate, endDate);
        ensureExpenseDateInPeriod(expense, startDate, endDate);
        return save(expense, actor);
    }

    public void delete(Integer id, User actor) throws Exception {
        expenseDAO.delete(id);
        auditDAO.log(actor.getId(), "DELETE_EXPENSE", "expenses", id, null);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Укажите корректный отчетный период");
        }
    }

    private void ensureExpenseDateInPeriod(Expense expense, LocalDate startDate, LocalDate endDate) {
        if (expense == null || expense.getExpenseDate() == null) {
            throw new IllegalArgumentException("Укажите дату расхода");
        }
        LocalDate expenseDate = expense.getExpenseDate();
        if (expenseDate.isBefore(startDate) || expenseDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Нельзя сохранить расход вне выбранного периода");
        }
    }

    private void ensureUniqueCategoryName(ExpenseCategory category) throws Exception {
        boolean duplicate = expenseDAO.findCategories().stream()
                .anyMatch(existing -> existing.getName() != null
                        && category.getName() != null
                        && existing.getName().trim().equalsIgnoreCase(category.getName().trim())
                        && (category.getId() == null || !category.getId().equals(existing.getId())));
        if (duplicate) {
            throw new IllegalArgumentException("Категория расходов с таким названием уже существует");
        }
    }
}
