package models.dto;

import java.io.Serializable;

public class PasswordResetDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String newPassword;

    public PasswordResetDTO() {
    }

    public PasswordResetDTO(Integer userId, String newPassword) {
        this.userId = userId;
        this.newPassword = newPassword;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
