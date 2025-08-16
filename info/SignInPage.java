package ui;

import backend.UserManager;
import backend.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SignInPage extends JPanel {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnTogglePassword;
    private JLabel lblError;
    private JProgressBar loadingBar;
    private UserManager userManager;
    private boolean passwordVisible = false;
    private SignInPageListener listener;
    
    public interface SignInPageListener {
        void onLoginSuccess(int userId);
        void onRegistrationRequested();
    }
    
    public SignInPage() {
        this.userManager = new UserManager();
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }
    
    public SignInPage(UserManager userManager) {
        this.userManager = userManager != null ? userManager : new UserManager();
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }
    
    public void setSignInPageListener(SignInPageListener listener) {
        this.listener = listener;
    }
    
    private void initializeComponents() {
        // Username field
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Password field
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Password visibility toggle button
        btnTogglePassword = new JButton("👁");
        btnTogglePassword.setFont(new Font("Arial", Font.PLAIN, 12));
        btnTogglePassword.setPreferredSize(new Dimension(30, 30));
        btnTogglePassword.setFocusable(false);
        
        // Login button
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(120, 40));
        btnLogin.setFocusable(false);
        btnLogin.setBorderPainted(false);
        
        // Register button
        btnRegister = new JButton("Register");
        btnRegister.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRegister.setBackground(new Color(95, 158, 160));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(120, 40));
        btnRegister.setFocusable(false);
        btnRegister.setBorderPainted(false);
        
        // Error label
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Arial", Font.PLAIN, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Loading bar
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setStringPainted(true);
        loadingBar.setString("Authenticating...");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("Recipe Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(titleLabel, gbc);
        
        // Username label
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(lblUsername, gbc);
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(txtUsername, gbc);
        
        // Password label
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(lblPassword, gbc);
        
        // Password panel (field + toggle button)
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        passwordPanel.add(btnTogglePassword, BorderLayout.EAST);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(passwordPanel, gbc);
        
        // Error label
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(lblError, gbc);
        
        // Loading bar
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(loadingBar, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void setupEventListeners() {
        // Login button action
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        // Register button action
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        // Password visibility toggle
        btnTogglePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePasswordVisibility();
            }
        });
        
        // Enter key press for login
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {}
            
            @Override
            public void keyTyped(KeyEvent e) {}
        };
        
        txtUsername.addKeyListener(enterKeyListener);
        txtPassword.addKeyListener(enterKeyListener);
        
        // Clear error message when user starts typing
        ActionListener clearError = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearErrorMessage();
            }
        };
        
        txtUsername.addActionListener(clearError);
        txtPassword.addActionListener(clearError);
    }
    
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Input validation
        if (!validateInput(username, password)) {
            return;
        }
        
        // Show loading indicator
        setLoading(true);
        
        // Authenticate user in background thread
        SwingWorker<UserManager.UserOperationResult, Void> worker = new SwingWorker<UserManager.UserOperationResult, Void>() {
            @Override
            protected UserManager.UserOperationResult doInBackground() throws Exception {
                // Simulate network delay (remove in production)
                Thread.sleep(1000);
                return userManager.validateLogin(username, password);
            }
            
            @Override
            protected void done() {
                setLoading(false);
                try {
                    UserManager.UserOperationResult result = get();
                    if (result.isSuccess()) {
                        // Login successful
                        clearErrorMessage();
                        if (listener != null) {
                            listener.onLoginSuccess(result.getUser().getUserId());
                        } else {
                            // Fallback: try to open HomePage directly
                            openHomePage(result.getUser().getUserId());
                        }
                    } else {
                        showError(result.getMessage());
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                    }
                } catch (Exception e) {
                    showError("Authentication failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleRegister() {
        // Get the parent window correctly
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        
        // Create registration dialog
        RegistrationDialog registrationDialog;
        if (parentWindow instanceof JFrame) {
            registrationDialog = new RegistrationDialog((JFrame) parentWindow, userManager);
        } else if (parentWindow instanceof JDialog) {
            registrationDialog = new RegistrationDialog((JDialog) parentWindow, userManager);
        } else {
            // Create a temporary parent frame
            JFrame tempFrame = new JFrame();
            tempFrame.setLocationRelativeTo(this);
            registrationDialog = new RegistrationDialog(tempFrame, userManager);
        }
        
        // Show the dialog
        registrationDialog.setVisible(true);
        
        // Handle registration result
        if (registrationDialog.isRegistrationSuccessful()) {
            txtUsername.setText("");
            txtPassword.setText("");
            showSuccess("Registration successful! Please log in with your new account.");
            txtUsername.requestFocus();
        }
        
        // Notify listener if available
        if (listener != null) {
            listener.onRegistrationRequested();
        }
    }
    
    private boolean validateInput(String username, String password) {
        clearErrorMessage();
        
        if (username.isEmpty()) {
            showError("Please enter a username");
            txtUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            txtUsername.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Please enter a password");
            txtPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            txtPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("🙈");
        } else {
            txtPassword.setEchoChar('*');
            btnTogglePassword.setText("👁");
        }
    }
    
    private void setLoading(boolean loading) {
        loadingBar.setVisible(loading);
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        txtUsername.setEnabled(!loading);
        txtPassword.setEnabled(!loading);
        btnTogglePassword.setEnabled(!loading);
    }
    
    private void showError(String message) {
        lblError.setForeground(Color.RED);
        lblError.setText(message);
    }
    
    private void showSuccess(String message) {
        lblError.setForeground(new Color(0, 128, 0));
        lblError.setText(message);
    }
    
    private void clearErrorMessage() {
        lblError.setText(" ");
    }
    
    // Fallback method for direct HomePage opening (when no listener is set)
    private void openHomePage(int userId) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    HomePage homePage = new HomePage(userId);
                    
                    // Find the parent window and close it
                    Window parentWindow = SwingUtilities.getWindowAncestor(SignInPage.this);
                    if (parentWindow instanceof JFrame) {
                        parentWindow.setVisible(false);
                        parentWindow.dispose();
                    }
                    
                    // Show home page
                    homePage.setVisible(true);
                } catch (Exception e) {
                    showError("Failed to open home page: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    // Public methods for external control
    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        clearErrorMessage();
    }
    
    public void focusUsernameField() {
        txtUsername.requestFocus();
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
        btnTogglePassword.setEnabled(enabled);
    }
    
    // Corrected Registration Dialog
    private class RegistrationDialog extends JDialog {
        private JTextField txtRegUsername;
        private JTextField txtRegEmail;
        private JPasswordField txtRegPassword;
        private JPasswordField txtConfirmPassword;
        private JButton btnSubmit;
        private JButton btnCancel;
        private JLabel lblRegError;
        private JProgressBar regLoadingBar;
        private UserManager userManager;
        private boolean registrationSuccessful = false;
        
        // Constructor for JFrame parent
        public RegistrationDialog(JFrame parent, UserManager userManager) {
            super(parent, "Register New User", true);
            this.userManager = userManager;
            initializeDialog();
        }
        
        // Constructor for JDialog parent
        public RegistrationDialog(JDialog parent, UserManager userManager) {
            super(parent, "Register New User", true);
            this.userManager = userManager;
            initializeDialog();
        }
        
        private void initializeDialog() {
            initializeRegistrationComponents();
            setupRegistrationLayout();
            setupRegistrationEventListeners();
            
            setSize(400, 400);
            setLocationRelativeTo(getParent());
            setResizable(false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        private void initializeRegistrationComponents() {
            // Text fields with improved styling
            txtRegUsername = new JTextField(20);
            txtRegUsername.setFont(new Font("Arial", Font.PLAIN, 12));
            txtRegUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            txtRegEmail = new JTextField(20);
            txtRegEmail.setFont(new Font("Arial", Font.PLAIN, 12));
            txtRegEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            txtRegPassword = new JPasswordField(20);
            txtRegPassword.setFont(new Font("Arial", Font.PLAIN, 12));
            txtRegPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            txtConfirmPassword = new JPasswordField(20);
            txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 12));
            txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            
            // Buttons
            btnSubmit = new JButton("Register");
            btnSubmit.setFont(new Font("Arial", Font.BOLD, 12));
            btnSubmit.setBackground(new Color(95, 158, 160));
            btnSubmit.setForeground(Color.WHITE);
            btnSubmit.setBorderPainted(false);
            btnSubmit.setPreferredSize(new Dimension(100, 35));
            btnSubmit.setFocusable(false);
            
            btnCancel = new JButton("Cancel");
            btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
            btnCancel.setBackground(Color.GRAY);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setBorderPainted(false);
            btnCancel.setPreferredSize(new Dimension(100, 35));
            btnCancel.setFocusable(false);
            
            // Error label
            lblRegError = new JLabel(" ");
            lblRegError.setForeground(Color.RED);
            lblRegError.setFont(new Font("Arial", Font.PLAIN, 11));
            lblRegError.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Loading bar
            regLoadingBar = new JProgressBar();
            regLoadingBar.setIndeterminate(true);
            regLoadingBar.setVisible(false);
            regLoadingBar.setStringPainted(true);
            regLoadingBar.setString("Creating account...");
        }
        
        private void setupRegistrationLayout() {
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);
            
            // Main panel
            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Title
            JLabel titleLabel = new JLabel("Create New Account");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setForeground(new Color(95, 158, 160));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 20, 0);
            gbc.anchor = GridBagConstraints.CENTER;
            mainPanel.add(titleLabel, gbc);
            
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            
            // Username
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 5, 0);
            JLabel lblUsername = new JLabel("Username:");
            lblUsername.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(lblUsername, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(txtRegUsername, gbc);
            
            // Email
            gbc.gridx = 0; gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 5, 0);
            JLabel lblEmail = new JLabel("Email:");
            lblEmail.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(lblEmail, gbc);
            
            gbc.gridx = 0; gbc.gridy = 4;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(txtRegEmail, gbc);
            
            // Password
            gbc.gridx = 0; gbc.gridy = 5;
            gbc.insets = new Insets(0, 0, 5, 0);
            JLabel lblPassword = new JLabel("Password:");
            lblPassword.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(lblPassword, gbc);
            
            gbc.gridx = 0; gbc.gridy = 6;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(txtRegPassword, gbc);
            
            // Confirm Password
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.insets = new Insets(0, 0, 5, 0);
            JLabel lblConfirmPassword = new JLabel("Confirm Password:");
            lblConfirmPassword.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(lblConfirmPassword, gbc);
            
            gbc.gridx = 0; gbc.gridy = 8;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(txtConfirmPassword, gbc);
            
            // Error label
            gbc.gridx = 0; gbc.gridy = 9;
            gbc.insets = new Insets(0, 0, 10, 0);
            mainPanel.add(lblRegError, gbc);
            
            // Loading bar
            gbc.gridx = 0; gbc.gridy = 10;
            gbc.insets = new Insets(0, 0, 15, 0);
            mainPanel.add(regLoadingBar, gbc);
            
            add(mainPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(btnSubmit);
            buttonPanel.add(btnCancel);
            
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void setupRegistrationEventListeners() {
            btnSubmit.addActionListener(e -> handleRegistration());
            btnCancel.addActionListener(e -> dispose());
            
            // Enter key for registration
            KeyListener enterListener = new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        handleRegistration();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dispose();
                    }
                }
                @Override public void keyReleased(KeyEvent e) {}
                @Override public void keyTyped(KeyEvent e) {}
            };
            
            txtRegUsername.addKeyListener(enterListener);
            txtRegEmail.addKeyListener(enterListener);
            txtRegPassword.addKeyListener(enterListener);
            txtConfirmPassword.addKeyListener(enterListener);
            
            // Clear error message when user starts typing
            KeyListener clearErrorListener = new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    SwingUtilities.invokeLater(() -> clearRegistrationError());
                }
                @Override public void keyReleased(KeyEvent e) {}
                @Override public void keyTyped(KeyEvent e) {}
            };
            
            txtRegUsername.addKeyListener(clearErrorListener);
            txtRegEmail.addKeyListener(clearErrorListener);
            txtRegPassword.addKeyListener(clearErrorListener);
            txtConfirmPassword.addKeyListener(clearErrorListener);
        }
        
        private void handleRegistration() {
            String username = txtRegUsername.getText().trim();
            String email = txtRegEmail.getText().trim();
            String password = new String(txtRegPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            
            // Validation
            if (!validateRegistrationInput(username, email, password, confirmPassword)) {
                return;
            }
            
            // Show loading
            setRegistrationLoading(true);
            
            // Perform registration in background thread
            SwingWorker<UserManager.UserOperationResult, Void> worker = new SwingWorker<UserManager.UserOperationResult, Void>() {
                @Override
                protected UserManager.UserOperationResult doInBackground() throws Exception {
                    // Simulate network delay
                    Thread.sleep(1500);
                    User newUser = new User(0, username, password, email);
                    return userManager.addUser(newUser);
                }
                
                @Override
                protected void done() {
                    setRegistrationLoading(false);
                    try {
                        UserManager.UserOperationResult result = get();
                        if (result.isSuccess()) {
                            registrationSuccessful = true;
                            dispose();
                        } else {
                            showRegistrationError(result.getMessage());
                        }
                    } catch (Exception e) {
                        showRegistrationError("Registration failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            
            worker.execute();
        }
        
        private boolean validateRegistrationInput(String username, String email, String password, String confirmPassword) {
            clearRegistrationError();
            
            if (username.isEmpty()) {
                showRegistrationError("Please enter a username");
                txtRegUsername.requestFocus();
                return false;
            }
            
            if (username.length() < 3) {
                showRegistrationError("Username must be at least 3 characters long");
                txtRegUsername.requestFocus();
                return false;
            }
            
            if (email.isEmpty()) {
                showRegistrationError("Please enter an email address");
                txtRegEmail.requestFocus();
                return false;
            }
            
            if (!isValidEmail(email)) {
                showRegistrationError("Please enter a valid email address");
                txtRegEmail.requestFocus();
                return false;
            }
            
            if (password.isEmpty()) {
                showRegistrationError("Please enter a password");
                txtRegPassword.requestFocus();
                return false;
            }
            
            if (password.length() < 6) {
                showRegistrationError("Password must be at least 6 characters long");
                txtRegPassword.requestFocus();
                return false;
            }
            
            if (confirmPassword.isEmpty()) {
                showRegistrationError("Please confirm your password");
                txtConfirmPassword.requestFocus();
                return false;
            }
            
            if (!password.equals(confirmPassword)) {
                showRegistrationError("Passwords do not match");
                txtConfirmPassword.requestFocus();
                return false;
            }
            
            return true;
        }
        
        private boolean isValidEmail(String email) {
            return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
        }
        
        private void setRegistrationLoading(boolean loading) {
            regLoadingBar.setVisible(loading);
            btnSubmit.setEnabled(!loading);
            btnCancel.setEnabled(!loading);
            txtRegUsername.setEnabled(!loading);
            txtRegEmail.setEnabled(!loading);
            txtRegPassword.setEnabled(!loading);
            txtConfirmPassword.setEnabled(!loading);
        }
        
        private void showRegistrationError(String message) {
            lblRegError.setForeground(Color.RED);
            lblRegError.setText(message);
        }
        
        private void clearRegistrationError() {
            lblRegError.setText(" ");
        }
        
        public boolean isRegistrationSuccessful() {
            return registrationSuccessful;
        }
    }
}