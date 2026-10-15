package utility;

import enums.RoleType;
import models.entities.User;

public final class RoleChecker {
    private RoleChecker() {
    }

    public static boolean isAdmin(User user) {
        return hasRole(user, RoleType.ADMIN);
    }

    public static boolean isSalesManager(User user) {
        return hasRole(user, RoleType.SALES_MANAGER);
    }

    public static boolean isAccountant(User user) {
        return hasRole(user, RoleType.ACCOUNTANT);
    }

    public static boolean canManageUsers(User user) {
        return isAdmin(user);
    }

    public static boolean canManageCatalog(User user) {
        return isAdmin(user);
    }

    public static boolean canManageDiscounts(User user) {
        return isAdmin(user);
    }

    public static boolean canWorkWithSales(User user) {
        return isSalesManager(user);
    }

    public static boolean canWorkWithCustomers(User user) {
        return isSalesManager(user);
    }

    public static boolean canWorkWithFinance(User user) {
        return isAccountant(user);
    }

    private static boolean hasRole(User user, RoleType roleType) {
        return user != null && user.isActive() && roleType == user.getRole();
    }
}
