package movie;

import java.util.ArrayList;

//Storing of Accounts While the System Runs
final class AccountStore {
    private static ArrayList<UserAccount> accounts = new ArrayList<UserAccount>();

    //Blocking of Creating AccountStore Objects
    private AccountStore() {
    }

    //Finding of an Account by Role and Username
    static UserAccount findByUsername(UserAccount.Role role, String username) {
        if (username == null) {
            return null;
        }
        for (UserAccount account : accounts) {
            if (account.getRole() == role && account.getUsername().equalsIgnoreCase(username.trim())) {
                return account;
            }
        }
        return null;
    }

    //Registering of Staff or First Admin Accounts
    static String register(UserAccount.Role role, String username, String workId, String email, String password) {
        return addAccount(role, username, workId, email, password, false);
    }

    //Registering of an Additional Manager
    static String registerAdditionalManager(String username, String managerId, String email, String password) {
        return addAccount(UserAccount.Role.ADMIN, username, managerId, email, password, true);
    }

    //Validating and Saving of an Account
    private static String addAccount(UserAccount.Role role, String username, String workId, String email, String password, boolean approvedByAdmin) {
        if (username == null || workId == null || email == null || password == null) {
            return "Enter all account information.";
        }
        String cleanUsername = username.trim();
        String cleanWorkId = workId.trim();
        String cleanEmail = email.trim();
        if (cleanUsername.length() == 0 || cleanWorkId.length() == 0) {
            return "Enter a username and ID.";
        }
        if (!UserAccount.isValidEmail(cleanEmail)) {
            return "Enter a valid email address, such as name@gmail.com.";
        }
        if (password.length() < 6) {
            return "Use a password with at least 6 characters.";
        }
        for (UserAccount account : accounts) {
            if (role == UserAccount.Role.ADMIN && !approvedByAdmin && account.getRole() == UserAccount.Role.ADMIN) {
                return "An admin already exists. Ask an admin to add another one.";
            }
            if (account.getUsername().equalsIgnoreCase(cleanUsername)) {
                return "That username is already in use.";
            }
            if (account.getWorkId().equalsIgnoreCase(cleanWorkId)) {
                return "That ID is already registered.";
            }
            if (account.getEmail().equalsIgnoreCase(cleanEmail)) {
                return "That email is already registered.";
            }
        }
        UserAccount account = new UserAccount(cleanUsername, cleanWorkId, cleanEmail, password, role);
        accounts.add(account);
        return null;
    }

    //Getting of the Staff Accounts List
    static ArrayList<UserAccount> staffAccounts() {
        ArrayList<UserAccount> staff = new ArrayList<UserAccount>();
        for (UserAccount account : accounts) {
            if (account.getRole() == UserAccount.Role.STAFF) {
                staff.add(account);
            }
        }
        return staff;
    }
}