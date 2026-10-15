package dao;

import models.entities.Expense;
import models.entities.ExpenseCategory;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseDAO extends DAO<Expense> {
    List<ExpenseCategory> findCategories() throws Exception;

    ExpenseCategory saveCategory(ExpenseCategory category) throws Exception;

    void deleteCategory(Integer id) throws Exception;

    boolean isCategoryUsed(Integer id) throws Exception;

    List<Expense> findByPeriod(LocalDate startDate, LocalDate endDate) throws Exception;
}
