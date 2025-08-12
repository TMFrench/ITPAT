package backend;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;
import java.util.Arrays;

/**
 * UserManager - Handles all user-related database operations
 * Implements secure password hashing, comprehensive validation, and CRUD operations
 * 
 * @author Recipe Manager Team
 * @version 1.1
 */
public class UserManager {
    
    // Database connector instance
    private final DatabaseConnector dbConnector;
    
    // Logging
    private static final Logger logger = Logger.getLogger(UserManager.class.getName());
    
    // Password security constants
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 100000; // PBKDF2 iterations
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    
    // Validation patterns - Updated to match database schema
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,50}$" // Updated to match database limit of 50 characters
    );
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // SQL statements - Updated to match actual database schema
    private static final String INSERT_USER = 
        "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT userId, username, password, email, createdAt FROM users WHERE userId = ?";
    
    private static final String SELECT_USER_BY_USERNAME = 
        "SELECT userId, username, password, email, createdAt FROM users WHERE username = ?";
   
    private static final String UPDATE_USER = 
        "UPDATE users SET username = ?, password = ?, email = ? WHERE userId = ?";
    
    private static final String DELETE_USER = 
        "DELETE FROM users WHERE userId = ?";
    
    private static final String CHECK_USERNAME_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE username = ? AND userId != ?";
    
    private static final String CHECK_EMAIL_EXISTS = 
        "SELECT COUNT(*) FROM users WHERE email = ? AND userId != ?";
    
    /**
     * Constructor - Initialize UserManager with database connector
     */
    public UserManager() {
        this.dbConnector = DatabaseConnector.getInstance();
        logger.info("UserManager initialized successfully");
    }
    
    /**
     * Add a new user to the database with encrypted password
     * 
     * @param user User object containing user information
     * @return UserOperationResult containing success status and messages
     * @throws IllegalArgumentException if user data is invalid
     */
    public UserOperationResult addUser(User user) {
        logger.info("Attempting to add new user: " + (user != null ? user.getUsername() : "null"));
        
        try {
            // Validate user input
            UserOperationResult validationResult = validateUserInput(user, true);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            // Check if username already exists
            if (usernameExists(user.getUsername(), -1)) {
                String errorMsg = "Username '" + user.getUsername() + "' already exists";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Check if email already exists
            if (emailExists(user.getEmail(), -1)) {
                String errorMsg = "Email '" + user.getEmail() + "' already exists";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Encrypt password
            String hashedPassword = encryptPassword(user.getPassword());
            if (hashedPassword == null) {
                String errorMsg = "Failed to encrypt password";
                logger.severe(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Insert user into database
            try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getUsername().trim());
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, user.getEmail().trim().toLowerCase());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Get generated user ID
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setUserId(generatedKeys.getInt(1));
                        }
                    }
                    
                    String successMsg = "User '" + user.getUsername() + "' added successfully with ID: " + user.getUserId();
                    logger.info(successMsg);
                    return new UserOperationResult(true, successMsg, user);
                } else {
                    String errorMsg = "Failed to insert user into database";
                    logger.warning(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = "Database error while adding user: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Retrieve user by their unique ID
     * 
     * @param id User ID to search for
     * @return UserOperationResult containing user data if found
     * @throws IllegalArgumentException if ID is invalid
     */
    public UserOperationResult getUserById(int id) {
        logger.info("Retrieving user by ID: " + id);
        
        if (id <= 0) {
            String errorMsg = "Invalid user ID: " + id + ". ID must be positive.";
            logger.warning(errorMsg);
            return new UserOperationResult(false, errorMsg);
        }
        
        try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(SELECT_USER_BY_ID)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = createUserFromResultSet(rs);
                    String successMsg = "User found with ID: " + id;
                    logger.info(successMsg);
                    return new UserOperationResult(true, successMsg, user);
                } else {
                    String errorMsg = "No user found with ID: " + id;
                    logger.info(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = "Database error while retrieving user by ID " + id + ": " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Retrieve user by their username
     * 
     * @param username Username to search for
     * @return UserOperationResult containing user data if found
     * @throws IllegalArgumentException if username is invalid
     */
    public UserOperationResult getUserByUsername(String username) {
        logger.info("Retrieving user by username: " + username);
        
        if (!isValidUsername(username)) {
            String errorMsg = "Invalid username format: " + username;
            logger.warning(errorMsg);
            return new UserOperationResult(false, errorMsg);
        }
        
        try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(SELECT_USER_BY_USERNAME)) {
            pstmt.setString(1, username.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = createUserFromResultSet(rs);
                    String successMsg = "User found with username: " + username;
                    logger.info(successMsg);
                    return new UserOperationResult(true, successMsg, user);
                } else {
                    String errorMsg = "No user found with username: " + username;
                    logger.info(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = "Database error while retrieving user by username " + username + ": " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Update existing user information
     * 
     * @param user User object with updated information (must include valid user ID)
     * @return UserOperationResult indicating success or failure
     * @throws IllegalArgumentException if user data is invalid
     */
    public UserOperationResult updateUser(User user) {
        logger.info("Attempting to update user: " + (user != null ? user.getUsername() : "null"));
        
        try {
            // Validate user input
            UserOperationResult validationResult = validateUserInput(user, false);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
            
            if (user.getUserId() <= 0) {
                String errorMsg = "Invalid user ID for update: " + user.getUserId();
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Check if user exists
            UserOperationResult existingUser = getUserById(user.getUserId());
            if (!existingUser.isSuccess()) {
                String errorMsg = "Cannot update: User with ID " + user.getUserId() + " does not exist";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Check if new username conflicts with existing users
            if (usernameExists(user.getUsername(), user.getUserId())) {
                String errorMsg = "Username '" + user.getUsername() + "' is already taken by another user";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Check if new email conflicts with existing users
            if (emailExists(user.getEmail(), user.getUserId())) {
                String errorMsg = "Email '" + user.getEmail() + "' is already taken by another user";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            // Encrypt password if it's being updated
            String passwordToStore = user.getPassword();
            if (passwordToStore != null && !passwordToStore.trim().isEmpty()) {
                passwordToStore = encryptPassword(user.getPassword());
                if (passwordToStore == null) {
                    String errorMsg = "Failed to encrypt new password";
                    logger.severe(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            } else {
                // Keep existing password if no new password provided
                passwordToStore = existingUser.getUser().getPassword();
            }
            
            // Update user in database
            try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(UPDATE_USER)) {
                pstmt.setString(1, user.getUsername().trim());
                pstmt.setString(2, passwordToStore);
                pstmt.setString(3, user.getEmail().trim().toLowerCase());
                pstmt.setInt(4, user.getUserId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    String successMsg = "User '" + user.getUsername() + "' updated successfully";
                    logger.info(successMsg);
                    return new UserOperationResult(true, successMsg, user);
                } else {
                    String errorMsg = "Failed to update user in database";
                    logger.warning(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = "Database error while updating user: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Delete user by their ID
     * This will also delete all associated recipes and ingredients due to foreign key constraints
     * 
     * @param id User ID to delete
     * @return UserOperationResult indicating success or failure
     * @throws IllegalArgumentException if ID is invalid
     */
    public UserOperationResult deleteUser(int id) {
        logger.info("Attempting to delete user with ID: " + id);
        
        if (id <= 0) {
            String errorMsg = "Invalid user ID for deletion: " + id;
            logger.warning(errorMsg);
            return new UserOperationResult(false, errorMsg);
        }
        
        try {
            // Check if user exists before attempting deletion
            UserOperationResult existingUser = getUserById(id);
            if (!existingUser.isSuccess()) {
                String errorMsg = "Cannot delete: User with ID " + id + " does not exist";
                logger.warning(errorMsg);
                return new UserOperationResult(false, errorMsg);
            }
            
            String username = existingUser.getUser().getUsername();
            
            // Delete user from database
            try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(DELETE_USER)) {
                pstmt.setInt(1, id);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    String successMsg = "User '" + username + "' (ID: " + id + ") deleted successfully";
                    logger.info(successMsg);
                    return new UserOperationResult(true, successMsg);
                } else {
                    String errorMsg = "Failed to delete user from database";
                    logger.warning(errorMsg);
                    return new UserOperationResult(false, errorMsg);
                }
            }
            
        } catch (SQLException e) {
            String errorMsg = "Database error while deleting user ID " + id + ": " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Encrypt password using PBKDF2 with SHA-256
     * Generates a random salt for each password
     * 
     * @param password Plain text password to encrypt
     * @return Encrypted password string with salt, or null if encryption fails
     */
    public String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            logger.warning("Cannot encrypt null or empty password");
            return null;
        }
        
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Generate hash using PBKDF2
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            // Clear the password from memory
            spec.clearPassword();
            
            // Encode salt and hash to Base64 and combine
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hash);
            
            String result = encodedSalt + ":" + encodedHash;
            logger.info("Password encrypted successfully");
            return result;
            
        } catch (NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            logger.log(Level.SEVERE, "Failed to encrypt password", e);
            return null;
        }
    }
    
    /**
     * Validate user login credentials
     * 
     * @param username Username for login
     * @param password Plain text password for login
     * @return UserOperationResult containing user data if login is successful
     */
    public UserOperationResult validateLogin(String username, String password) {
        logger.info("Validating login for username: " + username);
        
        // Validate input parameters
        if (!isValidUsername(username)) {
            String errorMsg = "Invalid username format";
            logger.warning(errorMsg + ": " + username);
            return new UserOperationResult(false, errorMsg);
        }
        
        if (password == null || password.isEmpty()) {
            String errorMsg = "Password cannot be empty";
            logger.warning(errorMsg);
            return new UserOperationResult(false, errorMsg);
        }
        
        // Get user from database
        UserOperationResult userResult = getUserByUsername(username);
        if (!userResult.isSuccess()) {
            String errorMsg = "Invalid username or password";
            logger.warning("Login failed for username: " + username + " - user not found");
            return new UserOperationResult(false, errorMsg);
        }
        
        User user = userResult.getUser();
        String storedPassword = user.getPassword();
        
        // Verify password
        if (verifyPassword(password, storedPassword)) {
            String successMsg = "Login successful for user: " + username;
            logger.info(successMsg);
            // Clear password from user object for security
            user.password = null;
            return new UserOperationResult(true, successMsg, user);
        } else {
            String errorMsg = "Invalid username or password";
            logger.warning("Login failed for username: " + username + " - incorrect password");
            return new UserOperationResult(false, errorMsg);
        }
    }
    
    /**
     * Verify a plain text password against a stored encrypted password
     * 
     * @param password Plain text password to verify
     * @param storedPassword Stored encrypted password (salt:hash format)
     * @return true if password matches, false otherwise
     */
    private boolean verifyPassword(String password, String storedPassword) {
        if (password == null || storedPassword == null) {
            return false;
        }
        
        try {
            // Split stored password into salt and hash
            String[] parts = storedPassword.split(":");
            if (parts.length != 2) {
                logger.warning("Invalid stored password format");
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);
            
            // Generate hash from provided password using the same salt
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();
            
            // Clear the password from memory
            spec.clearPassword();
            
            // Compare hashes using constant time comparison
            return Arrays.equals(storedHash, testHash);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Create User object from ResultSet
     * 
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if data extraction fails
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // Keep encrypted password for updates
        user.setEmail(rs.getString("email"));
        
        // Set createdAt timestamp if available
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Validate user input data
     * 
     * @param user User object to validate
     * @param isNewUser true if this is a new user (password required)
     * @return UserOperationResult indicating validation success or failure
     */
    private UserOperationResult validateUserInput(User user, boolean isNewUser) {
        if (user == null) {
            String errorMsg = "User object cannot be null";
            return new UserOperationResult(false, errorMsg);
        }
        
        // Validate username
        if (!isValidUsername(user.getUsername())) {
            String errorMsg = "Invalid username. Must be 3-50 characters, alphanumeric and underscores only";
            return new UserOperationResult(false, errorMsg);
        }
        
        // Validate email
        if (!isValidEmail(user.getEmail())) {
            String errorMsg = "Invalid email format or exceeds 100 characters";
            return new UserOperationResult(false, errorMsg);
        }
        
        // Validate password (only required for new users)
        if (isNewUser) {
            if (!isValidPassword(user.getPassword())) {
                String errorMsg = "Invalid password. Must be " + MIN_PASSWORD_LENGTH + 
                                "-" + MAX_PASSWORD_LENGTH + " characters long";
                return new UserOperationResult(false, errorMsg);
            }
        }
        
        return new UserOperationResult(true, "Validation passed");
    }
    
    /**
     * Validate username format and length
     * 
     * @param username Username to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidUsername(String username) {
        return username != null && 
               username.trim().length() >= 3 && 
               username.trim().length() <= 50 && // Updated to match database limit
               USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    /**
     * Validate email format and length
     * 
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && 
               email.trim().length() <= 100 && // Updated to match database limit
               EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPassword(String password) {
        return password != null && 
               password.length() >= MIN_PASSWORD_LENGTH && 
               password.length() <= MAX_PASSWORD_LENGTH;
    }
    
    /**
     * Check if username already exists for a different user
     * 
     * @param username Username to check
     * @param excludeUserId User ID to exclude from check (-1 for no exclusion)
     * @return true if username exists, false otherwise
     */
    private boolean usernameExists(String username, int excludeUserId) {
        try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(CHECK_USERNAME_EXISTS)) {
            pstmt.setString(1, username.trim());
            pstmt.setInt(2, excludeUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking username existence", e);
            return true; // Assume it exists to be safe
        }
    }
    
    /**
     * Check if email already exists for a different user
     * 
     * @param email Email to check
     * @param excludeUserId User ID to exclude from check (-1 for no exclusion)
     * @return true if email exists, false otherwise
     */
    private boolean emailExists(String email, int excludeUserId) {
        try (PreparedStatement pstmt = dbConnector.connect().prepareStatement(CHECK_EMAIL_EXISTS)) {
            pstmt.setString(1, email.trim().toLowerCase());
            pstmt.setInt(2, excludeUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error checking email existence", e);
            return true; // Assume it exists to be safe
        }
    }
    
    /**
     * Inner class for returning operation results with detailed information
     */
    public static class UserOperationResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        public UserOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.user = null;
        }
        
        public UserOperationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        
        @Override
        public String toString() {
            return String.format("UserOperationResult{success=%s, message='%s', user=%s}", 
                               success, message, user != null ? user.getUsername() : "null");
        }
    }
}