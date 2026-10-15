package services;

import dao.AuditDAO;
import dao.UserDAO;
import dao.impl.AuditDAOImpl;
import dao.impl.UserDAOImpl;
import enums.RoleType;
import exceptions.AuthException;
import models.entities.Role;
import models.entities.User;
import utility.PasswordHasher;

import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAOImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();

    public User login(String login, String password) throws Exception {
        User user = userDAO.findByLogin(login)
                .orElseThrow(() -> new AuthException("Неверный логин или пароль"));
        if (!user.isActive()) {
            throw new AuthException("Учетная запись заблокирована");
        }
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            auditDAO.log(user.getId(), "LOGIN_FAILED", "users", user.getId(), "Invalid password");
            throw new AuthException("Неверный логин или пароль");
        }
        auditDAO.log(user.getId(), "LOGIN", "users", user.getId(), "Successful login");
        return sanitize(user);
    }

    public List<User> findAll() throws Exception {
        List<User> users = userDAO.findAll();
        users.forEach(this::sanitize);
        return users;
    }

    public List<Role> findRoles() throws Exception {
        return userDAO.findRoles();
    }

    public User save(User user, User actor) throws Exception {
        boolean newUser = user.isNew();
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new IllegalArgumentException("Укажите логин пользователя");
        }
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new IllegalArgumentException("Укажите ФИО пользователя");
        }
        user.setLogin(user.getLogin().trim());
        user.setFullName(user.getFullName().trim());
        if (newUser && (user.getPasswordHash() == null || user.getPasswordHash().isBlank())) {
            throw new IllegalArgumentException("Укажите пароль нового пользователя");
        }
        User existing = userDAO.findByLogin(user.getLogin()).orElse(null);
        if (existing != null && (user.getId() == null || !user.getId().equals(existing.getId()))) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        User current = null;
        if (!newUser) {
            current = userDAO.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        }
        if (user.getRoleId() == null && user.getRole() != null) {
            Role role = userDAO.findRole(user.getRole()).orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
            user.setRoleId(role.getId());
        }
        if (user.getRoleId() == null) {
            throw new IllegalArgumentException("Укажите роль пользователя");
        }
        if (current != null && !sameRole(current.getRoleId(), user.getRoleId())) {
            if (actor.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Администратор не может изменять собственную роль");
            }
            if (current.getRole() == RoleType.ADMIN) {
                throw new IllegalArgumentException("Администратор не может изменять роль другого администратора");
            }
        }
        if (newUser) {
            user.setPasswordHash(PasswordHasher.hash(user.getPasswordHash()));
        }
        User saved = userDAO.save(user);
        auditDAO.log(actor.getId(), newUser ? "CREATE_USER" : "UPDATE_USER", "users", saved.getId(), saved.getLogin());
        return sanitize(saved);
    }

    public void delete(Integer userId, User actor) throws Exception {
        User target = userDAO.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (actor.getId().equals(userId)) {
            throw new IllegalArgumentException("Администратор не может удалить собственную учетную запись");
        }
        if (target.getRole() == RoleType.ADMIN) {
            throw new IllegalArgumentException("Администратор не может удалять других администраторов");
        }
        userDAO.delete(userId);
        auditDAO.log(actor.getId(), "DELETE_USER", "users", userId, target.getLogin());
    }

    public void setActive(Integer userId, boolean active, User actor) throws Exception {
        if (actor.getId().equals(userId) && !active) {
            throw new IllegalArgumentException("Нельзя заблокировать собственную учетную запись");
        }
        userDAO.setActive(userId, active);
        auditDAO.log(actor.getId(), active ? "UNBLOCK_USER" : "BLOCK_USER", "users", userId, null);
    }

    public void resetPassword(Integer userId, String newPassword, User actor) throws Exception {
        userDAO.updatePassword(userId, PasswordHasher.hash(newPassword));
        auditDAO.log(actor.getId(), "RESET_PASSWORD", "users", userId, null);
    }

    public void bootstrapAdmin() throws Exception {
        userDAO.ensureDefaultRoles();
        if (!userDAO.hasUsers()) {
            Role adminRole = userDAO.findRole(RoleType.ADMIN)
                    .orElseThrow(() -> new IllegalStateException("ADMIN role is not initialized"));
            User admin = new User();
            admin.setLogin("admin");
            admin.setFullName("Системный администратор");
            admin.setRoleId(adminRole.getId());
            admin.setRole(RoleType.ADMIN);
            admin.setActive(true);
            admin.setPasswordHash(PasswordHasher.hash("admin123"));
            userDAO.save(admin);
            auditDAO.log(admin.getId(), "BOOTSTRAP_ADMIN", "users", admin.getId(), "Default password: admin123");
        }
    }

    private User sanitize(User user) {
        user.setPasswordHash("");
        return user;
    }

    private boolean sameRole(Integer first, Integer second) {
        return first != null && first.equals(second);
    }
}
