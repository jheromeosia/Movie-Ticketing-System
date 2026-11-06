package movie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Login, Registration and Password Recovery Screens
public final class LoginPage extends JFrame implements ActionListener {
    private static final Color BG = new Color(12, 17, 28);
    private static final Color HEADER = new Color(17, 24, 39);
    private static final Color CARD = new Color(25, 34, 51);
    private static final Color BORDER = new Color(52, 66, 89);
    private static final Color ACCENT = new Color(255, 190, 72);
    private static final Color TEXT = new Color(242, 245, 250);
    private static final Color MUTED = new Color(157, 170, 190);

    //Private Fields of the Login Window
    private JPanel content;
    private UserAccount.Role selectedRole;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JTextField registrationUsername;
    private JTextField registrationId;
    private JTextField registrationEmail;
    private JPasswordField registrationPassword;
    private JPasswordField registrationConfirmation;

    //Setting Up of the Login Window
    public LoginPage() {
        super("iScreen - Sign in");
        MovieTicketSystem.initializeSharedData();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(screen);
        content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        setContentPane(content);
        showRolePicker();
        setVisible(true);
    }

    //Displaying of the Role Picker (Staff or Admin)
    private void showRolePicker() {
        JPanel heading = stack(6);
        heading.add(label("Welcome", 27, TEXT, true));
        heading.add(label("Choose your account type to sign in.", 14, MUTED, false));
        JPanel buttons = stack(10);
        buttons.add(button("STAFF", "LOGIN_STAFF", true));
        buttons.add(button("ADMIN / MANAGER", "LOGIN_ADMIN", true));
        buttons.add(button("CREATE AN ACCOUNT", "REGISTER_ROLE", false));
        JPanel card = buildCard(460, heading, null, buttons);
        showScreen("MOVIE TICKETING SYSTEM", "Staff and manager access", card);
    }

    //Displaying of the Registration Role Picker
    private void showRegistrationRolePicker() {
        JPanel heading = stack(6);
        heading.add(label("Create an account", 25, TEXT, true));
        heading.add(label("Choose the position for this new account.", 14, MUTED, false));
        JPanel buttons = stack(10);
        buttons.add(button("STAFF ACCOUNT", "REGISTER_STAFF", true));
        buttons.add(button("ADMIN / MANAGER ACCOUNT", "REGISTER_ADMIN", true));
        buttons.add(button("BACK", "ROLE_PICKER", false));
        JPanel card = buildCard(460, heading, null, buttons);
        showScreen("CREATE AN ACCOUNT", "Choose the account role", card);
    }

    //Displaying of the Login Form
    private void showLogin(UserAccount.Role role) {
        selectedRole = role;
        String roleName = "Staff";
        if (role == UserAccount.Role.ADMIN) {
            roleName = "Admin / Manager";
        }
        JPanel heading = stack(0);
        heading.add(label(roleName + " login", 25, TEXT, true));
        usernameField = new JTextField();
        styleField(usernameField);
        passwordField = new JPasswordField();
        styleField(passwordField);
        JPanel fields = stack(10);
        fields.add(fieldGroup("Username", usernameField));
        fields.add(fieldGroup("Password", passwordField));
        loginButton = button("LOG IN", "ATTEMPT_LOGIN", true);
        JPanel buttons = stack(8);
        buttons.add(loginButton);
        buttons.add(button("FORGOT PASSWORD?", "FORGOT_PASSWORD", false));
        buttons.add(button("CREATE AN ACCOUNT", "REGISTER_CURRENT_ROLE", false));
        buttons.add(button("BACK", "ROLE_PICKER", false));
        JPanel card = buildCard(460, heading, fields, buttons);
        showScreen("SIGN IN", roleName, card);
    }

    //Displaying of the Registration Form
    private void showRegistration(UserAccount.Role role) {
        selectedRole = role;
        String roleName = "Staff";
        String idLabel = "Work ID";
        if (role == UserAccount.Role.ADMIN) {
            roleName = "Admin / Manager";
            idLabel = "Manager ID";
        }
        JPanel heading = stack(0);
        heading.add(label(roleName + " registration", 24, TEXT, true));
        registrationUsername = new JTextField();
        styleField(registrationUsername);
        registrationId = new JTextField();
        styleField(registrationId);
        registrationEmail = new JTextField();
        styleField(registrationEmail);
        registrationPassword = new JPasswordField();
        styleField(registrationPassword);
        registrationConfirmation = new JPasswordField();
        styleField(registrationConfirmation);
        JPanel fields = stack(8);
        fields.add(fieldGroup("Username", registrationUsername));
        fields.add(fieldGroup(idLabel, registrationId));
        fields.add(fieldGroup("Email (example: name@gmail.com)", registrationEmail));
        fields.add(fieldGroup("Password (at least 6 characters)", registrationPassword));
        fields.add(fieldGroup("Confirm password", registrationConfirmation));
        JPanel buttons = stack(8);
        buttons.add(button("CREATE", "CREATE_ACCOUNT", true));
        buttons.add(button("BACK", "REGISTER_ROLE", false));
        JPanel card = buildCard(480, heading, fields, buttons);
        showScreen("CREATE AN ACCOUNT", roleName, card);
    }

