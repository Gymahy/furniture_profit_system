package models.entities;

import enums.RoleType;

public class Role extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private RoleType name;

    public Role() {
    }

    public Role(Integer id, RoleType name) {
        super(id);
        this.name = name;
    }

    public RoleType getName() {
        return name;
    }

    public void setName(RoleType name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name == null ? "" : name.getTitle();
    }
}
