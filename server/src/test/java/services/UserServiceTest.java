package services;

import models.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {
    private final UserService service = new UserService();

    @Test
    void rejectsBlankLogin() {
        User user = validUser();
        user.setLogin(" ");

        assertThrows(IllegalArgumentException.class, () -> service.save(user, actor()));
    }

    @Test
    void rejectsBlankFullName() {
        User user = validUser();
        user.setFullName(" ");

        assertThrows(IllegalArgumentException.class, () -> service.save(user, actor()));
    }

    @Test
    void rejectsBlankPassword() {
        User user = validUser();
        user.setPasswordHash(" ");

        assertThrows(IllegalArgumentException.class, () -> service.save(user, actor()));
    }

    private User validUser() {
        User user = new User();
        user.setLogin("user");
        user.setFullName("User");
        user.setPasswordHash("secret");
        return user;
    }

    private User actor() {
        User user = new User();
        user.setId(1);
        return user;
    }
}
