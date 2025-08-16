package backend;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Properties;

/**
 * DatabaseConnector - Singleton class for managing MySQL database connections.
 * Optimized for HeidiSQL compatibility and MySQL best practices.
 */
public class DatabaseConnector {
    
    // Singleton instance
    private static volatile DatabaseConnector instance;
    private static final Object lock = new Object();
    
    // Database configuration for MySQL/HeidiSQL
    // TODO: Update with your HeidiSQL connection details
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_DATABASE_NAME = "recipe_manager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    // Enhanced connection URL with HeidiSQL-friendly parameters
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_DATABASE_NAME + 
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8" +
        "&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2";
    
    // Connection management
    private Connection connection;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    
    // Logging
    private static final Logger logger = Logger.getLogger(DatabaseConnector.class.getName());
    
    // Enhanced SQL statements for table creation (HeidiSQL/MySQL optimized)
    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS `users` (
            `userId` INT NOT NULL AUTO_INCREMENT,
            `username` VARCHAR(50) NOT NULL,
            `password` VARCHAR(255) NOT NULL,
            `email` VARCHAR(100) NOT NULL,
            `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (`userId`),
            UNIQUE KEY `username` (`username`),
            UNIQUE KEY `email` (`email`),
            KEY `idx_users_username` (`username`),
            KEY `idx_users_email` (`email`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;
    
    private static final String CREATE_RECIPES_TABLE = """
        CREATE TABLE IF NOT EXISTS `recipes` (
            `recipeId` INT NOT NULL AUTO_INCREMENT,
            `userId` INT NOT NULL,
            `name` VARCHAR(200) NOT NULL,
            `category` VARCHAR(50) NOT NULL,
            `prepTime` INT NOT NULL,
            `cookTime` INT NOT NULL,
            `instructions` TEXT NOT NULL,
            `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
            `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            PRIMARY KEY (`recipeId`),
            KEY `idx_recipes_userId` (`userId`),
            KEY `idx_recipes_category` (`category`),
            KEY `idx_recipes_createdat` (`createdAt` DESC),
            KEY `idx_recipes_name` (`name`),
            FULLTEXT KEY `ft_recipes_instructions` (`instructions`),
            CONSTRAINT `recipes_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;
    
    private static final String CREATE_INGREDIENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS `ingredients` (
            `ingredientId` INT NOT NULL AUTO_INCREMENT,
            `recipeId` INT NOT NULL,
            `name` VARCHAR(100) NOT NULL,
            `quantity` DECIMAL(10,2) NOT NULL,
            `unit` VARCHAR(20) NOT NULL,
            PRIMARY KEY (`ingredientId`),
            KEY `idx_ingredients_recipeId` (`recipeId`),
            FULLTEXT KEY `ft_ingredients_name` (`name`),
            CONSTRAINT `ingredients_ibfk_1` FOREIGN KEY (`recipeId`) REFERENCES `recipes` (`recipeId`) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;
    /*
    private static final String CREATE_USER_PREFERENCES_TABLE = """
        CREATE TABLE IF NOT EXISTS `user_preferences` (
          `preference_id` int NOT NULL AUTO_INCREMENT,
          `userId` int NOT NULL,
          `dark_mode` tinyint(1) DEFAULT '0',
          `language` char(2) COLLATE utf8mb4_unicode_ci DEFAULT 'en' COMMENT 'ISO 639-1 language code',
          `measurement_system` enum('metric','imperial') COLLATE utf8mb4_unicode_ci DEFAULT 'metric',
          `default_servings` int DEFAULT '4',
          `show_nutrition` tinyint(1) DEFAULT '1',
          `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
          `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          PRIMARY KEY (`preference_id`),
          UNIQUE KEY `uk_preferences_userid` (`userId`),
          CONSTRAINT `fk_preferences_userid` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE ON UPDATE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User preferences and settings'
        """;
    */
    /**
     * Private constructor to enforce singleton pattern
     */
    public DatabaseConnector() {
        initializeLogger();
        logger.info("DatabaseConnector instance created for HeidiSQL compatibility");
    }
    
    /**
     * Get singleton instance using double-checked locking pattern
     * Thread-safe implementation
     * 
     * @return DatabaseConnector instance
     */
    public static DatabaseConnector getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DatabaseConnector();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize logging configuration
     */
    private void initializeLogger() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            FileHandler fileHandler = new FileHandler("logs/database.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }
    
    /**
     * Establish database connection with HeidiSQL-optimized settings
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection connect() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                logger.info("Closing existing database connection");
                connection.close();
            }

            logger.info("Attempting to connect to MySQL database: " + DB_DATABASE_NAME + " (HeidiSQL optimized)");
            
            // Enhanced connection properties for HeidiSQL compatibility
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("useUnicode", "true");
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("autoReconnect", "true");
            props.setProperty("failOverReadOnly", "false");
            props.setProperty("maxReconnects", "3");
            props.setProperty("initialTimeout", "2");
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");
            
            connection = DriverManager.getConnection(DB_URL.split("\\?")[0], props);
            
            // Set connection properties that work well with HeidiSQL
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            logger.info("Database connection established successfully to: " + DB_DATABASE_NAME);
            
            if (!isInitialized.get()) {
                initializeTables();
                isInitialized.set(true);
            }
            
            return connection;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection", e);
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute UPDATE, INSERT, DELETE statements with proper error handling
     * 
     * @param sql SQL statement to execute
     * @return number of affected rows
     * @throws SQLException if execution fails
     */
    public int executeUpdate(String sql) throws SQLException {
        validateSqlStatement(sql);
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            int rowsAffected = pstmt.executeUpdate();
            logger.info(String.format("Executed update: %s | Rows affected: %d", 
                       sql.substring(0, Math.min(sql.length(), 50)) + "...", rowsAffected));
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute update: " + sql, e);
            throw new SQLException("Update execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute UPDATE statement with parameters to prevent SQL injection
     * 
     * @param sql SQL statement with placeholders
     * @param parameters parameters to bind to the statement
     * @return number of affected rows
     * @throws SQLException if execution fails
     */
    public int executeUpdate(String sql, Object... parameters) throws SQLException {
        validateSqlStatement(sql);
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            setParameters(pstmt, parameters);
            int rowsAffected = pstmt.executeUpdate();
            logger.info(String.format("Executed parameterized update | Rows affected: %d", rowsAffected));
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute parameterized update", e);
            throw new SQLException("Parameterized update execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute SELECT statements with proper error handling
     * 
     * @param sql SQL query to execute
     * @return ResultSet containing query results
     * @throws SQLException if execution fails
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        validateSqlStatement(sql);
        
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            logger.info("Executed query: " + sql.substring(0, Math.min(sql.length(), 50)) + "...");
            return rs;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query: " + sql, e);
            throw new SQLException("Query execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute SELECT query with parameters to prevent SQL injection
     * 
     * @param sql SQL query with placeholders
     * @param parameters parameters to bind to the statement
     * @return ResultSet containing query results
     * @throws SQLException if execution fails
     */
    public ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        validateSqlStatement(sql);
        
        try {
            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            setParameters(pstmt, parameters);
            ResultSet rs = pstmt.executeQuery();
            logger.info("Executed parameterized query successfully");
            return rs;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute parameterized query", e);
            throw new SQLException("Parameterized query execution failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if an index exists on a table
     * 
     * @param tableName name of the table
     * @param indexName name of the index
     * @return true if index exists, false otherwise
     */
    private boolean indexExists(String tableName, String indexName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?";
            try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
                pstmt.setString(1, tableName);
                pstmt.setString(2, indexName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to check if index exists: " + indexName, e);
        }
        return false;
    }
    
    /**
     * Safely create an index only if it doesn't exist
     * 
     * @param sql the CREATE INDEX statement
     * @param tableName name of the table
     * @param indexName name of the index
     */
    private void createIndexIfNotExists(String sql, String tableName, String indexName) {
        try {
            if (!indexExists(tableName, indexName)) {
                executeUpdate(sql);
                logger.info("Created index: " + indexName + " on table: " + tableName);
            } else {
                logger.info("Index already exists: " + indexName + " on table: " + tableName);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to create index: " + indexName, e);
        }
    }
    
    /**
     * Initialize all database tables with HeidiSQL-friendly structure
     * Creates tables if they don't exist with proper indexing and constraints
     */
    public void initializeTables() {
        try {
            logger.info("Initializing database tables for HeidiSQL...");
            
            executeUpdate(CREATE_USERS_TABLE);
            logger.info("Users table initialized");
            
            executeUpdate(CREATE_RECIPES_TABLE);
            logger.info("Recipes table initialized");
            
            executeUpdate(CREATE_INGREDIENTS_TABLE);
            logger.info("Ingredients table initialized");
            /*
            executeUpdate(CREATE_USER_PREFERENCES_TABLE);
            logger.info("User preferences table initialized");
            */
            logger.info("Database tables and indexes initialized successfully for HeidiSQL");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database tables", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get current connection or create new one if needed
     * 
     * @return active database connection
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return connect();
        }
        return connection;
    }
    
    /**
     * Set parameters for prepared statements with enhanced type handling
     * 
     * @param pstmt PreparedStatement to set parameters for
     * @param parameters array of parameters
     * @throws SQLException if parameter setting fails
     */
    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        if (parameters == null) return;
        
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            if (param == null) {
                pstmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof String) {
                pstmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                pstmt.setLong(i + 1, (Long) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(i + 1, (Double) param);
            } else if (param instanceof Float) {
                pstmt.setFloat(i + 1, (Float) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof java.util.Date) {
                pstmt.setTimestamp(i + 1, new Timestamp(((java.util.Date) param).getTime()));
            } else {
                pstmt.setObject(i + 1, param);
            }
        }
    }
    
    /**
     * Validate SQL statement for basic security
     * 
     * @param sql SQL statement to validate
     * @throws IllegalArgumentException if SQL is invalid
     */
    private void validateSqlStatement(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL statement cannot be null or empty");
        }
        
        String lowerSql = sql.toLowerCase().trim();
        if (lowerSql.contains("drop table") || lowerSql.contains("drop database") || lowerSql.contains("truncate")) {
            logger.warning("Potentially dangerous SQL detected: " + sql);
            throw new IllegalArgumentException("Dangerous SQL operation detected");
        }
    }
    
    /**
     * Test database connection with HeidiSQL compatibility check
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            Connection testConn = connect();
            if (testConn != null && !testConn.isClosed()) {
                // Test with a simple query that HeidiSQL would typically run
                try (PreparedStatement pstmt = testConn.prepareStatement("SELECT VERSION(), DATABASE(), USER()")) {
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        logger.info("Database connection test successful - MySQL Version: " + rs.getString(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Database connection test failed", e);
        }
        return false;
    }
    
    /**
     * Get database metadata information (useful for HeidiSQL inspection)
     * 
     * @return DatabaseMetaData object
     * @throws SQLException if metadata retrieval fails
     */
    public DatabaseMetaData getDatabaseMetadata() throws SQLException {
        return getConnection().getMetaData();
    }
    
    /**
     * Get server information that HeidiSQL typically displays
     * 
     * @return Server info string
     */
    public String getServerInfo() {
        try {
            Connection conn = getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            return String.format("Server: %s %s | JDBC Driver: %s %s | Database: %s", 
                meta.getDatabaseProductName(), 
                meta.getDatabaseProductVersion(),
                meta.getDriverName(),
                meta.getDriverVersion(),
                DB_DATABASE_NAME);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get server info", e);
            return "Server info unavailable";
        }
    }
    
    /**
     * Close database connection properly
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed successfully");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Execute a transaction with automatic rollback on error
     * Enhanced for HeidiSQL transaction handling
     * 
     * @param operations array of SQL operations to execute in transaction
     * @return true if transaction completed successfully
     */
    public boolean executeTransaction(String... operations) {
        if (operations == null || operations.length == 0) {
            logger.warning("No operations provided for transaction");
            return false;
        }
        
        try {
            Connection conn = getConnection();
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            try {
                for (String operation : operations) {
                    executeUpdate(operation);
                }
                conn.commit();
                logger.info("Transaction completed successfully with " + operations.length + " operations");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Transaction rolled back due to error", e);
                return false;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute transaction", e);
            return false;
        }
    }
}