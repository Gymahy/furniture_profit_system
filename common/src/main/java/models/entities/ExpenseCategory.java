package models.entities;

public class ExpenseCategory extends NamedEntity {
    private static final long serialVersionUID = 1L;

    public ExpenseCategory() {
    }

    public ExpenseCategory(Integer id, String name, String description) {
        super(id, name, description);
    }
}
