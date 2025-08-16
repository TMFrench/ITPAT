package backend;

/**
 * Represents an ingredient in a recipe.
 * Contains ingredient information including name, quantity, and unit of measurement.
 **/
public class Ingredient {
    private int ingredientId;
    private int recipeId;
    private String name;
    private double quantity;
    private String unit;
    
    /**
     * Default constructor for Ingredient.
     * Initializes a new Ingredient with default values.
     */
    public Ingredient() {
        this.ingredientId = 0;
        this.recipeId = 0;
        this.name = "";
        this.quantity = 0.0;
        this.unit = "";
    }
    
    /**
     * Constructor for creating an Ingredient with all fields.
     * Used when loading existing ingredients from database.
     * 
     * @param ingredientId The unique identifier for the ingredient
     * @param recipeId     The ID of the recipe this ingredient belongs to
     * @param name         The name of the ingredient
     * @param quantity     The amount of the ingredient
     * @param unit         The unit of measurement
     */
    public Ingredient(int ingredientId, int recipeId, String name, 
                     double quantity, String unit) {
        setIngredientId(ingredientId);
        setRecipeId(recipeId);
        setName(name);
        setQuantity(quantity);
        setUnit(unit);
    }
    
    /**
     * Constructor for creating a new Ingredient without ID.
     * Used when creating new ingredients (ID will be auto-generated).
     * 
     * @param recipeId The ID of the recipe this ingredient belongs to
     * @param name     The name of the ingredient
     * @param quantity The amount of the ingredient
     * @param unit     The unit of measurement
     */
    public Ingredient(int recipeId, String name, double quantity, String unit) {
        this(0, recipeId, name, quantity, unit);
    }
    
    /**
     * Gets the ingredient's unique identifier.
     * 
     * @return The ingredient ID
     */
    public int getIngredientId() {
        return ingredientId;
    }
    
    /**
     * Sets the ingredient's unique identifier.
     * 
     * @param ingredientId The ingredient ID (must be non-negative)
     * @throws IllegalArgumentException if ingredientId is negative
     */
    public void setIngredientId(int ingredientId) {
        if (ingredientId < 0) {
            throw new IllegalArgumentException("Ingredient ID cannot be negative");
        }
        this.ingredientId = ingredientId;
    }
    
    /**
     * Gets the ID of the recipe this ingredient belongs to.
     * 
     * @return The recipe ID
     */
    public int getRecipeId() {
        return recipeId;
    }
    
    /**
     * Sets the ID of the recipe this ingredient belongs to.
     * 
     * @param recipeId The recipe ID (must be positive)
     * @throws IllegalArgumentException if recipeId is not positive
     */
    public void setRecipeId(int recipeId) {
        if (recipeId <= 0) {
            throw new IllegalArgumentException("Recipe ID must be positive");
        }
        this.recipeId = recipeId;
    }
    
    /**
     * Gets the ingredient name.
     * 
     * @return The ingredient name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the ingredient name with validation.
     * 
     * @param name The ingredient name (1-100 characters)
     * @throws IllegalArgumentException if name is invalid
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException("Ingredient name cannot exceed 100 characters");
        }
        
        this.name = trimmedName;
    }
    
    /**
     * Gets the ingredient quantity.
     * 
     * @return The ingredient quantity
     */
    public double getQuantity() {
        return quantity;
    }
    
    /**
     * Sets the ingredient quantity with validation.
     * 
     * @param quantity The ingredient quantity (must be positive)
     * @throws IllegalArgumentException if quantity is not positive
     */
    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > 999999.99) {
            throw new IllegalArgumentException("Quantity cannot exceed 999999.99");
        }
        this.quantity = Math.round(quantity * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Gets the unit of measurement.
     * 
     * @return The unit of measurement
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Sets the unit of measurement with validation.
     * 
     * @param unit The unit of measurement (1-20 characters)
     * @throws IllegalArgumentException if unit is invalid
     */
    public void setUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be null or empty");
        }
        
        String trimmedUnit = unit.trim().toLowerCase();
        if (trimmedUnit.length() > 20) {
            throw new IllegalArgumentException("Unit cannot exceed 20 characters");
        }
        
        // Standardize common units
        switch (trimmedUnit) {
            case "cups":
            case "cup":
                this.unit = "cup";
                break;
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                this.unit = "tbsp";
                break;
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                this.unit = "tsp";
                break;
            case "oz":
            case "ounce":
            case "ounces":
                this.unit = "oz";
                break;
            case "lb":
            case "lbs":
            case "pound":
            case "pounds":
                this.unit = "lb";
                break;
            case "g":
            case "gram":
            case "grams":
                this.unit = "g";
                break;
            case "kg":
            case "kilogram":
            case "kilograms":
                this.unit = "kg";
                break;
            case "ml":
            case "milliliter":
            case "milliliters":
                this.unit = "ml";
                break;
            case "l":
            case "liter":
            case "liters":
                this.unit = "l";
                break;
            default:
                this.unit = trimmedUnit;
                break;
        }
    }
    
    /**
     * Gets a formatted string representation of the quantity and unit.
     * 
     * @return Formatted quantity and unit (e.g., "2.5 cups")
     */
    public String getFormattedQuantity() {
        if (quantity == (int) quantity) {
            return String.format("%d %s", (int) quantity, unit);
        } else {
            return String.format("%.2f %s", quantity, unit);
        }
    }
    
    /**
     * Returns a string representation of the Ingredient object.
     * 
     * @return String representation of the ingredient
     */
    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientId=" + ingredientId +
                ", recipeId=" + recipeId +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", formatted='" + getFormattedQuantity() + '\'' +
                '}';
    }
    
    /**
     * Checks if two Ingredient objects are equal based on ingredientId.
     * 
     * @param obj The object to compare
     * @return true if ingredients have the same ingredientId, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ingredient ingredient = (Ingredient) obj;
        return ingredientId == ingredient.ingredientId;
    }
    
    /**
     * Generates hash code based on ingredientId.
     * 
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(ingredientId);
    }
}