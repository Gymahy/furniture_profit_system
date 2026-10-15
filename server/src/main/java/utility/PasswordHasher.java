package utility;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String passwordHash) {
        if (plainPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, passwordHash);
    }
}
