package me.bartosz1.monitoring.models;

//Password modification data object :)
//me & my weird naming conventions
public class PasswordMDO {

    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirmation;

    public String getOldPassword() {
        return oldPassword;
    }

    public PasswordMDO setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public PasswordMDO setNewPassword(String newPassword) {
        this.newPassword = newPassword;
        return this;
    }

    public String getNewPasswordConfirmation() {
        return newPasswordConfirmation;
    }

    public PasswordMDO setNewPasswordConfirmation(String newPasswordConfirmation) {
        this.newPasswordConfirmation = newPasswordConfirmation;
        return this;
    }
}
