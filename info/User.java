package backend;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User - Model class representing a user in the Recipe Manager system
 * Contains user authentication and profile information
 * 
 * @author Recipe Manager Team
 * @version 1.1
 */
public class User {
    
    // Primary key
    private int userId;
    
    // User credentials and profile
    private String username;
    protected String password; // Will be encrypted when stored
    private String email;
    
    // Timestamps - Note: updatedAt is not in the database schema
    private LocalDateTime createdAt;
    
    /**
     * Default constructor
     */
    public User() {
        // Default constructor for creating empty User objects
    }
    
    /**
     * Constructor with basic user information
     * 
     * @param username User's login name (max 50 characters)
     * @param password User's password (will be encrypted, max 255 characters)
     * @param email User's email address (max 100 characters)
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    /**
     * Constructor with all fields
     * 
     * @param userId User's unique identifier
     * @param username User's login name (max 50 characters)
     * @param password User's password (will be encrypted, max 255 characters)
     * @param email User's email address (max 100 characters)
     * @param createdAt Creation timestamp
     */
    public User(int userId, String username, String password, String email, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    /**
     * Constructor with all fields except timestamps
     * 
     * @param userId User's unique identifier
     * @param username User's login name (max 50 characters)
     * @param password User's password (will be encrypted, max 255 characters)
     * @param email User's email address (max 100 characters)
     */
    public User(int userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    // Getters
    
    /**
     * Get user's unique identifier
     * 
     * @return user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Get user's login name
     * 
     * @return username (max 50 characters)
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get user's password (encrypted when retrieved from database)
     * 
     * @return password (max 255 characters when encrypted)
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Get user's email address
     * 
     * @return email (max 100 characters)
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Get timestamp when user was created
     * 
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    
    /**
     * Set user's unique identifier
     * 
     * @param userId user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    /**
     * Set user's login name
     * 
     * @param username username (max 50 characters, must be unique)
     * @throws IllegalArgumentException if username is null, empty, or exceeds 50 characters
     */
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("Username cannot exceed 50 characters");
        }
        this.username = username.trim();
    }
    
    /**
     * Set user's password
     * 
     * @param password password (will be encrypted when stored, max 255 characters when encrypted)
     * @throws IllegalArgumentException if password is null or empty
     */
    public void setPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        this.password = password;
    }
    
    /**
     * Set user's email address
     * 
     * @param email valid email address (max 100 characters, must be unique)
     * @throws IllegalArgumentException if email is null, empty, exceeds 100 characters, or invalid format
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email cannot exceed 100 characters");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim().toLowerCase();
    }
    
    /**
     * Set creation timestamp
     * 
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    
    /**
     * Check if this user has a valid ID (is persisted in database)
     * 
     * @return true if user has a valid ID, false otherwise
     */
    public boolean isPersisted() {
        return userId > 0;
    }
    
    /**
     * Validate all required fields are present and valid
     * 
     * @return true if all required fields are valid, false otherwise
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() && username.length() <= 50 &&
               password != null && !password.isEmpty() &&
               email != null && !email.trim().isEmpty() && email.length() <= 100 &&
               email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    /**
     * Get a safe string representation of the user (without password)
     * 
     * @return string representation without sensitive information
     */
    public String toSafeString() {
        return String.format("User{id=%d, username='%s', email='%s', createdAt=%s}", 
                           userId, username, email, createdAt);
    }
    
    /**
     * Create a copy of this user without the password (for security)
     * 
     * @return User object copy without password
     */
    public User createSafeCopy() {
        User safeCopy = new User();
        safeCopy.setUserId(this.userId);
        if (this.username != null) {
            safeCopy.username = this.username; // Direct assignment to avoid validation
        }
        if (this.email != null) {
            safeCopy.email = this.email; // Direct assignment to avoid validation
        }
        safeCopy.setCreatedAt(this.createdAt);
        // Intentionally not copying password
        return safeCopy;
    }
    
    /**
     * Prepare user data for database insertion/update
     * Ensures all string fields are properly trimmed and formatted
     */
    public void prepareForPersistence() {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', createdAt=%s}", 
                           userId, username, email, createdAt);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User user = (User) obj;
        return userId == user.userId &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }
}