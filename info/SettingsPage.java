package ui;

import backend.UserManager;
import backend.User;
import backend.FileManager;
import backend.RecipeManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.prefs.Preferences;
import backend.UserManager.UserOperationResult;
import java.util.List;
import backend.Recipe;
import ui.SignInPage;


public class SettingsPage extends JFrame {
    // User Profile Components
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JPasswordField txtCurrentPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnChangePassword;
    private JButton btnSaveProfile;
    private JButton btnToggleCurrentPassword;
    private JButton btnToggleNewPassword;
    private JButton btnToggleConfirmPassword;
    
    // Application Preferences Components - COMMENTED OUT
    /*
    private JCheckBox chkDarkMode;
    private JCheckBox chkAutoSave;
    private JCheckBox chkConfirmDeletes;
    private JSpinner spnTimerSound;
    private JComboBox<String> cmbTheme;
    private JButton btnResetPreferences;
    */
    
    // Data Management Components
    private JButton btnExportData;
    private JButton btnImportData;
    private JButton btnBackupData;
    private JLabel lblLastBackup;
    
    // Account Management Components
    private JButton btnDeleteAccount;
    private JButton btnDeactivateAccount;
    
    // Navigation Components
    private JButton btnSaveChanges;
    private JButton btnCancel;
    private JLabel lblErrorMessage;
    private JLabel lblSuccessMessage;
    
    // Backend Components
    private UserManager userManager;
    private FileManager fileManager;
    private RecipeManager recipeManager;
    private User currentUser;
    private Preferences userPrefs;
    
    // Password visibility states
    private boolean currentPasswordVisible = false;
    private boolean newPasswordVisible = false;
    private boolean confirmPasswordVisible = false;
    
    // Themes and preferences - COMMENTED OUT
    /*
    private final String[] themes = {"Light", "Dark", "System Default"};
    */
    
