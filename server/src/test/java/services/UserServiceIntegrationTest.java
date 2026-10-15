package services;

import enums.RoleType;
import models.entities.Role;
import models.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import support.TestDatabaseSupport;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class UserServiceIntegrationTest {
    private final UserService service = new UserService();
    private Integer actorId;
    private Integer userId;

    @AfterEach
    void cleanup() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            if (userId != null) {
                TestDatabaseSupport.deleteUser(connection, userId);
            }
            if (actorId != null) {
                TestDatabaseSupport.deleteUser(connection, actorId);
            }
        }
    }

    @Test
    void handlesUserLifecycle() throws Exception {
        setupActor();
        Role accountant = service.findRoles().stream()
                .filter(role -> role.getName() == RoleType.ACCOUNTANT)
                .findFirst()
                .orElseThrow();
        User user = new User();
        user.setLogin(TestDatabaseSupport.unique("service_user"));
        user.setFullName("Service User Integration");
        user.setPasswordHash("oldPassword123");
        user.setRoleId(accountant.getId());
        user.setRole(RoleType.ACCOUNTANT);
        user.setActive(true);

        User saved = service.save(user, actor());
        userId = saved.getId();
        User loggedIn = service.login(user.getLogin(), "oldPassword123");
        service.resetPassword(userId, "newPassword123", actor());
        User loggedInAfterReset = service.login(user.getLogin(), "newPassword123");
        service.setActive(userId, false, actor());

        assertEquals(user.getLogin(), loggedIn.getLogin());
        assertEquals(user.getLogin(), loggedInAfterReset.getLogin());
        assertNotEquals("oldPassword123", saved.getPasswordHash());
        assertFalse(service.findAll().stream().filter(row -> userId.equals(row.getId())).findFirst().orElseThrow().isActive());
    }

    private void setupActor() throws Exception {
        try (Connection connection = TestDatabaseSupport.connection()) {
            actorId = TestDatabaseSupport.insertUser(connection, RoleType.ADMIN, "user_actor");
        }
        assertTrue(actorId > 0);
    }

    private User actor() {
        User user = new User();
        user.setId(actorId);
        return user;
    }
}
