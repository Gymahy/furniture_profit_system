package utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test
    void verifiesHash() {
        String hash = PasswordHasher.hash("secret123");

        assertTrue(PasswordHasher.verify("secret123", hash));
        assertFalse(PasswordHasher.verify("other-password", hash));
    }

    @Test
    void hashUsesSalt() {
        String first = PasswordHasher.hash("secret123");
        String second = PasswordHasher.hash("secret123");

        assertNotEquals(first, second);
    }

    @Test
    void rejectsBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(" "));
    }
}
