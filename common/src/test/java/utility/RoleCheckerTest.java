package utility;

import enums.RoleType;
import models.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleCheckerTest {
    @Test
    void checksAdminPermissions() {
        User user = user(RoleType.ADMIN, true);

        assertTrue(RoleChecker.canManageUsers(user));
        assertTrue(RoleChecker.canManageCatalog(user));
        assertTrue(RoleChecker.canManageDiscounts(user));
        assertFalse(RoleChecker.canWorkWithSales(user));
        assertFalse(RoleChecker.canWorkWithFinance(user));
    }

    @Test
    void checksManagerPermissions() {
        User user = user(RoleType.SALES_MANAGER, true);

        assertTrue(RoleChecker.canWorkWithSales(user));
        assertTrue(RoleChecker.canWorkWithCustomers(user));
        assertFalse(RoleChecker.canManageUsers(user));
        assertFalse(RoleChecker.canWorkWithFinance(user));
    }

    @Test
    void rejectsInactiveUser() {
        User user = user(RoleType.ADMIN, false);

        assertFalse(RoleChecker.canManageUsers(user));
        assertFalse(RoleChecker.canManageCatalog(user));
        assertFalse(RoleChecker.canManageDiscounts(user));
        assertFalse(RoleChecker.canWorkWithSales(user));
        assertFalse(RoleChecker.canWorkWithCustomers(user));
        assertFalse(RoleChecker.canWorkWithFinance(user));
    }

    private User user(RoleType roleType, boolean active) {
        User user = new User();
        user.setRole(roleType);
        user.setActive(active);
        return user;
    }
}
