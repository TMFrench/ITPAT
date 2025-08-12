package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RecipeManager handles all CRUD operations for Recipe objects in the database.
 * This class provides methods to create, read, update, and delete recipes,
 * as well as search and filter functionality.
 * 
 * @author Recipe Manager Team
 * @version 1.0
 */
public class RecipeManager {
    
    private static final Logger logger = Logger.getLogger(RecipeManager.class.getName());
    private DatabaseConnector dbConnector;
    
    // SQL Query Constants
    private static final String INSERT_RECIPE = 
        "INSERT INTO recipes (userid, name, category, prepTime, cookTime, instructions) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_RECIPE_BY_ID = 
        "SELECT * FROM recipes WHERE recipeId = ?";
    
    private static final String SELECT_ALL_RECIPES = 
        "SELECT * FROM recipes ORDER BY name ASC";
    
    private static final String SELECT_RECIPES_BY_USER = 
        "SELECT * FROM recipes WHERE userid = ? ORDER BY name ASC";
    
    private static final String SELECT_RECIPES_BY_CATEGORY = 
        "SELECT * FROM recipes WHERE category = ? ORDER BY name ASC";
    
    private static final String UPDATE_RECIPE = 
        "UPDATE recipes SET name = ?, category = ?, prepTime = ?, cookTime = ?, instructions = ? WHERE recipeId = ?";
    
    private static final String DELETE_RECIPE = 
        "DELETE FROM recipes WHERE recipeId = ?";
    
    private static final String DELETE_INGREDIENTS_BY_RECIPE = 
        "DELETE FROM ingredients WHERE recipeId = ?";
    
    private static final String SEARCH_RECIPES = 
        "SELECT * FROM recipes WHERE LOWER(name) LIKE ? OR LOWER(instructions) LIKE ? ORDER BY name ASC";
    
    private static final String CHECK_RECIPE_EXISTS = 
        "SELECT COUNT(*) FROM recipes WHERE recipeId = ?";
    
    /**
     * Constructor initializes RecipeManager with database connector.
     */
    public RecipeManager() {
        this.dbConnector = new DatabaseConnector();
    }
    
    /**
     * Constructor with custom DatabaseConnector for dependency injection.
     * 
     * @param dbConnector Custom database connector instance
     */
    public RecipeManager(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }
    