    public SettingsPage(int userId) {
        this.userManager = new UserManager();
        this.fileManager = new FileManager();
        this.recipeManager = new RecipeManager();
        this.userPrefs = Preferences.userNodeForPackage(SettingsPage.class);
        
        // Load current user
        loadCurrentUser(userId);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadUserSettings();
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Settings - Recipe Manager");
        setSize(700, 800);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void loadCurrentUser(int userId) {
        try {
            UserOperationResult result = userManager.getUserById(userId);
            if (result.isSuccess()) {
                currentUser = result.getUser();
            } else {
                throw new RuntimeException(result.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to load user data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void initializeComponents() {
        // User Profile Components
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setEnabled(false); // Username cannot be changed
        
        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtCurrentPassword = new JPasswordField(20);
        txtCurrentPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Password visibility toggle buttons
        btnToggleCurrentPassword = createToggleButton();
        btnToggleNewPassword = createToggleButton();
        btnToggleConfirmPassword = createToggleButton();
        
        // Action buttons
        btnChangePassword = createStyledButton("Change Password", new Color(70, 130, 180));
        btnSaveProfile = createStyledButton("Save Profile", new Color(95, 158, 160));
        
        // Application Preferences - COMMENTED OUT
        /*
        chkDarkMode = new JCheckBox("Enable Dark Mode");
        chkDarkMode.setFont(new Font("Arial", Font.PLAIN, 12));
        
        chkAutoSave = new JCheckBox("Auto-save recipes every 5 minutes");
        chkAutoSave.setFont(new Font("Arial", Font.PLAIN, 12));
        
        chkConfirmDeletes = new JCheckBox("Confirm before deleting recipes");
        chkConfirmDeletes.setFont(new Font("Arial", Font.PLAIN, 12));
        
        spnTimerSound = new JSpinner(new SpinnerNumberModel(50, 0, 100, 5));
        spnTimerSound.setFont(new Font("Arial", Font.PLAIN, 12));
        
        
        cmbTheme = new JComboBox<>(themes);
        cmbTheme.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnResetPreferences = createStyledButton("Reset to Defaults", new Color(255, 165, 0));
        */
        
        // Data Management
        btnExportData = createStyledButton("Export Recipes", new Color(34, 139, 34));
        btnImportData = createStyledButton("Import Recipes", new Color(30, 144, 255));
        btnBackupData = createStyledButton("Create Backup", new Color(138, 43, 226));
        
        lblLastBackup = new JLabel("Last backup: Never");
        lblLastBackup.setFont(new Font("Arial", Font.ITALIC, 11));
        lblLastBackup.setForeground(Color.GRAY);
        
        // Account Management
        btnDeleteAccount = createStyledButton("Delete Account", new Color(220, 20, 60));
        btnDeactivateAccount = createStyledButton("Deactivate Account", new Color(255, 69, 0));
        
        // Navigation
        btnSaveChanges = createStyledButton("Save All Changes", new Color(70, 130, 180));
        btnCancel = createStyledButton("Cancel", new Color(128, 128, 128));
        
        // Status labels
        lblErrorMessage = new JLabel(" ");
        lblErrorMessage.setForeground(Color.RED);
        lblErrorMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        lblErrorMessage.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblSuccessMessage = new JLabel(" ");
        lblSuccessMessage.setForeground(new Color(0, 128, 0));
        lblSuccessMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSuccessMessage.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusable(false);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = backgroundColor;
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private JButton createToggleButton() {
        JButton button = new JButton("👁");
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(30, 30));
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setBackground(Color.WHITE);
        return button;
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        
        // Main scroll pane
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // User Profile Panel
        mainPanel.add(createUserProfilePanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Password Change Panel
        mainPanel.add(createPasswordChangePanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Application Preferences Panel - COMMENTED OUT
        /*
        mainPanel.add(createPreferencesPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        */
        
        // Data Management Panel
        mainPanel.add(createDataManagementPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Account Management Panel
        mainPanel.add(createAccountManagementPanel());
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Status messages
        mainPanel.add(lblErrorMessage);
        mainPanel.add(lblSuccessMessage);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnSaveChanges);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel);
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createUserProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "User Profile",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtUsername, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtEmail, gbc);
        
        // Save Profile Button
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnSaveProfile, gbc);
        
        return panel;
    }
    
    private JPanel createPasswordChangePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Change Password",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel currentPasswordPanel = new JPanel(new BorderLayout());
        currentPasswordPanel.add(txtCurrentPassword, BorderLayout.CENTER);
        currentPasswordPanel.add(btnToggleCurrentPassword, BorderLayout.EAST);
        panel.add(currentPasswordPanel, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel newPasswordPanel = new JPanel(new BorderLayout());
        newPasswordPanel.add(txtNewPassword, BorderLayout.CENTER);
        newPasswordPanel.add(btnToggleNewPassword, BorderLayout.EAST);
        panel.add(newPasswordPanel, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
        confirmPasswordPanel.add(txtConfirmPassword, BorderLayout.CENTER);
        confirmPasswordPanel.add(btnToggleConfirmPassword, BorderLayout.EAST);
        panel.add(confirmPasswordPanel, gbc);
        
        // Change Password Button
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(btnChangePassword, gbc);
        
        return panel;
    }
    
    // Application Preferences Panel - COMMENTED OUT
    /*
    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Application Preferences",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbTheme, gbc);
        
        // Date Format
        
        // Timer Sound Volume
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Timer Volume (%):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spnTimerSound, gbc);
        
        // Checkboxes
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(chkDarkMode, gbc);
        
        gbc.gridy = 4;
        panel.add(chkAutoSave, gbc);
        
        gbc.gridy = 5;
        panel.add(chkConfirmDeletes, gbc);
        
        // Reset Button
        gbc.gridy = 6; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 1;
        panel.add(btnResetPreferences, gbc);
        
        return panel;
    }
    */
    
    private JPanel createDataManagementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Data Management",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Export/Import buttons
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(btnExportData, gbc);
        
        gbc.gridx = 1;
        panel.add(btnImportData, gbc);
        
        gbc.gridx = 2;
        panel.add(btnBackupData, gbc);
        
        // Last backup info
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblLastBackup, gbc);
        
        return panel;
    }
    
    private JPanel createAccountManagementPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Account Management",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        panel.add(btnDeactivateAccount);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnDeleteAccount);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Profile management
        btnSaveProfile.addActionListener(e -> saveProfileChanges());
        btnChangePassword.addActionListener(e -> changePassword());
        
        // Password visibility toggles
        btnToggleCurrentPassword.addActionListener(e -> togglePasswordVisibility(txtCurrentPassword, btnToggleCurrentPassword));
        btnToggleNewPassword.addActionListener(e -> togglePasswordVisibility(txtNewPassword, btnToggleNewPassword));
        btnToggleConfirmPassword.addActionListener(e -> togglePasswordVisibility(txtConfirmPassword, btnToggleConfirmPassword));
        
        // Preferences - COMMENTED OUT
        /*
        chkDarkMode.addActionListener(e -> toggleDarkMode(chkDarkMode.isSelected()));
        btnResetPreferences.addActionListener(e -> resetPreferences());
        */
        
        // Data management
        btnExportData.addActionListener(e -> exportData());
        btnImportData.addActionListener(e -> importData());
        btnBackupData.addActionListener(e -> createBackup());
        
        // Account management
        btnDeactivateAccount.addActionListener(e -> deactivateAccount());
        btnDeleteAccount.addActionListener(e -> deleteAccount());
        
        // Navigation
        btnSaveChanges.addActionListener(e -> saveAllChanges());
        btnCancel.addActionListener(e -> dispose());
        
        // Clear messages when user starts interacting
        ActionListener clearMessages = e -> clearMessages();
        txtEmail.addActionListener(clearMessages);
        txtCurrentPassword.addActionListener(clearMessages);
        txtNewPassword.addActionListener(clearMessages);
        txtConfirmPassword.addActionListener(clearMessages);
    }
    
    private void loadUserSettings() {
        if (currentUser != null) {
            txtUsername.setText(currentUser.getUsername());
            txtEmail.setText(currentUser.getEmail());
        }
        
        // Load preferences - COMMENTED OUT
        /*
        chkDarkMode.setSelected(userPrefs.getBoolean("darkMode", false));
        chkAutoSave.setSelected(userPrefs.getBoolean("autoSave", true));
        chkConfirmDeletes.setSelected(userPrefs.getBoolean("confirmDeletes", true));
        spnTimerSound.setValue(userPrefs.getInt("timerVolume", 50));
        cmbTheme.setSelectedItem(userPrefs.get("theme", "Light"));
        */
        
        // Load last backup date
        String lastBackup = userPrefs.get("lastBackup", "Never");
        lblLastBackup.setText("Last backup: " + lastBackup);
        
        // Apply current theme - COMMENTED OUT
        /*
        toggleDarkMode(chkDarkMode.isSelected());
        */
    }
    
    private void saveProfileChanges() {
        try {
            String email = txtEmail.getText().trim();
            
            // Validate email
            if (email.isEmpty() || !email.contains("@")) {
                showError("Please enter a valid email address.");
                txtEmail.requestFocus();
                return;
            }
            
            // Update user object
            currentUser.setEmail(email);
            
            // Save to database
            UserOperationResult result = userManager.updateUser(currentUser);
            
            if (result.isSuccess()) {
                showSuccess("Profile updated successfully!");
            } else {
                showError("Failed to update profile: " + result.getMessage());
            }
            
        } catch (Exception e) {
            showError("Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void changePassword() {
    if (!validatePasswordChange()) {
        return;
    }
    
    // Show loading state (similar to SignInPage pattern)
    setPasswordChangeLoading(true);
    
    // Perform password change in background thread (following SignInPage pattern)
    SwingWorker<UserManager.UserOperationResult, Void> worker = new SwingWorker<UserManager.UserOperationResult, Void>() {
        @Override
        protected UserManager.UserOperationResult doInBackground() throws Exception {
            String currentPassword = new String(txtCurrentPassword.getPassword());
            String newPassword = new String(txtNewPassword.getPassword());
            
            // Verify current password using the same method as SignInPage
            UserManager.UserOperationResult validationResult = userManager.validateLogin(currentUser.getUsername(), currentPassword);
            
            if (!validationResult.isSuccess()) {
                return new UserManager.UserOperationResult(false, "Current password is incorrect.", null);
            }
            
            // Update password
            currentUser.setPassword(newPassword);
            return userManager.updateUser(currentUser);
        }
        
        @Override
        protected void done() {
            setPasswordChangeLoading(false);
            try {
                UserManager.UserOperationResult result = get();
                
                if (result.isSuccess()) {
                    showSuccess("Password changed successfully!");
                    // Clear password fields (following SignInPage pattern)
                    txtCurrentPassword.setText("");
                    txtNewPassword.setText("");
                    txtConfirmPassword.setText("");
                } else {
                    showError(result.getMessage());
                    txtCurrentPassword.requestFocus();
                }
            } catch (Exception e) {
                showError("Error changing password: " + e.getMessage());
                e.printStackTrace();
            }
        }
    };
    
    worker.execute();
}

private boolean validatePasswordChange() {
    clearMessages();
    
    String currentPassword = new String(txtCurrentPassword.getPassword());
    String newPassword = new String(txtNewPassword.getPassword());
    String confirmPassword = new String(txtConfirmPassword.getPassword());
    
    // Current password validation
    if (currentPassword.isEmpty()) {
        showError("Please enter your current password.");
        txtCurrentPassword.requestFocus();
        return false;
    }
    
    // Using the same password length validation as SignInPage
    if (currentPassword.length() < 6) {
        showError("Current password must be at least 6 characters long.");
        txtCurrentPassword.requestFocus();
        return false;
    }
    
    // New password validation (same as SignInPage validation)
    if (newPassword.isEmpty()) {
        showError("Please enter a new password.");
        txtNewPassword.requestFocus();
        return false;
    }
    
    if (newPassword.length() < 6) {
        showError("New password must be at least 6 characters long.");
        txtNewPassword.requestFocus();
        return false;
    }
    
    // Confirm password validation
    if (confirmPassword.isEmpty()) {
        showError("Please confirm your new password.");
        txtConfirmPassword.requestFocus();
        return false;
    }
    
    if (!newPassword.equals(confirmPassword)) {
        showError("New password and confirmation do not match.");
        txtConfirmPassword.requestFocus();
        return false;
    }
    
    // Additional validation: new password should be different from current
    if (currentPassword.equals(newPassword)) {
        showError("New password must be different from current password.");
        txtNewPassword.requestFocus();
        return false;
    }
    
    return true;
}

// Helper method to control loading state (following SignInPage pattern)
private void setPasswordChangeLoading(boolean loading) {
    // Assuming you have these components - adjust names as needed
    if (btnChangePassword != null) btnChangePassword.setEnabled(!loading);
    if (txtCurrentPassword != null) txtCurrentPassword.setEnabled(!loading);
    if (txtNewPassword != null) txtNewPassword.setEnabled(!loading);
    if (txtConfirmPassword != null) txtConfirmPassword.setEnabled(!loading);
    
}
    
    private void togglePasswordVisibility(JPasswordField passwordField, JButton toggleButton) {
        if (passwordField.getEchoChar() == 0) {
            passwordField.setEchoChar('*');
            toggleButton.setText("👁");
        } else {
            passwordField.setEchoChar((char) 0);
            toggleButton.setText("🙈");
        }
    }
    
    // Theme and preferences methods - COMMENTED OUT
    /*
    private void toggleDarkMode(boolean enabled) {
        userPrefs.putBoolean("darkMode", enabled);
        applyThemeToAllComponents(getContentPane(), enabled);
    }

    private void applyThemeToAllComponents(Container container, boolean darkMode) {
        Color bg = darkMode ? new Color(45, 45, 45) : Color.WHITE;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;
    
        container.setBackground(bg);
        for (Component comp : container.getComponents()) {
            comp.setBackground(bg);
            comp.setForeground(fg);
            if (comp instanceof Container) {
                applyThemeToAllComponents((Container) comp, darkMode);
            }
        }
    }
    
    private void resetPreferences() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all preferences to default values?",
            "Confirm Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Reset to defaults
            chkDarkMode.setSelected(false);
            chkAutoSave.setSelected(true);
            chkConfirmDeletes.setSelected(true);
            spnTimerSound.setValue(50);
            cmbTheme.setSelectedIndex(0);
            
            showSuccess("Preferences reset to defaults.");
        }
    }
    */
    
    private void exportData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Recipes");
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
            fileChooser.setSelectedFile(new File("my_recipes.json"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }
                
                boolean success = fileManager.exportUserRecipesToJSON(currentUser.getUserId(), file.getAbsolutePath());
                
                if (success) {
                    showSuccess("Recipes exported successfully to " + file.getName());
                } else {
                    showError("Failed to export recipes.");
                }
            }
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void importData() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Importing recipes will add them to your existing collection. Continue?",
            "Confirm Import",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Import Recipes");
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
            
            int fileResult = fileChooser.showOpenDialog(this);
            if (fileResult == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                List<Recipe> importedRecipes = fileManager.importRecipesFromJSON(file.getAbsolutePath());
                int importedCount = importedRecipes.size();
                
                if (importedCount >= 0) {
                    showSuccess("Successfully imported " + importedCount + " recipe(s).");
                } else {
                    showError("Failed to import recipes. Please check file format.");
                }
            }
        } catch (Exception e) {
            showError("Import failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createBackup() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Create Backup");
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
            
            String defaultName = "recipe_backup_" + java.time.LocalDate.now().toString() + ".json";
            fileChooser.setSelectedFile(new File(defaultName));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    file = new File(file.getAbsolutePath() + ".json");
                }
                
                boolean success = fileManager.exportUserRecipesToJSON(currentUser.getUserId(), file.getAbsolutePath());
                
                if (success) {
                    showSuccess("Backup created successfully!");
                    userPrefs.put("lastBackup", java.time.LocalDateTime.now().toString());
                    lblLastBackup.setText("Last backup: " + java.time.LocalDate.now().toString());
                } else {
                    showError("Failed to create backup.");
                }
            }
        } catch (Exception e) {
            showError("Backup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deactivateAccount() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deactivate your account?\n" +
            "Your data will be preserved but you won't be able to login until reactivation.",
            "Confirm Account Deactivation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Additional confirmation
            String confirmation = JOptionPane.showInputDialog(
                this,
                "Type 'DEACTIVATE' to confirm account deactivation:",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE
            );
            
            if ("DEACTIVATE".equals(confirmation)) {
                try {
                    UserOperationResult success = userManager.deleteUser(currentUser.getUserId());
                    
                    if (success.isSuccess()) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Account deactivated successfully.\nYou will now be logged out.",
                            "Account Deactivated",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Return to sign-in page
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            new SignInPage().setVisible(true);
                        });
                    } else {
                        showError("Failed to deactivate account.");
                    }
                } catch (Exception e) {
                    showError("Error deactivating account: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (confirmation != null) {
                showError("Confirmation text incorrect. Account not deactivated.");
            }
        }
    }
    
