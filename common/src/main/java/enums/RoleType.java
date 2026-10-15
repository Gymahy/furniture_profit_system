package enums;

import java.io.Serializable;

public enum RoleType implements Serializable {
    ADMIN("Администратор"),
    SALES_MANAGER("Менеджер по продажам"),
    ACCOUNTANT("Бухгалтер");

    private final String title;

    RoleType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static RoleType fromDbName(String name) {
        if (name == null) {
            return SALES_MANAGER;
        }
        for (RoleType role : values()) {
            if (role.name().equalsIgnoreCase(name) || role.title.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return RoleType.valueOf(name.toUpperCase());
    }

    @Override
    public String toString() {
        return title;
    }
}