    /**
     * Adds a new recipe to the database.
     * Validates input parameters and ensures data integrity before insertion.
     * 
     * @param recipe The Recipe object to be added to the database
     * @return true if the recipe was successfully added, false otherwise
     * @throws IllegalArgumentException if recipe is null or contains invalid data
     */
    public boolean addRecipe(Recipe recipe) {
        if (!validateRecipe(recipe)) {
            throw new IllegalArgumentException("Invalid recipe data provided");
        }
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_RECIPE, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, recipe.getUserId());
            pstmt.setString(2, recipe.getName().trim());
            pstmt.setString(3, recipe.getCategory().trim());
            pstmt.setInt(4, recipe.getPrepTime());
            pstmt.setInt(5, recipe.getCookTime());
            pstmt.setString(6, recipe.getInstructions().trim());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated recipe ID and set it in the recipe object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        recipe.setRecipeId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Recipe added successfully: " + recipe.getName());
                return true;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding recipe: " + recipe.getName(), e);
        }
        
        return false;
    }
    
    /**
     * Retrieves a recipe by its unique ID.
     * 
     * @param id The recipe ID to search for
     * @return Recipe object if found, null otherwise
     * @throws IllegalArgumentException if id is less than or equal to 0
     */
    public Recipe getRecipeById(int id) {
        validateId(id, "Recipe ID");
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_RECIPE_BY_ID)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecipe(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving recipe with ID: " + id, e);
        }
        
        return null;
    }
    
    /**
     * Retrieves all recipes from the database.
     * 
     * @return List of all Recipe objects, empty list if no recipes found
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_RECIPES);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                recipes.add(mapResultSetToRecipe(rs));
            }
            
            logger.info("Retrieved " + recipes.size() + " recipes from database");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all recipes", e);
        }
        
        return recipes;
    }
    
    /**
     * Retrieves all recipes belonging to a specific user.
     * 
     * @param userId The user ID to filter recipes by
     * @return List of Recipe objects for the specified user, empty list if none found
     * @throws IllegalArgumentException if userId is less than or equal to 0
     */
    public List<Recipe> getRecipesByUserId(int userId) {
        validateId(userId, "User ID");
        List<Recipe> recipes = new ArrayList<>();
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_RECIPES_BY_USER)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(mapResultSetToRecipe(rs));
                }
            }
            
            logger.info("Retrieved " + recipes.size() + " recipes for user ID: " + userId);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving recipes for user ID: " + userId, e);
        }
        
        return recipes;
    }
    
    /**
     * Retrieves all recipes in a specific category.
     * 
     * @param category The category to filter recipes by (case-sensitive)
     * @return List of Recipe objects in the specified category, empty list if none found
     * @throws IllegalArgumentException if category is null or empty
     */
    public List<Recipe> getRecipesByCategory(String category) {
        validateString(category, "Category");
        List<Recipe> recipes = new ArrayList<>();
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_RECIPES_BY_CATEGORY)) {
            
            pstmt.setString(1, category.trim());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(mapResultSetToRecipe(rs));
                }
            }
            
            logger.info("Retrieved " + recipes.size() + " recipes for category: " + category);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving recipes for category: " + category, e);
        }
        
        return recipes;
    }
    
    /**
     * Updates an existing recipe in the database.
     * All fields except recipe ID and user ID can be updated.
     * 
     * @param recipe The Recipe object with updated information
     * @return true if the recipe was successfully updated, false otherwise
     * @throws IllegalArgumentException if recipe is null or contains invalid data
     */
    public boolean updateRecipe(Recipe recipe) {
        if (!validateRecipe(recipe) || recipe.getRecipeId() <= 0) {
            throw new IllegalArgumentException("Invalid recipe data or missing recipe ID");
        }
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_RECIPE)) {
            
            pstmt.setString(1, recipe.getName().trim());
            pstmt.setString(2, recipe.getCategory().trim());
            pstmt.setInt(3, recipe.getPrepTime());
            pstmt.setInt(4, recipe.getCookTime());
            pstmt.setString(5, recipe.getInstructions().trim());
            pstmt.setInt(6, recipe.getRecipeId());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Recipe updated successfully: " + recipe.getName());
                return true;
            } else {
                logger.warning("No recipe found with ID: " + recipe.getRecipeId());
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating recipe: " + recipe.getName(), e);
        }
        
        return false;
    }
    
    /**
     * Deletes a recipe and all associated ingredients from the database.
     * This operation cascades to remove all ingredients linked to the recipe.
     * 
     * @param id The recipe ID to delete
     * @return true if the recipe was successfully deleted, false otherwise
     * @throws IllegalArgumentException if id is less than or equal to 0
     */
    public boolean deleteRecipe(int id) {
        validateId(id, "Recipe ID");
        
        Connection conn = null;
        
        try {
            conn = dbConnector.connect();
            conn.setAutoCommit(false); // Start transaction
            
            // First, delete associated ingredients (cascade deletion)
            try (PreparedStatement deleteIngredients = conn.prepareStatement(DELETE_INGREDIENTS_BY_RECIPE)) {
                deleteIngredients.setInt(1, id);
                int ingredientsDeleted = deleteIngredients.executeUpdate();
                logger.info("Deleted " + ingredientsDeleted + " ingredients for recipe ID: " + id);
            }
            
            // Then, delete the recipe
            try (PreparedStatement deleteRecipe = conn.prepareStatement(DELETE_RECIPE)) {
                deleteRecipe.setInt(1, id);
                int rowsAffected = deleteRecipe.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit(); // Commit transaction
                    logger.info("Recipe deleted successfully with ID: " + id);
                    return true;
                } else {
                    conn.rollback(); // Rollback if recipe not found
                    logger.warning("No recipe found with ID: " + id);
                }
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
                }
            }
            logger.log(Level.SEVERE, "Error deleting recipe with ID: " + id, e);
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
     * Searches for recipes by name or instructions using case-insensitive matching.
     * Uses LIKE operator with wildcards for partial matching.
     * 
     * @param searchTerm The term to search for in recipe names and instructions
     * @return List of Recipe objects matching the search term, empty list if none found
     * @throws IllegalArgumentException if searchTerm is null or empty
     */
    public List<Recipe> searchRecipes(String searchTerm) {
        validateString(searchTerm, "Search term");
        List<Recipe> recipes = new ArrayList<>();
        
        try (Connection conn = dbConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_RECIPES)) {
            
            String searchPattern = "%" + searchTerm.trim().toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(mapResultSetToRecipe(rs));
                }
            }
            
            logger.info("Found " + recipes.size() + " recipes matching search term: " + searchTerm);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching recipes with term: " + searchTerm, e);
        }
        
        return recipes;
    }
    
    /**
     * Checks if a recipe exists in the database.
     * 
     * @param recipeId The recipe ID to check
     * @return true if the recipe exists, false otherwise
     */
    public boolean recipeExists(int recipeId) {
        validateId(recipeId, "Recipe ID");
        
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
     * Maps a ResultSet row to a Recipe object.
     * 
     * @param rs The ResultSet containing recipe data
     * @return Recipe object populated with data from ResultSet
     * @throws SQLException if error occurs while reading ResultSet
     */
    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setRecipeId(rs.getInt("recipeId"));
        recipe.setUserId(rs.getInt("userid"));
        recipe.setName(rs.getString("name"));
        recipe.setCategory(rs.getString("category"));
        recipe.setPrepTime(rs.getInt("prepTime"));
        recipe.setCookTime(rs.getInt("cookTime"));
        recipe.setInstructions(rs.getString("instructions"));
        return recipe;
    }
    
    /**
     * Validates a Recipe object for required fields and data integrity.
     * 
     * @param recipe The Recipe object to validate
     * @return true if recipe is valid, false otherwise
     */
    private boolean validateRecipe(Recipe recipe) {
        if (recipe == null) {
            logger.warning("Recipe object is null");
            return false;
        }
        
        if (recipe.getUserId() <= 0) {
            logger.warning("Invalid user ID: " + recipe.getUserId());
            return false;
        }
        
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            logger.warning("Recipe name is null or empty");
            return false;
        }
        
        if (recipe.getName().trim().length() > 255) {
            logger.warning("Recipe name exceeds maximum length of 255 characters");
            return false;
        }
        
        if (recipe.getCategory() == null || recipe.getCategory().trim().isEmpty()) {
            logger.warning("Recipe category is null or empty");
            return false;
        }
        
        if (recipe.getPrepTime() < 0) {
            logger.warning("Invalid prep time: " + recipe.getPrepTime());
            return false;
        }
        
        if (recipe.getCookTime() < 0) {
            logger.warning("Invalid cook time: " + recipe.getCookTime());
            return false;
        }
        
        if (recipe.getInstructions() == null || recipe.getInstructions().trim().isEmpty()) {
            logger.warning("Recipe instructions are null or empty");
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
    
    /**
     * Validates a String parameter.
     * 
     * @param value The String to validate
     * @param fieldName The name of the field being validated (for error messages)
     * @throws IllegalArgumentException if String is null or empty
     */
    private void validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
}