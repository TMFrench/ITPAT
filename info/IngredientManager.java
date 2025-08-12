package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IngredientManager handles all CRUD operations for Ingredient objects in the database.
 * This class provides methods to create, read, update, and delete ingredients,
 * while maintaining proper relationships with recipes and ensuring data integrity.
 * 
 * @author Recipe Manager Team
 * @version 1.0
 */
public class IngredientManager {
    
    private static final Logger logger = Logger.getLogger(IngredientManager.class.getName());
    private DatabaseConnector dbConnector;
    
    // SQL Query Constants
    private static final String INSERT_INGREDIENT = 
        "INSERT INTO ingredients (recipeId, name, quantity, unit) VALUES (?, ?, ?, ?)";
    
    private static final String SELECT_INGREDIENTS_BY_RECIPE = 
        "SELECT * FROM ingredients WHERE recipeId = ? ORDER BY name ASC";
    
    private static final String SELECT_INGREDIENT_BY_ID = 
        "SELECT * FROM ingredients WHERE ingredientId = ?";
    
    private static final String UPDATE_INGREDIENT = 
        "UPDATE ingredients SET name = ?, quantity = ?, unit = ? WHERE ingredientId = ?";
    
    private static final String DELETE_INGREDIENT = 
        "DELETE FROM ingredients WHERE ingredientId = ?";
    
    private static final String DELETE_INGREDIENTS_BY_RECIPE = 
        "DELETE FROM ingredients WHERE recipeId = ?";
    
    private static final String CHECK_RECIPE_EXISTS = 
        "SELECT COUNT(*) FROM recipes WHERE recipeId = ?";
    
    private static final String CHECK_INGREDIENT_EXISTS = 
        "SELECT COUNT(*) FROM ingredients WHERE ingredientId = ?";
    
    private static final String COUNT_INGREDIENTS_BY_RECIPE = 
        "SELECT COUNT(*) FROM ingredients WHERE recipeId = ?";
    
    /**
     * Constructor initializes IngredientManager with database connector.
     */
    public IngredientManager() {
        this.dbConnector = new DatabaseConnector();
    }
    
    /**
     * Constructor with custom DatabaseConnector for dependency injection.
     * 
     * @param dbConnector Custom database connector instance
     */
    public IngredientManager(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }
    
