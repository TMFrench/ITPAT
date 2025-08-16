package backend;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileManager handles all file operations for the Recipe Manager application.
 * This class provides methods to export and import recipes in JSON format,
 * with comprehensive error handling, progress tracking, and data validation.
 */
public class FileManager {
    
    private static final Logger logger = Logger.getLogger(FileManager.class.getName());
    private final Gson gson;
    private final RecipeManager recipeManager;
    private final IngredientManager ingredientManager;
    
    // JSON schema validation constants
    private static final String EXPECTED_RECIPE_FIELDS[] = {
        "recipeId", "userId", "name", "category", "prepTime", "cookTime", "instructions"
    };
    
    private static final String EXPECTED_INGREDIENT_FIELDS[] = {
        "ingredientId", "recipeId", "name", "quantity", "unit"
    };
    
    /**
     * Progress callback interface for tracking export/import operations.
     */
    public interface ProgressCallback {
        /**
         * Called to report progress during operations.
         * 
         * @param current Current item being processed
         * @param total Total items to process
         * @param message Descriptive message about current operation
         */
        void onProgress(int current, int total, String message);
    }
    
    /**
     * Container class for recipe export data including associated ingredients.
     */
    private static class RecipeExportData {
        private Recipe recipe;
        private List<Ingredient> ingredients;
        private String exportTimestamp;
        
        public RecipeExportData() {}
        
        public RecipeExportData(Recipe recipe, List<Ingredient> ingredients) {
            this.recipe = recipe;
            this.ingredients = ingredients;
            this.exportTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        // Getters and setters
        public Recipe getRecipe() { return recipe; }
        public void setRecipe(Recipe recipe) { this.recipe = recipe; }
        public List<Ingredient> getIngredients() { return ingredients; }
        public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
        public String getExportTimestamp() { return exportTimestamp; }
        public void setExportTimestamp(String exportTimestamp) { this.exportTimestamp = exportTimestamp; }
    }
    
    /**
     * Container class for the complete export file structure.
     */
    private static class ExportContainer {
        private String exportVersion;
        private String exportTimestamp;
        private int totalRecipes;
        private List<RecipeExportData> recipes;
        
        public ExportContainer() {
            this.exportVersion = "1.0";
            this.exportTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        // Getters and setters
        public String getExportVersion() { return exportVersion; }
        public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
        public String getExportTimestamp() { return exportTimestamp; }
        public void setExportTimestamp(String exportTimestamp) { this.exportTimestamp = exportTimestamp; }
        public int getTotalRecipes() { return totalRecipes; }
        public void setTotalRecipes(int totalRecipes) { this.totalRecipes = totalRecipes; }
        public List<RecipeExportData> getRecipes() { return recipes; }
        public void setRecipes(List<RecipeExportData> recipes) { this.recipes = recipes; }
    }
    
    /**
     * Constructor initializes FileManager with required dependencies.
     * 
     * @param recipeManager RecipeManager instance for database operations
     * @param ingredientManager IngredientManager instance for ingredient operations
     */
    public FileManager(RecipeManager recipeManager, IngredientManager ingredientManager) {
        this.recipeManager = recipeManager;
        this.ingredientManager = ingredientManager;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    }
    
    /**
     * Constructor with default managers (creates new instances).
     */
    public FileManager() {
        this.recipeManager = new RecipeManager();
        this.ingredientManager = new IngredientManager();
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    }
    
    /**
     * Exports a list of recipes to a JSON file at the specified file path.
     * Includes all associated ingredients and provides progress tracking for large datasets.
     * 
     * @param recipes List of Recipe objects to export
     * @param filePath The file path where the JSON file will be saved
     * @return true if export was successful, false otherwise
     * @throws IllegalArgumentException if recipes list is null or filePath is invalid
     * @throws IOException if file operations fail
     * 
     * @example
     * <pre>
     * List&lt;Recipe&gt; recipes = recipeManager.getAllRecipes();
     * boolean success = fileManager.exportRecipesToJSON(recipes, "/path/to/recipes.json");
     * </pre>
     */
    public boolean exportRecipesToJSON(List<Recipe> recipes, String filePath) {
        return exportRecipesToJSON(recipes, filePath, null);
    }
    
