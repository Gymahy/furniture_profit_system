package dao;

import enums.RoleType;
import models.entities.Role;
import models.entities.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO extends DAO<User> {
    Optional<User> findByLogin(String login) throws Exception;

    Optional<User> findById(Integer id) throws Exception;

    List<Role> findRoles() throws Exception;

    Optional<Role> findRole(RoleType roleType) throws Exception;

    void setActive(Integer userId, boolean active) throws Exception;

    void updatePassword(Integer userId, String passwordHash) throws Exception;

    void ensureDefaultRoles() throws Exception;

    boolean hasUsers() throws Exception;
}
