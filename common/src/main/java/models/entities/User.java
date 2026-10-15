package models.entities;

import enums.RoleType;

public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String login;
    private String passwordHash;
    private String fullName;
    private Integer roleId;
    private RoleType role;
    private boolean active = true;

    public User() {
    }

    public User(Integer id, String login, String passwordHash, String fullName, Integer roleId, RoleType role, boolean active) {
        super(id);
        this.login = login;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.roleId = roleId;
        this.role = role;
        this.active = active;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRoleTitle() {
        return role == null ? "" : role.getTitle();
    }

    @Override
    public String toString() {
        return fullName == null || fullName.isBlank() ? login : fullName;
    }
}
