package models.entities;

public class Category extends NamedEntity {
    private static final long serialVersionUID = 1L;

    public Category() {
    }

    public Category(Integer id, String name, String description) {
        super(id, name, description);
    }
}