    //Checking of Login Credentials
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        UserAccount account = AccountStore.findByUsername(selectedRole, username);
        if (account == null) {
            showMessage("No account matches that username.", "Account not found");
            return;
        }
        if (account.isLocked()) {
            loginButton.setEnabled(false);
            showMessage("This staff account is disabled after 3 failed attempts. Ask an admin to unlock it.",
                    "Account disabled");
            return;
        }
        String password = new String(passwordField.getPassword());
        if (!account.verifyPassword(password)) {
            if (account.isLocked()) {
                loginButton.setEnabled(false);
                showMessage("Three failed attempts. This staff account is now disabled.",
                        "Account disabled");
            } else {
                int remaining = 3 - account.getFailedAttempts();
                if (account.getRole() == UserAccount.Role.ADMIN) {
                    showMessage("Incorrect password.", "Login failed");
                } else {
                    showMessage("Incorrect password. " + remaining + " attempt(s) remain.",
                            "Login failed");
                }
            }
            passwordField.setText("");
            return;
        }
        dispose();
        if (account.getRole() == UserAccount.Role.ADMIN) {
            new AdminDashboard(account);
        } else {
            new MovieTicketSystem(account);
        }
    }

    //Creating of a New Account
    private void createAccount() {
        String password = new String(registrationPassword.getPassword());
        String confirmation = new String(registrationConfirmation.getPassword());

        if (registrationUsername.getText().trim().length() == 0 || registrationId.getText().trim().length() == 0) {
            showMessage("Enter a username and ID.", "Missing information");
            return;
        }
        if (!UserAccount.isValidEmail(registrationEmail.getText())) {
            showMessage("Enter a valid email address, such as name@gmail.com.", "Invalid email");
            return;
        }
        if (password.length() < 6) {
            showMessage("Use a password with at least 6 characters.", "Password too short");
            return;
        }
        if (!password.equals(confirmation)) {
            showMessage("The passwords do not match.", "Check the passwords");
            return;
        }
        String error = AccountStore.register(selectedRole, registrationUsername.getText(),
                registrationId.getText(), registrationEmail.getText(), password);
        if (error != null) {
            showMessage(error, "Account not created");
        } else {
            showMessage("Account created. You can now log in.", "Account created");
            showLogin(selectedRole);
        }
    }

    //Checking of the Password Reset Code
    private void recoverPassword() {
        String username = JOptionPane.showInputDialog(this,
                "Enter your username to reset the password.", "Forgot password?",
                JOptionPane.QUESTION_MESSAGE);
        if (username == null) {
            return;
        }
        UserAccount account = AccountStore.findByUsername(selectedRole, username);
        if (account == null) {
            showMessage("No account matches that username.", "Account not found");
            return;
        }
        //Blocking of Reset for Locked Accounts
        if (account.isLocked()) {
            showMessage("This account is disabled. Only an admin can unlock it.","Account disabled");
            return;
        }
        JOptionPane.showMessageDialog(this, "A verification code was sent to " + account.getMaskedEmail() + ".", "Password recovery", JOptionPane.INFORMATION_MESSAGE);
        String code = JOptionPane.showInputDialog(this, "Enter the verification code.", "Password recovery", JOptionPane.QUESTION_MESSAGE);
  
        if (code == null) {
            return;
        }
        if (!UserAccount.isValidPasswordResetCode(code)) {
            showMessage("The verification code is incorrect. Use 6 numbers.", "Verification failed");
            return;
        }
        enterNewPassword(account);
    }

    //Saving of the New Password
    private void enterNewPassword(UserAccount account) {
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("New password"));
        form.add(newPassword);
        form.add(new JLabel("Confirm password"));
        form.add(confirmation);
        int answer = JOptionPane.showConfirmDialog(this, form, "Reset password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        String password = new String(newPassword.getPassword());
        String confirm = new String(confirmation.getPassword());
        if (password.length() < 6) {
            showMessage("Use a password with at least 6 characters.", "Password too short");
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("The passwords do not match.", "Check the passwords");
            return;
        }
        account.changePassword(password);
        passwordField.setText("");
        showMessage("Your password has been reset. You can now log in.", "Password updated");
    }

    //Replacing of the Current Screen
    private void showScreen(String title, String subtitle, JPanel body) {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT),
                new EmptyBorder(18, 28, 16, 28)));
        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headingPanel.setBackground(HEADER);
        headingPanel.add(label(title, 22, TEXT, true));
        headingPanel.add(label(subtitle, 13, MUTED, false));
        header.add(headingPanel, BorderLayout.CENTER);
        JLabel brand = label("iScreen", 18, ACCENT, true);
        JButton close = button("X", "CLOSE", false);
        close.setFont(new Font("SansSerif", Font.BOLD, 10));
        close.setPreferredSize(new Dimension(30, 28));
        close.setMargin(new Insets(2, 4, 2, 4));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(HEADER);
        right.add(brand);
        right.add(close);
        header.add(right, BorderLayout.EAST);
        screen.add(header, BorderLayout.NORTH);
        body.setBackground(BG);
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        int availableHeight = getHeight() - header.getPreferredSize().height
                - body.getPreferredSize().height;
        int verticalGap = availableHeight / 2;
        if (verticalGap < 0) {
            verticalGap = 0;
        }
        JPanel topSpace = new JPanel();
        topSpace.setBackground(BG);
        topSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel bottomSpace = new JPanel();
        bottomSpace.setBackground(BG);
        bottomSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel bodyHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bodyHolder.setBackground(BG);
        bodyHolder.add(body);
        center.add(topSpace, BorderLayout.NORTH);
        center.add(bodyHolder, BorderLayout.CENTER);
        center.add(bottomSpace, BorderLayout.SOUTH);
        screen.add(center, BorderLayout.CENTER);
        content.removeAll();
        content.add(screen, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    //Creating of a Vertical Stack Panel
    private JPanel stack(int gap) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, gap));
        panel.setBackground(BG);
        return panel;
    }

    //Grouping of a Label and Its Input Field
    private JPanel fieldGroup(String labelText, JTextField field) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setBackground(BG);
        group.add(label(labelText, 13, MUTED, false), BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    //Building of the Card Layout
    private JPanel buildCard(int width, JPanel heading, JPanel fields, JPanel buttons) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(22, 28, 22, 28)));
        panel.add(heading, BorderLayout.NORTH);
        if (fields != null) {
            panel.add(fields, BorderLayout.CENTER);
        }
        panel.add(buttons, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(width, panel.getPreferredSize().height));
        return panel;
    }

    //Creating of Styled Buttons
    private JButton button(String text, String command, boolean primary) {
        JButton result = new JButton(text);
        result.setActionCommand(command);
        result.addActionListener(this);
        result.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (primary) {
            result.setForeground(new Color(28, 28, 30));
            result.setBackground(ACCENT);
        } else {
            result.setForeground(TEXT);
            result.setBackground(new Color(33, 44, 64));
        }
        result.setFocusPainted(false);
        result.setPreferredSize(new Dimension(350, 38));
        //Hand Cursor for Clickable Buttons
        result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return result;
    }

    //Styling of Input Fields
    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(new Color(33, 44, 64));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(5, 10, 5, 10)));
        //Fixed Height for Input Fields
        field.setPreferredSize(new Dimension(300, 36));
    }

    //Creating of Labels
    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        int style = Font.PLAIN;
        if (bold) {
            style = Font.BOLD;
        }
        result.setFont(new Font("SansSerif", style, size));
        result.setForeground(color);
        return result;
    }

    //Displaying of Messages
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    //Handling of Button Clicks
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("LOGIN_STAFF")) {
            showLogin(UserAccount.Role.STAFF);
        } else if (command.equals("LOGIN_ADMIN")) {
            showLogin(UserAccount.Role.ADMIN);
        } else if (command.equals("REGISTER_ROLE")) {
            showRegistrationRolePicker();
        } else if (command.equals("REGISTER_STAFF")) {
            showRegistration(UserAccount.Role.STAFF);
        } else if (command.equals("REGISTER_ADMIN")) {
            showRegistration(UserAccount.Role.ADMIN);
        } else if (command.equals("ROLE_PICKER")) {
            showRolePicker();
        } else if (command.equals("REGISTER_CURRENT_ROLE")) {
            showRegistration(selectedRole);
        } else if (command.equals("ATTEMPT_LOGIN")) {
            attemptLogin();
        } else if (command.equals("CREATE_ACCOUNT")) {
            createAccount();
        } else if (command.equals("FORGOT_PASSWORD")) {
            recoverPassword();
        } else if (command.equals("CLOSE")) {
            int answer = JOptionPane.showConfirmDialog(this, "Close iScreen?", "Exit",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }
}