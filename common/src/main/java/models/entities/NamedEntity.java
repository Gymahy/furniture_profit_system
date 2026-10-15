package models.entities;

public abstract class NamedEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;

    public NamedEntity() {
    }

    public NamedEntity(Integer id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