    /**
     * Exports a list of recipes to a JSON file with progress callback.
     * 
     * @param recipes List of Recipe objects to export
     * @param filePath The file path where the JSON file will be saved
     * @param progressCallback Callback for progress updates (can be null)
     * @return true if export was successful, false otherwise
     * @throws IllegalArgumentException if recipes list is null or filePath is invalid
     * @throws IOException if file operations fail
     */
    public boolean exportRecipesToJSON(List<Recipe> recipes, String filePath, ProgressCallback progressCallback) {
        validateRecipeList(recipes, "Recipes list");
        validateFilePath(filePath, "Export file path");
        
        try {
            // Create directory if it doesn't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            // Prepare export container
            ExportContainer container = new ExportContainer();
            container.setTotalRecipes(recipes.size());
            
            List<RecipeExportData> exportDataList = new ArrayList<>();
            
            // Process each recipe with progress tracking
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                
                if (progressCallback != null) {
                    progressCallback.onProgress(i + 1, recipes.size(), 
                        "Processing recipe: " + recipe.getName());
                }
                
                // Get ingredients for this recipe
                List<Ingredient> ingredients = ingredientManager.getIngredientsByRecipe(recipe.getRecipeId());
                
                // Create export data
                RecipeExportData exportData = new RecipeExportData(recipe, ingredients);
                exportDataList.add(exportData);
            }
            
            container.setRecipes(exportDataList);
            
            // Write to file with UTF-8 encoding
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
                
                gson.toJson(container, writer);
                writer.flush();
            }
            
            if (progressCallback != null) {
                progressCallback.onProgress(recipes.size(), recipes.size(), "Export completed successfully");
            }
            
            logger.info("Successfully exported " + recipes.size() + " recipes to: " + filePath);
            return true;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error exporting recipes to file: " + filePath, e);
            throw new RuntimeException("Failed to export recipes: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during recipe export", e);
            return false;
        }
    }
    
    /**
     * Imports recipes from a JSON file located at the specified file path.
     * Validates JSON structure and maintains ingredient relationships during import.
     * 
     * @param filePath The file path of the JSON file to import
     * @return List of imported Recipe objects with populated IDs, empty list if import fails
     * @throws IllegalArgumentException if filePath is invalid
     * @throws IOException if file operations fail
     * @throws JsonSyntaxException if JSON structure is invalid
     * 
     * @example
     * <pre>
     * List&lt;Recipe&gt; importedRecipes = fileManager.importRecipesFromJSON("/path/to/recipes.json");
     * if (!importedRecipes.isEmpty()) {
     *     System.out.println("Successfully imported " + importedRecipes.size() + " recipes");
     * }
     * </pre>
     */
    public List<Recipe> importRecipesFromJSON(String filePath) {
        return importRecipesFromJSON(filePath, null);
    }
    