    /**
     * Adds a new ingredient to the database.
     * Validates input parameters and ensures referential integrity with recipes.
     * 
     * @param ingredient The Ingredient object to be added to the database
     * @return true if the ingredient was successfully added, false otherwise
     * @throws IllegalArgumentException if ingredient is null or contains invalid data
     * @throws IllegalStateException if the associated recipe does not exist
     */
    public boolean addIngredient(Ingredient ingredient) {
        if (!validateIngredient(ingredient)) {
            throw new IllegalArgumentException("Invalid ingredient data provided");
        }
        
        // Check if the recipe exists before adding ingredient
        if (!recipeExists(ingredient.getRecipeId())) {
            throw new IllegalStateException("Cannot add ingredient: Recipe with ID " + 
                ingredient.getRecipeId() + " does not exist");
        }
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_INGREDIENT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, ingredient.getRecipeId());
            pstmt.setString(2, ingredient.getName().trim());
            pstmt.setDouble(3, ingredient.getQuantity());
            pstmt.setString(4, ingredient.getUnit().trim());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated ingredient ID and set it in the ingredient object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ingredient.setIngredientId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Ingredient added successfully: " + ingredient.getName() + 
                           " for recipe ID: " + ingredient.getRecipeId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding ingredient: " + ingredient.getName(), e);
        }
        
        return false;
    }
    
    /**
     * Retrieves all ingredients associated with a specific recipe ID.
     * Returns ingredients ordered by name for consistent presentation.
     * 
     * @param recipeId The recipe ID to retrieve ingredients for
     * @return List of Ingredient objects for the specified recipe, empty list if none found
     * @throws IllegalArgumentException if recipeId is less than or equal to 0
     */
    public List<Ingredient> getIngredientsByRecipe(int recipeId) {
        validateId(recipeId, "Recipe ID");
        List<Ingredient> ingredients = new ArrayList<>();
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_INGREDIENTS_BY_RECIPE)) {
            
            pstmt.setInt(1, recipeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(mapResultSetToIngredient(rs));
                }
            }
            
            logger.info("Retrieved " + ingredients.size() + " ingredients for recipe ID: " + recipeId);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving ingredients for recipe ID: " + recipeId, e);
        }
        
        return ingredients;
    }
    
    /**
     * Retrieves a specific ingredient by its ID.
     * 
     * @param ingredientId The ingredient ID to search for
     * @return Ingredient object if found, null otherwise
     * @throws IllegalArgumentException if ingredientId is less than or equal to 0
     */
    public Ingredient getIngredientById(int ingredientId) {
        validateId(ingredientId, "Ingredient ID");
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_INGREDIENT_BY_ID)) {
            
            pstmt.setInt(1, ingredientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIngredient(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving ingredient with ID: " + ingredientId, e);
        }
        
        return null;
    }
    
    /**
     * Updates an existing ingredient in the database.
     * All fields except ingredient ID and recipe ID can be updated.
     * The recipe ID cannot be changed to maintain referential integrity.
     * 
     * @param ingredient The Ingredient object with updated information
     * @return true if the ingredient was successfully updated, false otherwise
     * @throws IllegalArgumentException if ingredient is null or contains invalid data
     * @throws IllegalStateException if the ingredient does not exist
     */
    public boolean updateIngredient(Ingredient ingredient) {
        if (!validateIngredient(ingredient) || ingredient.getIngredientId() <= 0) {
            throw new IllegalArgumentException("Invalid ingredient data or missing ingredient ID");
        }
        
        // Check if ingredient exists before updating
        if (!ingredientExists(ingredient.getIngredientId())) {
            throw new IllegalStateException("Cannot update ingredient: Ingredient with ID " + 
                ingredient.getIngredientId() + " does not exist");
        }
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_INGREDIENT)) {
            
            pstmt.setString(1, ingredient.getName().trim());
            pstmt.setDouble(2, ingredient.getQuantity());
            pstmt.setString(3, ingredient.getUnit().trim());
            pstmt.setInt(4, ingredient.getIngredientId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Ingredient updated successfully: " + ingredient.getName());
                return true;
            } else {
                logger.warning("No ingredient found with ID: " + ingredient.getIngredientId());
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating ingredient: " + ingredient.getName(), e);
        }
        
        return false;
    }
    
    /**
     * Deletes a specific ingredient from the database by its ID.
     * 
     * @param id The ingredient ID to delete
     * @return true if the ingredient was successfully deleted, false otherwise
     * @throws IllegalArgumentException if id is less than or equal to 0
     */
    public boolean deleteIngredient(int id) {
        validateId(id, "Ingredient ID");
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_INGREDIENT)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Ingredient deleted successfully with ID: " + id);
                return true;
            } else {
                logger.warning("No ingredient found with ID: " + id);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting ingredient with ID: " + id, e);
        }
        
        return false;
    }
    
    /**
     * Deletes all ingredients associated with a specific recipe ID.
     * This method is typically used when a recipe is deleted to maintain database integrity.
     * 
     * @param recipeId The recipe ID whose ingredients should be deleted
     * @return The number of ingredients deleted
     * @throws IllegalArgumentException if recipeId is less than or equal to 0
     */
    public int deleteIngredientsByRecipe(int recipeId) {
        validateId(recipeId, "Recipe ID");
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_INGREDIENTS_BY_RECIPE)) {
            
            pstmt.setInt(1, recipeId);
            int rowsAffected = pstmt.executeUpdate();
            
            logger.info("Deleted " + rowsAffected + " ingredients for recipe ID: " + recipeId);
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting ingredients for recipe ID: " + recipeId, e);
        }
        
        return 0;
    }
    
    /**
     * Adds multiple ingredients to a recipe in a single transaction.
     * If any ingredient fails to add, the entire transaction is rolled back.
     * 
     * @param ingredients List of ingredients to add
     * @return true if all ingredients were successfully added, false otherwise
     * @throws IllegalArgumentException if ingredients list is null or empty
     */
    public boolean addIngredientsToRecipe(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients list cannot be null or empty");
        }
        
        Connection conn = null;
        
        try {
            conn = dbConnector.connect();
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_INGREDIENT, Statement.RETURN_GENERATED_KEYS)) {
                
                for (Ingredient ingredient : ingredients) {
                    if (!validateIngredient(ingredient)) {
                        throw new IllegalArgumentException("Invalid ingredient data in batch: " + ingredient.getName());
                    }
                    
                    // Check recipe exists for each ingredient
                    if (!recipeExists(ingredient.getRecipeId())) {
                        throw new IllegalStateException("Recipe with ID " + ingredient.getRecipeId() + " does not exist");
                    }
                    
                    pstmt.setInt(1, ingredient.getRecipeId());
                    pstmt.setString(2, ingredient.getName().trim());
                    pstmt.setDouble(3, ingredient.getQuantity());
                    pstmt.setString(4, ingredient.getUnit().trim());
                    pstmt.addBatch();
                }
                
                int[] results = pstmt.executeBatch();
                
                // Check if all inserts were successful
                for (int result : results) {
                    if (result <= 0) {
                        conn.rollback();
                        logger.warning("Batch ingredient insert failed, rolling back transaction");
                        return false;
                    }
                }
                
                conn.commit(); // Commit transaction
                logger.info("Successfully added " + ingredients.size() + " ingredients in batch");
                return true;
            }
            
        } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Error rolling back batch ingredient transaction", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Error adding ingredients in batch", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore auto-commit
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.log(Level.SEVERE, "Error closing connection", closeEx);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Gets the count of ingredients for a specific recipe.
     * 
     * @param recipeId The recipe ID to count ingredients for
     * @return The number of ingredients associated with the recipe
     * @throws IllegalArgumentException if recipeId is less than or equal to 0
     */
    public int getIngredientCountByRecipe(int recipeId) {
        validateId(recipeId, "Recipe ID");
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_INGREDIENTS_BY_RECIPE)) {
            
            pstmt.setInt(1, recipeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting ingredients for recipe ID: " + recipeId, e);
        }
        
        return 0;
    }
    
    /**
     * Checks if a recipe exists in the database.
     * Used to maintain referential integrity before adding/updating ingredients.
     * 
     * @param recipeId The recipe ID to check
     * @return true if the recipe exists, false otherwise
     */
    private boolean recipeExists(int recipeId) {
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_RECIPE_EXISTS)) {
            
            pstmt.setInt(1, recipeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if recipe exists with ID: " + recipeId, e);
        }
        
        return false;
    }
    
    /**
     * Checks if an ingredient exists in the database.
     * 
     * @param ingredientId The ingredient ID to check
     * @return true if the ingredient exists, false otherwise
     */
    private boolean ingredientExists(int ingredientId) {
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_INGREDIENT_EXISTS)) {
            
            pstmt.setInt(1, ingredientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if ingredient exists with ID: " + ingredientId, e);
        }
        
        return false;
    }
    
    /**
     * Maps a ResultSet row to an Ingredient object.
     * 
     * @param rs The ResultSet containing ingredient data
     * @return Ingredient object populated with data from ResultSet
     * @throws SQLException if error occurs while reading ResultSet
     */
    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(rs.getInt("ingredientId"));
        ingredient.setRecipeId(rs.getInt("recipeId"));
        ingredient.setName(rs.getString("name"));
        ingredient.setQuantity(rs.getDouble("quantity"));
        ingredient.setUnit(rs.getString("unit"));
        return ingredient;
    }
    
    /**
     * Validates an Ingredient object for required fields and data integrity.
     * 
     * @param ingredient The Ingredient object to validate
     * @return true if ingredient is valid, false otherwise
     */
    private boolean validateIngredient(Ingredient ingredient) {
        if (ingredient == null) {
            logger.warning("Ingredient object is null");
            return false;
        }
        
        if (ingredient.getRecipeId() <= 0) {
            logger.warning("Invalid recipe ID: " + ingredient.getRecipeId());
            return false;
        }
        
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            logger.warning("Ingredient name is null or empty");
            return false;
        }
        
        if (ingredient.getName().trim().length() > 100) {
            logger.warning("Ingredient name exceeds maximum length of 100 characters");
            return false;
        }
        
        if (ingredient.getQuantity() < 0) {
            logger.warning("Invalid quantity: " + ingredient.getQuantity());
            return false;
        }
        
        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
            logger.warning("Ingredient unit is null or empty");
            return false;
        }
        
        if (ingredient.getUnit().trim().length() > 50) {
            logger.warning("Ingredient unit exceeds maximum length of 50 characters");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates an ID parameter.
     * 
     * @param id The ID to validate
     * @param fieldName The name of the field being validated (for error messages)
     * @throws IllegalArgumentException if ID is invalid
     */
    private void validateId(int id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }
    }
}