package ui;

import backend.RecipeManager;
import backend.IngredientManager;
import backend.TimerManager;
import backend.Recipe;
import backend.Ingredient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class RecipeEditorPage extends JFrame {
    private JTextField txtRecipeName;
    private JComboBox<String> cmbCategory;
    private JSpinner spnPrepTime;
    private JSpinner spnCookTime;
    private JTextArea txtInstructions;
    private JScrollPane instructionsScrollPane;
    private JTable ingredientTable;
    private DefaultTableModel ingredientTableModel;
    private JScrollPane ingredientScrollPane;
    
    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnAddIngredient;
    private JButton btnEditIngredient;
    private JButton btnRemoveIngredient;
    private JButton btnStartTimer;
    private JButton btnStopTimer;
    private JLabel lblTimer;
    private JLabel lblTimerStatus;
    private JLabel lblErrorMessage;
    
    private RecipeManager recipeManager;
    private IngredientManager ingredientManager;
    private TimerManager timerManager;
    private Recipe currentRecipe;
    private int userId;
    private HomePage parentPage;
    private boolean isEditMode;
    private List<Ingredient> ingredients;
    private Timer uiTimer;
    
    // Predefined categories
    private final String[] categories = {
        "Appetizer", "Main Course", "Side Dish", "Dessert", "Beverage", 
        "Breakfast", "Lunch", "Dinner", "Snack", "Soup", "Salad", "Other"
    };
    
    public RecipeEditorPage(int userId, Recipe recipe, HomePage parent) {
        this.userId = userId;
        this.currentRecipe = recipe;
        this.parentPage = parent;
        this.isEditMode = (recipe != null);
        this.ingredients = new ArrayList<>();
        
        this.recipeManager = new RecipeManager();
        this.ingredientManager = new IngredientManager();
        this.timerManager = new TimerManager();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        if (isEditMode) {
            loadRecipeData();
            loadIngredients();
        }
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle(isEditMode ? "Edit Recipe" : "Add New Recipe");
        setSize(800, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
        
        // Handle window closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleCancel();
            }
        });
    }
    
    private void initializeComponents() {
        // Recipe name field
        txtRecipeName = new JTextField(25);
        txtRecipeName.setFont(new Font("Arial", Font.PLAIN, 14));
        txtRecipeName.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Category combo box
        cmbCategory = new JComboBox<>(categories);
        cmbCategory.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbCategory.setEditable(true);
        
        // Time spinners
        spnPrepTime = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        spnPrepTime.setFont(new Font("Arial", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spnPrepTime.getEditor()).getTextField().setColumns(5);
        
        spnCookTime = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        spnCookTime.setFont(new Font("Arial", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spnCookTime.getEditor()).getTextField().setColumns(5);
        
        // Instructions text area
        txtInstructions = new JTextArea(8, 40);
        txtInstructions.setFont(new Font("Arial", Font.PLAIN, 14));
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        txtInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        instructionsScrollPane = new JScrollPane(txtInstructions);
        instructionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        instructionsScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Ingredient table
        String[] columnNames = {"Ingredient", "Quantity", "Unit"};
        ingredientTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ingredientTable = new JTable(ingredientTableModel);
        ingredientTable.setFont(new Font("Arial", Font.PLAIN, 13));
        ingredientTable.setRowHeight(25);
        ingredientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ingredientTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        ingredientTable.getTableHeader().setBackground(new Color(240, 240, 240));
        
        ingredientScrollPane = new JScrollPane(ingredientTable);
        ingredientScrollPane.setPreferredSize(new Dimension(400, 200));
        
        // Action buttons
        btnSave = createStyledButton("Save Recipe", new Color(70, 130, 180));
        btnCancel = createStyledButton("Cancel", new Color(128, 128, 128));
        
        btnAddIngredient = createStyledButton("Add Ingredient", new Color(95, 158, 160));
        btnEditIngredient = createStyledButton("Edit Ingredient", new Color(255, 165, 0));
        btnRemoveIngredient = createStyledButton("Remove Ingredient", new Color(220, 20, 60));
        
        btnStartTimer = createStyledButton("Start Cooking Timer", new Color(34, 139, 34));
        btnStopTimer = createStyledButton("Stop Timer", new Color(220, 20, 60));
        
        // Initially disable some buttons
        btnEditIngredient.setEnabled(false);
        btnRemoveIngredient.setEnabled(false);
        btnStopTimer.setEnabled(false);
        
        // Timer display
        lblTimer = new JLabel("00:00:00");
        lblTimer.setFont(new Font("Courier New", Font.BOLD, 24));
        lblTimer.setForeground(new Color(70, 130, 180));
        lblTimer.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblTimerStatus = new JLabel("Timer not started");
        lblTimerStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTimerStatus.setForeground(Color.GRAY);
        lblTimerStatus.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Error message label
        lblErrorMessage = new JLabel(" ");
        lblErrorMessage.setForeground(Color.RED);
        lblErrorMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        lblErrorMessage.setHorizontalAlignment(SwingConstants.CENTER);
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
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Recipe" : "Add New Recipe");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Recipe Information Panel
        JPanel recipeInfoPanel = createRecipeInfoPanel();
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(recipeInfoPanel, gbc);
        
        // Instructions Panel
        JPanel instructionsPanel = createInstructionsPanel();
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 0.4;
        mainPanel.add(instructionsPanel, gbc);
        
        // Ingredients Panel
        JPanel ingredientsPanel = createIngredientsPanel();
        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 15, 0);
        gbc.weightx = 0.4;
        mainPanel.add(ingredientsPanel, gbc);
        
        // Timer Panel
        JPanel timerPanel = createTimerPanel();
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        mainPanel.add(timerPanel, gbc);
        
        // Error message
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(lblErrorMessage, gbc);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createRecipeInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Recipe Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Recipe Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Recipe Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtRecipeName, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cmbCategory, gbc);
        
        // Prep Time
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Prep Time (min):"), gbc);
        gbc.gridx = 3;
        panel.add(spnPrepTime, gbc);
        
        // Cook Time
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Cook Time (min):"), gbc);
        gbc.gridx = 1;
        panel.add(spnCookTime, gbc);
        
        return panel;
    }
    
    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Instructions",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        panel.add(instructionsScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createIngredientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Ingredients",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        panel.add(ingredientScrollPane, BorderLayout.CENTER);
        
        // Ingredient buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddIngredient);
        buttonPanel.add(btnEditIngredient);
        buttonPanel.add(btnRemoveIngredient);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTimerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Cooking Timer",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        // Timer display
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBackground(Color.WHITE);
        displayPanel.add(lblTimer, BorderLayout.CENTER);
        displayPanel.add(lblTimerStatus, BorderLayout.SOUTH);
        
        // Timer buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnStartTimer);
        buttonPanel.add(btnStopTimer);
        
        panel.add(displayPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Save button
        btnSave.addActionListener(e -> saveRecipe());
        
        // Cancel button
        btnCancel.addActionListener(e -> handleCancel());
        
        // Ingredient management buttons
        btnAddIngredient.addActionListener(e -> addIngredient());
        btnEditIngredient.addActionListener(e -> editIngredient());
        btnRemoveIngredient.addActionListener(e -> removeIngredient());
        
        // Timer buttons
        btnStartTimer.addActionListener(e -> startCookingTimer());
        btnStopTimer.addActionListener(e -> stopCookingTimer());
        
        // Table selection listener
        ingredientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = ingredientTable.getSelectedRow() != -1;
                btnEditIngredient.setEnabled(hasSelection);
                btnRemoveIngredient.setEnabled(hasSelection);
            }
        });
        
        // Double-click to edit ingredient
        ingredientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && ingredientTable.getSelectedRow() != -1) {
                    editIngredient();
                }
            }
        });
        
        // Clear error message when user starts typing
        txtRecipeName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                clearErrorMessage();
            }
        });
    }
    
    private void loadRecipeData() {
        if (currentRecipe != null) {
            txtRecipeName.setText(currentRecipe.getName());
            cmbCategory.setSelectedItem(currentRecipe.getCategory());
            spnPrepTime.setValue(currentRecipe.getPrepTime());
            spnCookTime.setValue(currentRecipe.getCookTime());
            txtInstructions.setText(currentRecipe.getInstructions());
        }
    }
    
    private void loadIngredients() {
        if (isEditMode && currentRecipe != null) {
            try {
                ingredients = ingredientManager.getIngredientsByRecipe(currentRecipe.getRecipeId());
                if (ingredients == null) {
                    ingredients = new ArrayList<>();
                }
                updateIngredientTable();
            } catch (Exception e) {
                showError("Failed to load ingredients: " + e.getMessage());
                ingredients = new ArrayList<>();
                e.printStackTrace();
            }
        }
    }
    
    private void updateIngredientTable() {
        ingredientTableModel.setRowCount(0);
        
        for (Ingredient ingredient : ingredients) {
            Object[] rowData = {
                ingredient.getName(),
                ingredient.getQuantity(),
                ingredient.getUnit()
            };
            ingredientTableModel.addRow(rowData);
        }
    }
    
    private void addIngredient() {
        IngredientDialog dialog = new IngredientDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Ingredient newIngredient = dialog.getIngredient();
            ingredients.add(newIngredient);
            updateIngredientTable();
            clearErrorMessage();
        }
    }
    
    private void editIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an ingredient to edit.");
            return;
        }
        
        Ingredient selectedIngredient = ingredients.get(selectedRow);
        IngredientDialog dialog = new IngredientDialog(this, selectedIngredient);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Ingredient updatedIngredient = dialog.getIngredient();
            ingredients.set(selectedRow, updatedIngredient);
            updateIngredientTable();
            clearErrorMessage();
        }
    }
    
    private void removeIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an ingredient to remove.");
            return;
        }
        
        String ingredientName = (String) ingredientTableModel.getValueAt(selectedRow, 0);
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove '" + ingredientName + "'?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            ingredients.remove(selectedRow);
            updateIngredientTable();
            clearErrorMessage();
        }
    }
    
    private void saveRecipe() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Create or update recipe object
            Recipe recipe;
            if (isEditMode) {
                recipe = currentRecipe;
                recipe.setName(txtRecipeName.getText().trim());
                recipe.setCategory((String) cmbCategory.getSelectedItem());
                recipe.setPrepTime((Integer) spnPrepTime.getValue());
                recipe.setCookTime((Integer) spnCookTime.getValue());
                recipe.setInstructions(txtInstructions.getText().trim());
            } else {
                recipe = new Recipe(
                    0, // ID will be set by database
                    userId,
                    txtRecipeName.getText().trim(),
                    (String) cmbCategory.getSelectedItem(),
                    (Integer) spnPrepTime.getValue(),
                    (Integer) spnCookTime.getValue(),
                    txtInstructions.getText().trim()
                );
            }
            
            // Save recipe
            boolean recipeSuccess;
            if (isEditMode) {
                recipeSuccess = recipeManager.updateRecipe(recipe);
            } else {
                recipeSuccess = recipeManager.addRecipe(recipe);
            }
            
            if (!recipeSuccess) {
                showError("Failed to save recipe to database.");
                return;
            }
            
            // Save ingredients
            boolean ingredientsSuccess = saveIngredients(recipe.getRecipeId());
            
            if (ingredientsSuccess) {
                showSuccess("Recipe saved successfully!");
                
                // Refresh parent page and close this window
                if (parentPage != null) {
                    parentPage.refreshData();
                }
                
                // Close after short delay to show success message
                Timer closeTimer = new Timer(1500, e -> dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
            } else {
                showError("Recipe saved, but failed to save some ingredients.");
            }
            
        } catch (Exception e) {
            showError("Failed to save recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean saveIngredients(int recipeId) {
        try {
            if (isEditMode) {
                // Delete existing ingredients first
                ingredientManager.deleteIngredientsByRecipe(recipeId);
            }
            
            // Add all current ingredients
            for (Ingredient ingredient : ingredients) {
                ingredient.setRecipeId(recipeId);
                boolean success = ingredientManager.addIngredient(ingredient);
                if (!success) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void startCookingTimer() {
        try {
            int totalMinutes = (Integer) spnPrepTime.getValue() + (Integer) spnCookTime.getValue();
            if (totalMinutes <= 0) {
                showError("Please set prep time and/or cook time before starting timer.");
                return;
            }
            
            int totalSeconds = totalMinutes * 60;
            timerManager.startTimer(totalSeconds);
            
            btnStartTimer.setEnabled(false);
            btnStopTimer.setEnabled(true);
            lblTimerStatus.setText("Timer running - " + totalMinutes + " minutes");
            
            // UI timer to update display
            uiTimer = new Timer(1000, e -> updateTimerDisplay());
            uiTimer.start();
            
            clearErrorMessage();
        } catch (Exception e) {
            showError("Failed to start timer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void stopCookingTimer() {
        try {
            timerManager.stopTimer();
            
            if (uiTimer != null) {
                uiTimer.stop();
            }
            
            btnStartTimer.setEnabled(true);
            btnStopTimer.setEnabled(false);
            lblTimer.setText("00:00:00");
            lblTimerStatus.setText("Timer stopped");
            
            clearErrorMessage();
        } catch (Exception e) {
            showError("Failed to stop timer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTimerDisplay() {
        try {
            int remainingSeconds = timerManager.getRemainingTime();
            
            if (remainingSeconds <= 0) {
                // Timer finished
                if (uiTimer != null) {
                    uiTimer.stop();
                }
                
                btnStartTimer.setEnabled(true);
                btnStopTimer.setEnabled(false);
                lblTimer.setText("00:00:00");
                lblTimerStatus.setText("Timer finished!");
                
                // Show completion message
                JOptionPane.showMessageDialog(
                    this,
                    "Cooking timer finished!",
                    "Timer Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                return;
            }
            
            // Format time display
            int hours = remainingSeconds / 3600;
            int minutes = (remainingSeconds % 3600) / 60;
            int seconds = remainingSeconds % 60;
            
            String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            lblTimer.setText(timeText);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean validateInput() {
        clearErrorMessage();
        
        String recipeName = txtRecipeName.getText().trim();
        if (recipeName.isEmpty()) {
            showError("Please enter a recipe name.");
            txtRecipeName.requestFocus();
            return false;
        }
        
        if (recipeName.length() < 2) {
            showError("Recipe name must be at least 2 characters long.");
            txtRecipeName.requestFocus();
            return false;
        }
        
        String category = (String) cmbCategory.getSelectedItem();
        if (category == null || category.trim().isEmpty()) {
            showError("Please select or enter a category.");
            cmbCategory.requestFocus();
            return false;
        }
        
        int prepTime = (Integer) spnPrepTime.getValue();
        int cookTime = (Integer) spnCookTime.getValue();
        if (prepTime + cookTime <= 0) {
            showError("Please enter prep time and/or cook time.");
            spnPrepTime.requestFocus();
            return false;
        }
        
        String instructions = txtInstructions.getText().trim();
        if (instructions.isEmpty()) {
            showError("Please enter cooking instructions.");
            txtInstructions.requestFocus();
            return false;
        }
        
        if (ingredients.isEmpty()) {
            showError("Please add at least one ingredient.");
            return false;
        }
        
        return true;
    }
    
    private void handleCancel() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel? Any unsaved changes will be lost.",
            "Confirm Cancel",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Stop timer if running
            if (timerManager != null && btnStopTimer.isEnabled()) {
                stopCookingTimer();
            }
            dispose();
        }
    }
    
    private void showError(String message) {
        lblErrorMessage.setForeground(Color.RED);
        lblErrorMessage.setText(message);
    }
    
    private void showSuccess(String message) {
        lblErrorMessage.setForeground(new Color(0, 128, 0));
        lblErrorMessage.setText(message);
    }
    
    private void clearErrorMessage() {
        lblErrorMessage.setText(" ");
    }
    
    // Inner class for ingredient dialog
    private class IngredientDialog extends JDialog {
        private JTextField txtName;
        private JSpinner spnQuantity;
        private JComboBox<String> cmbUnit;
        private JButton btnOK;
        private JButton btnCancel;
        private boolean confirmed = false;
        private Ingredient ingredient;
        
        private final String[] units = {
            "cup", "cups", "tbsp", "tsp", "oz", "lb", "g", "kg", "ml", "l", 
            "piece", "pieces", "slice", "slices", "clove", "cloves", "pinch", "dash", "to taste"
        };
        
        public IngredientDialog(JFrame parent, Ingredient ingredient) {
            super(parent, ingredient == null ? "Add Ingredient" : "Edit Ingredient", true);
            this.ingredient = ingredient;
            
            initializeComponents();
            setupLayout();
            setupEventListeners();
            
            if (ingredient != null) {
                loadIngredientData();
            }
            
            setSize(400, 300);
            setLocationRelativeTo(parent);
            setResizable(false);
        }
        
        private void initializeComponents() {
            txtName = new JTextField(20);
            txtName.setFont(new Font("Arial", Font.PLAIN, 14));
            
            spnQuantity = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 999.0, 0.1));
            spnQuantity.setFont(new Font("Arial", Font.PLAIN, 14));
            ((JSpinner.DefaultEditor) spnQuantity.getEditor()).getTextField().setColumns(8);
            
            cmbUnit = new JComboBox<>(units);
            cmbUnit.setFont(new Font("Arial", Font.PLAIN, 14));
            cmbUnit.setEditable(true);
            
            btnOK = new JButton("OK");
            btnOK.setFont(new Font("Arial", Font.BOLD, 12));
            btnOK.setBackground(new Color(70, 130, 180));
            btnOK.setForeground(Color.WHITE);
            btnOK.setBorderPainted(false);
            
            btnCancel = new JButton("Cancel");
            btnCancel.setFont(new Font("Arial", Font.BOLD, 12));
            btnCancel.setBackground(Color.GRAY);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setBorderPainted(false);
        }
        
        private void setupLayout() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Name
            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Ingredient Name:"), gbc);
            gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtName, gbc);
            
            // Quantity
            gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
            add(new JLabel("Quantity:"), gbc);
            gbc.gridy = 3;
            add(spnQuantity, gbc);
            
            // Unit
            gbc.gridy = 4;
            add(new JLabel("Unit:"), gbc);
            gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
            add(cmbUnit, gbc);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(btnOK);
            buttonPanel.add(btnCancel);
            gbc.gridy = 6; gbc.insets = new Insets(15, 10, 10, 10);
            add(buttonPanel, gbc);
        }
        
        private void setupEventListeners() {
            btnOK.addActionListener(e -> handleOK());
            btnCancel.addActionListener(e -> dispose());
            
            // Enter key for OK
            getRootPane().setDefaultButton(btnOK);
            
            // Escape key for cancel
            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, false);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
            getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }
        
        private void loadIngredientData() {
            if (ingredient != null) {
                txtName.setText(ingredient.getName());
                spnQuantity.setValue(ingredient.getQuantity());
                cmbUnit.setSelectedItem(ingredient.getUnit());
            }
        }
        
        private void handleOK() {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an ingredient name.", "Error", JOptionPane.ERROR_MESSAGE);
                txtName.requestFocus();
                return;
            }
            
            double quantity = (Double) spnQuantity.getValue();
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                spnQuantity.requestFocus();
                return;
            }
            
            String unit = (String) cmbUnit.getSelectedItem();
            if (unit == null || unit.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select or enter a unit.", "Error", JOptionPane.ERROR_MESSAGE);
                cmbUnit.requestFocus();
                return;
            }
            
            // Create ingredient object with proper recipe ID
            if (ingredient == null) {
                // For new ingredients, use a temporary recipe ID of 1 (will be updated when recipe is saved)
                // This prevents the validation error while still allowing the ingredient to be created
                int tempRecipeId = isEditMode && currentRecipe != null ? currentRecipe.getRecipeId() : 1;
                ingredient = new Ingredient(0, tempRecipeId, name, quantity, unit.trim());
            } else {
                ingredient.setName(name);
                ingredient.setQuantity(quantity);
                ingredient.setUnit(unit.trim());
            }
            
            confirmed = true;
            dispose();
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Ingredient getIngredient() {
            return ingredient;
        }
    }
}