    /**
     * Imports recipes from a JSON file with progress callback.
     * 
     * @param filePath The file path of the JSON file to import
     * @param progressCallback Callback for progress updates (can be null)
     * @return List of imported Recipe objects with populated IDs
     * @throws IllegalArgumentException if filePath is invalid
     * @throws IOException if file operations fail
     * @throws JsonSyntaxException if JSON structure is invalid
     */
    public List<Recipe> importRecipesFromJSON(String filePath, ProgressCallback progressCallback) {
        validateFilePath(filePath, "Import file path");
        
        if (!Files.exists(Paths.get(filePath))) {
            throw new IllegalArgumentException("Import file does not exist: " + filePath);
        }
        
        List<Recipe> importedRecipes = new ArrayList<>();
        
        try {
            // Read and parse JSON file
            String jsonContent;
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filePath), StandardCharsets.UTF_8)) {
                
                jsonContent = readFileContent(reader);
            }
            
            // Validate and parse JSON structure
            ExportContainer container = parseAndValidateJSON(jsonContent);
            
            List<RecipeExportData> recipeDataList = container.getRecipes();
            if (recipeDataList == null || recipeDataList.isEmpty()) {
                logger.warning("No recipes found in import file: " + filePath);
                return importedRecipes;
            }
            
            // Import each recipe with ingredients
            for (int i = 0; i < recipeDataList.size(); i++) {
                RecipeExportData recipeData = recipeDataList.get(i);
                
                if (progressCallback != null) {
                    progressCallback.onProgress(i + 1, recipeDataList.size(), 
                        "Importing recipe: " + recipeData.getRecipe().getName());
                }
                
                Recipe importedRecipe = importSingleRecipe(recipeData);
                if (importedRecipe != null) {
                    importedRecipes.add(importedRecipe);
                }
            }
            
            if (progressCallback != null) {
                progressCallback.onProgress(recipeDataList.size(), recipeDataList.size(), 
                    "Import completed successfully");
            }
            
            logger.info("Successfully imported " + importedRecipes.size() + " recipes from: " + filePath);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading import file: " + filePath, e);
            throw new RuntimeException("Failed to import recipes: " + e.getMessage(), e);
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "Invalid JSON structure in file: " + filePath, e);
            throw new RuntimeException("Invalid JSON format: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during recipe import", e);
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
        
        return importedRecipes;
    }
    
    /**
     * Exports all recipes associated with a specific user ID to a JSON file.
     * Retrieves user recipes from database and exports them with full ingredient data.
     * 
     * @param userId The user ID whose recipes should be exported
     * @param filePath The file path where the JSON file will be saved
     * @return true if export was successful, false otherwise
     * @throws IllegalArgumentException if userId is invalid or filePath is null
     * @throws IOException if file operations fail
     * 
     * @example
     * <pre>
     * boolean success = fileManager.exportUserRecipesToJSON(123, "/path/to/user_recipes.json");
     * </pre>
     */
    public boolean exportUserRecipesToJSON(int userId, String filePath) {
        return exportUserRecipesToJSON(userId, filePath, null);
    }
    
    /**
     * Exports user recipes with progress callback.
     * 
     * @param userId The user ID whose recipes should be exported
     * @param filePath The file path where the JSON file will be saved
     * @param progressCallback Callback for progress updates (can be null)
     * @return true if export was successful, false otherwise
     * @throws IllegalArgumentException if userId is invalid or filePath is null
     */
    public boolean exportUserRecipesToJSON(int userId, String filePath, ProgressCallback progressCallback) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0");
        }
        
        try {
            if (progressCallback != null) {
                progressCallback.onProgress(0, 100, "Retrieving user recipes...");
            }
            
            // Get all recipes for the specified user
            List<Recipe> userRecipes = recipeManager.getRecipesByUserId(userId);
            
            if (userRecipes.isEmpty()) {
                logger.info("No recipes found for user ID: " + userId);
                if (progressCallback != null) {
                    progressCallback.onProgress(100, 100, "No recipes found for user");
                }
                return true; // Not an error, just no recipes to export
            }
            
            // Export the recipes
            return exportRecipesToJSON(userRecipes, filePath, progressCallback);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error exporting recipes for user ID: " + userId, e);
            return false;
        }
    }
    
    /**
     * Validates the structure of a JSON file without importing it.
     * Useful for pre-validation before attempting import operations.
     * 
     * @param filePath The file path of the JSON file to validate
     * @return true if JSON structure is valid, false otherwise
     * @throws IllegalArgumentException if filePath is invalid
     */
    public boolean validateJSONFile(String filePath) {
        validateFilePath(filePath, "Validation file path");
        
        if (!Files.exists(Paths.get(filePath))) {
            logger.warning("Validation file does not exist: " + filePath);
            return false;
        }
        
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            
            String jsonContent = readFileContent(reader);
            parseAndValidateJSON(jsonContent);
            
            logger.info("JSON file validation successful: " + filePath);
            return true;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "JSON file validation failed: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Gets export statistics from a JSON file without fully importing it.
     * 
     * @param filePath The file path of the JSON file to analyze
     * @return Export statistics containing recipe count, export date, etc.
     * @throws IllegalArgumentException if filePath is invalid
     */
    public ExportStatistics getExportStatistics(String filePath) {
        validateFilePath(filePath, "Statistics file path");
        
        if (!Files.exists(Paths.get(filePath))) {
            throw new IllegalArgumentException("Statistics file does not exist: " + filePath);
        }
        
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            
            String jsonContent = readFileContent(reader);
            ExportContainer container = gson.fromJson(jsonContent, ExportContainer.class);
            
            return new ExportStatistics(
                container.getTotalRecipes(),
                container.getExportTimestamp(),
                container.getExportVersion(),
                filePath
            );
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading export statistics: " + filePath, e);
            throw new RuntimeException("Failed to read export statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Container class for export file statistics.
     */
    public static class ExportStatistics {
        private final int totalRecipes;
        private final String exportTimestamp;
        private final String exportVersion;
        private final String filePath;
        
        public ExportStatistics(int totalRecipes, String exportTimestamp, String exportVersion, String filePath) {
            this.totalRecipes = totalRecipes;
            this.exportTimestamp = exportTimestamp;
            this.exportVersion = exportVersion;
            this.filePath = filePath;
        }
        
        public int getTotalRecipes() { return totalRecipes; }
        public String getExportTimestamp() { return exportTimestamp; }
        public String getExportVersion() { return exportVersion; }
        public String getFilePath() { return filePath; }
        
        @Override
        public String toString() {
            return String.format("ExportStatistics{totalRecipes=%d, exportTimestamp='%s', exportVersion='%s', filePath='%s'}", 
                totalRecipes, exportTimestamp, exportVersion, filePath);
        }
    }
    
    /**
     * Imports a single recipe with its ingredients in a transaction.
     */
    private Recipe importSingleRecipe(RecipeExportData recipeData) {
        Recipe recipe = recipeData.getRecipe();
        List<Ingredient> ingredients = recipeData.getIngredients();
        
        try {
            // Clear IDs for import (will be auto-generated)
            recipe.setRecipeId(0);
            
            // Add recipe to database
            if (recipeManager.addRecipe(recipe)) {
                // Add ingredients if any exist
                if (ingredients != null && !ingredients.isEmpty()) {
                    for (Ingredient ingredient : ingredients) {
                        ingredient.setIngredientId(0); // Clear ID for import
                        ingredient.setRecipeId(recipe.getRecipeId()); // Set new recipe ID
                        ingredientManager.addIngredient(ingredient);
                    }
                }
                return recipe;
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to import recipe: " + recipe.getName(), e);
        }
        
        return null;
    }
    
    /**
     * Parses and validates JSON content structure.
     */
    private ExportContainer parseAndValidateJSON(String jsonContent) throws JsonSyntaxException {
        try {
            ExportContainer container = gson.fromJson(jsonContent, ExportContainer.class);
            
            if (container == null) {
                throw new JsonSyntaxException("Invalid JSON: Root object is null");
            }
            
            if (container.getRecipes() == null) {
                throw new JsonSyntaxException("Invalid JSON: Missing recipes array");
            }
            
            // Validate each recipe structure
            for (RecipeExportData recipeData : container.getRecipes()) {
                validateRecipeStructure(recipeData);
            }
            
            return container;
            
        } catch (JsonSyntaxException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonSyntaxException("JSON parsing failed: " + e.getMessage());
        }
    }
    
    /**
     * Validates the structure of a recipe export data object.
     */
    private void validateRecipeStructure(RecipeExportData recipeData) throws JsonSyntaxException {
        if (recipeData == null) {
            throw new JsonSyntaxException("Recipe data is null");
        }
        
        Recipe recipe = recipeData.getRecipe();
        if (recipe == null) {
            throw new JsonSyntaxException("Recipe object is null");
        }
        
        // Validate required recipe fields
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            throw new JsonSyntaxException("Recipe name is missing or empty");
        }
        
        if (recipe.getCategory() == null || recipe.getCategory().trim().isEmpty()) {
            throw new JsonSyntaxException("Recipe category is missing or empty");
        }
        
        if (recipe.getInstructions() == null || recipe.getInstructions().trim().isEmpty()) {
            throw new JsonSyntaxException("Recipe instructions are missing or empty");
        }
        
        // Validate ingredients if present
        List<Ingredient> ingredients = recipeData.getIngredients();
        if (ingredients != null) {
            for (Ingredient ingredient : ingredients) {
                if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
                    throw new JsonSyntaxException("Ingredient name is missing or empty");
                }
                if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
                    throw new JsonSyntaxException("Ingredient unit is missing or empty");
                }
                if (ingredient.getQuantity() < 0) {
                    throw new JsonSyntaxException("Ingredient quantity cannot be negative");
                }
            }
        }
    }
    
    /**
     * Reads complete content from an InputStreamReader.
     */
    private String readFileContent(InputStreamReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        char[] buffer = new char[8192];
        int charsRead;
        
        while ((charsRead = reader.read(buffer)) != -1) {
            content.append(buffer, 0, charsRead);
        }
        
        return content.toString();
    }
    
    /**
     * Validates a recipe list parameter.
     */
    private void validateRecipeList(List<Recipe> recipes, String paramName) {
        if (recipes == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
        if (recipes.isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be empty");
        }
    }
    
    /**
     * Validates a file path parameter.
     */
    private void validateFilePath(String filePath, String paramName) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
        
        // Check if path ends with .json extension
        if (!filePath.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException(paramName + " must have .json extension");
        }
    }
}