    private void deleteAccount() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "WARNING: This will permanently delete your account and ALL your recipes!\n" +
            "This action cannot be undone. Are you absolutely sure?",
            "Confirm Account Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Additional confirmation with password
            String password = JOptionPane.showInputDialog(
                this,
                "Enter your password to confirm account deletion:",
                "Password Confirmation",
                JOptionPane.WARNING_MESSAGE
            );
            
            if (password != null && !password.trim().isEmpty()) {
                try {
                    UserOperationResult validationResult = userManager.validateLogin(currentUser.getUsername(), password);
                    
                    if (!validationResult.isSuccess()) {
                        showError("Incorrect password. Account not deleted.");
                        return;
                    }
                    
                    // Final confirmation
                    String finalConfirmation = JOptionPane.showInputDialog(
                        this,
                        "Type 'DELETE MY ACCOUNT' to permanently delete your account:",
                        "Final Confirmation",
                        JOptionPane.ERROR_MESSAGE
                    );
                    
                    if ("DELETE MY ACCOUNT".equals(finalConfirmation)) {
                        UserOperationResult deleteResult = userManager.deleteUser(currentUser.getUserId());
                        
                        if (deleteResult.isSuccess()) {
                            JOptionPane.showMessageDialog(
                                this,
                                "Account deleted successfully.\nThank you for using Recipe Manager.",
                                "Account Deleted",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            // Close application or return to sign-in
                            dispose();
                            System.exit(0);
                        } else {
                            showError("Failed to delete account.");
                        }
                    } else if (finalConfirmation != null) {
                        showError("Confirmation text incorrect. Account not deleted.");
                    }
                } catch (Exception e) {
                    showError("Error deleting account: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void saveAllChanges() {
        try {
            // Save profile changes
            String email = txtEmail.getText().trim();
            if (!email.equals(currentUser.getEmail())) {
                if (email.isEmpty() || !email.contains("@")) {
                    showError("Please enter a valid email address.");
                    return;
                }
                currentUser.setEmail(email);
                userManager.updateUser(currentUser);
            }
            
            // Save preferences - COMMENTED OUT
            /*
            userPrefs.putBoolean("darkMode", chkDarkMode.isSelected());
            userPrefs.putBoolean("autoSave", chkAutoSave.isSelected());
            userPrefs.putBoolean("confirmDeletes", chkConfirmDeletes.isSelected());
            userPrefs.putInt("timerVolume", (Integer) spnTimerSound.getValue());
            userPrefs.put("theme", (String) cmbTheme.getSelectedItem());
            */
            
            // Apply theme if changed - COMMENTED OUT
            /*
            toggleDarkMode(chkDarkMode.isSelected());
            */

            showSuccess("All changes saved successfully!");
            
        } catch (Exception e) {
            showError("Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        lblErrorMessage.setText(message);
        lblSuccessMessage.setText(" ");
        
        // Auto-clear after 10 seconds
        Timer timer = new Timer(10000, e -> clearMessages());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showSuccess(String message) {
        lblSuccessMessage.setText(message);
        lblErrorMessage.setText(" ");
        
        // Auto-clear after 5 seconds
        Timer timer = new Timer(5000, e -> clearMessages());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void clearMessages() {
        lblErrorMessage.setText(" ");
        lblSuccessMessage.setText(" ");
    }
}