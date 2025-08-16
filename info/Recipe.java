package backend;

/**
 * Represents a recipe in the Recipe Manager system.
 * Contains recipe information including ingredients, cooking times, and instructions.
 */
public class Recipe {
    private int recipeId;
    private int userId;
    private String name;
    private String category;
    private int prepTime;
    private int cookTime;
    private String instructions;
    
    /**
     * Default constructor for Recipe.
     * Initializes a new Recipe with default values.
     */
    public Recipe() {
        this.recipeId = 0;
        this.userId = 0;
        this.name = "";
        this.category = "";
        this.prepTime = 0;
        this.cookTime = 0;
        this.instructions = "";
    }
    
    /**
     * Constructor for creating a Recipe with all fields.
     * Used when loading existing recipes from database.
     * 
     * @param recipeId     The unique identifier for the recipe
     * @param userId       The ID of the user who created the recipe
     * @param name         The name of the recipe
     * @param category     The category of the recipe
     * @param prepTime     The preparation time in minutes
     * @param cookTime     The cooking time in minutes
     * @param instructions The cooking instructions
     */
    public Recipe(int recipeId, int userId, String name, String category, 
                  int prepTime, int cookTime, String instructions) {
        setRecipeId(recipeId);
        setUserId(userId);
        setName(name);
        setCategory(category);
        setPrepTime(prepTime);
        setCookTime(cookTime);
        setInstructions(instructions);
    }
    
    /**
     * Constructor for creating a new Recipe without ID.
     * Used when creating new recipes (ID will be auto-generated).
     * 
     * @param userId       The ID of the user creating the recipe
     * @param name         The name of the recipe
     * @param category     The category of the recipe
     * @param prepTime     The preparation time in minutes
     * @param cookTime     The cooking time in minutes
     * @param instructions The cooking instructions
     */
    public Recipe(int userId, String name, String category, 
                  int prepTime, int cookTime, String instructions) {
        this(0, userId, name, category, prepTime, cookTime, instructions);
    }
    
    /**
     * Gets the recipe's unique identifier.
     * 
     * @return The recipe ID
     */
    public int getRecipeId() {
        return recipeId;
    }
    
    /**
     * Sets the recipe's unique identifier.
     * 
     * @param recipeId The recipe ID (must be non-negative)
     * @throws IllegalArgumentException if recipeId is negative
     */
    public void setRecipeId(int recipeId) {
        if (recipeId < 0) {
            throw new IllegalArgumentException("Recipe ID cannot be negative");
        }
        this.recipeId = recipeId;
    }
    
    /**
     * Gets the ID of the user who created this recipe.
     * 
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }
    
    /**
     * Sets the ID of the user who created this recipe.
     * 
     * @param userId The user ID (must be positive)
     * @throws IllegalArgumentException if userId is not positive
     */
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.userId = userId;
    }
    
    /**
     * Gets the recipe name.
     * 
     * @return The recipe name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the recipe name with validation.
     * 
     * @param name The recipe name (1-200 characters)
     * @throws IllegalArgumentException if name is invalid
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() > 200) {
            throw new IllegalArgumentException("Recipe name cannot exceed 200 characters");
        }
        
        this.name = trimmedName;
    }
    
    /**
     * Gets the recipe category.
     * 
     * @return The recipe category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the recipe category with validation.
     * 
     * @param category The recipe category (1-50 characters)
     * @throws IllegalArgumentException if category is invalid
     */
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe category cannot be null or empty");
        }
        
        String trimmedCategory = category.trim();
        if (trimmedCategory.length() > 50) {
            throw new IllegalArgumentException("Recipe category cannot exceed 50 characters");
        }
        
        this.category = trimmedCategory;
    }
    
    /**
     * Gets the preparation time in minutes.
     * 
     * @return The preparation time
     */
    public int getPrepTime() {
        return prepTime;
    }
    
    /**
     * Sets the preparation time with validation.
     * 
     * @param prepTime The preparation time in minutes (0-1440)
     * @throws IllegalArgumentException if prepTime is invalid
     */
    public void setPrepTime(int prepTime) {
        if (prepTime < 0) {
            throw new IllegalArgumentException("Preparation time cannot be negative");
        }
        if (prepTime > 1440) { // 24 hours max
            throw new IllegalArgumentException("Preparation time cannot exceed 24 hours (1440 minutes)");
        }
        this.prepTime = prepTime;
    }
    
    /**
     * Gets the cooking time in minutes.
     * 
     * @return The cooking time
     */
    public int getCookTime() {
        return cookTime;
    }
    
    /**
     * Sets the cooking time with validation.
     * 
     * @param cookTime The cooking time in minutes (0-1440)
     * @throws IllegalArgumentException if cookTime is invalid
     */
    public void setCookTime(int cookTime) {
        if (cookTime < 0) {
            throw new IllegalArgumentException("Cooking time cannot be negative");
        }
        if (cookTime > 1440) { // 24 hours max
            throw new IllegalArgumentException("Cooking time cannot exceed 24 hours (1440 minutes)");
        }
        this.cookTime = cookTime;
    }
    
    /**
     * Gets the total time (prep + cook) in minutes.
     * 
     * @return The total time in minutes
     */
    public int getTotalTime() {
        return prepTime + cookTime;
    }
    
    /**
     * Gets the cooking instructions.
     * 
     * @return The cooking instructions
     */
    public String getInstructions() {
        return instructions;
    }
    
    /**
     * Sets the cooking instructions with validation.
     * 
     * @param instructions The cooking instructions (cannot be empty)
     * @throws IllegalArgumentException if instructions are invalid
     */
    public void setInstructions(String instructions) {
        if (instructions == null || instructions.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructions cannot be null or empty");
        }
        
        String trimmedInstructions = instructions.trim();
        if (trimmedInstructions.length() > 5000) {
            throw new IllegalArgumentException("Instructions cannot exceed 5000 characters");
        }
        
        this.instructions = trimmedInstructions;
    }
    
    /**
     * Returns a string representation of the Recipe object.
     * 
     * @return String representation of the recipe
     */
    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", prepTime=" + prepTime +
                ", cookTime=" + cookTime +
                ", totalTime=" + getTotalTime() +
                ", instructions='" + (instructions.length() > 50 ? 
                    instructions.substring(0, 50) + "..." : instructions) + '\'' +
                '}';
    }
    
    /**
     * Checks if two Recipe objects are equal based on recipeId.
     * 
     * @param obj The object to compare
     * @return true if recipes have the same recipeId, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Recipe recipe = (Recipe) obj;
        return recipeId == recipe.recipeId;
    }
    
    /**
     * Generates hash code based on recipeId.
     * 
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(recipeId);
    }
}