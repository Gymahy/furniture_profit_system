package models.dto;

import models.entities.Expense;

import java.io.Serializable;
import java.time.LocalDate;

public class ExpenseRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Expense expense;
    private LocalDate startDate;
    private LocalDate endDate;

    public ExpenseRequestDTO() {
    }

    public ExpenseRequestDTO(Expense expense, LocalDate startDate, LocalDate endDate) {
        this.expense = expense;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
