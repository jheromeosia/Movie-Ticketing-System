package movie;

//Account Information and Password
public final class UserAccount {
    public enum Role {
        STAFF, ADMIN
    }

    //Private Account Fields
    private String username;
    private String workId;
    private String email;
    private String password;
    private Role role;
    private int failedAttempts;
    private boolean locked;

    //Creating of an Account
    public UserAccount(String username, String workId, String email, String password, Role role) {
        this.username = username;
        this.workId = workId;
        this.email = email;
        this.password = password;
        this.role = role;
        failedAttempts = 0;
        locked = false;
    }

    //Getting of the Username
    public String getUsername() {
        return username;
    }

    //Getting of the Staff or Manager ID
    public String getWorkId() {
        return workId;
    }

    //Getting of the Email
    public String getEmail() {
        return email;
    }

    //Hiding of the Middle Part of the Email
    public String getMaskedEmail() {
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String name = email.substring(0, at);
        String visible = name.substring(0, Math.min(2, name.length()));
        return visible + "***" + email.substring(at);
    }

    //Getting of the Role
    public Role getRole() {
        return role;
    }

    //Checking if the Account is Locked
    public boolean isLocked() {
        return locked;
    }

    //Getting of the Failed Login Attempts
    public int getFailedAttempts() {
        return failedAttempts;
    }

    //Checking of the Password and Locking After 3 Failures
    public boolean verifyPassword(String enteredPassword) {
        if (locked) {
            return false;
        }
        if (password.equals(enteredPassword)) {
            failedAttempts = 0;
            return true;
        }
        if (role == Role.STAFF) {
            failedAttempts = failedAttempts + 1;
            if (failedAttempts >= 3) {
                locked = true;
            }
        }
        return false;
    }

    //Changing of the Password
    public void changePassword(String newPassword) {
        password = newPassword;
        if (!locked) {
            failedAttempts = 0;
        }
    }

    //Unlocking of the Account
    public void unlock() {
        locked = false;
        failedAttempts = 0;
    }

    //Validation of the Email Format
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String clean = email.trim();
        if (clean.length() == 0 || clean.indexOf(' ') >= 0) {
            return false;
        }
        int at = clean.indexOf('@');
        if (at <= 0 || at != clean.lastIndexOf('@')) {
            return false;
        }
        String domain = clean.substring(at + 1);
        int dot = domain.lastIndexOf('.');
        return dot > 0 && dot < domain.length() - 1;
    }

    //Validation of the Password Reset Code
    public static boolean isValidPasswordResetCode(String code) {
        if (code == null) {
            return false;
        }
        return code.equals("123456");
    }
}