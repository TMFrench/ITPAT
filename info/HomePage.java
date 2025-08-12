package ui;

import backend.RecipeManager;
import backend.Recipe;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class HomePage extends JFrame {
    private JTable recipeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JPanel categoryPanel;
    private JTextField searchField;
    private JButton btnAddRecipe;
    private JButton btnEditRecipe;
    private JButton btnDeleteRecipe;
    private JButton btnSettings;
    private JButton btnLogout;
    private JLabel lblWelcome;
    private JLabel lblRecipeCount;
    private RecipeManager recipeManager;
    private int currentUserId;
    private List<Recipe> allRecipes;
    private List<String> allCategories;
    private String selectedCategory = "All";
    
    public HomePage(int userId) {
        this.currentUserId = userId;
        this.recipeManager = new RecipeManager();
        this.allRecipes = new ArrayList<>();
        this.allCategories = new ArrayList<>();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadData();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Recipe Manager - Home");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void initializeComponents() {
        // Welcome label
        lblWelcome = new JLabel("Welcome to Recipe Manager!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(70, 130, 180));
        
        // Recipe count label
        lblRecipeCount = new JLabel("0 recipes");
        lblRecipeCount.setFont(new Font("Arial", Font.PLAIN, 14));
        lblRecipeCount.setForeground(Color.GRAY);
        
        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Category panel
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        categoryPanel.setBackground(Color.WHITE);
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        
        // Recipe table setup
        String[] columnNames = {"Recipe Name", "Category", "Prep Time (min)", "Cook Time (min)", "Total Time (min)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        recipeTable = new JTable(tableModel);
        recipeTable.setFont(new Font("Arial", Font.PLAIN, 14));
        recipeTable.setRowHeight(25);
        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        recipeTable.getTableHeader().setBackground(new Color(240, 240, 240));
        
        // Table sorter for search functionality
        tableSorter = new TableRowSorter<>(tableModel);
        recipeTable.setRowSorter(tableSorter);
        
        // Action buttons
        btnAddRecipe = createStyledButton("Add Recipe", new Color(70, 130, 180));
        btnEditRecipe = createStyledButton("Edit Recipe", new Color(95, 158, 160));
        btnDeleteRecipe = createStyledButton("Delete Recipe", new Color(220, 20, 60));
        btnSettings = createStyledButton("Settings", new Color(128, 128, 128));
        btnLogout = createStyledButton("Logout", new Color(169, 169, 169));
        
        // Initially disable edit and delete buttons
        btnEditRecipe.setEnabled(false);
        btnDeleteRecipe.setEnabled(false);
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(120, 35));
        
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
        
        // Top panel with welcome and search
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.add(lblWelcome);
        welcomePanel.add(Box.createHorizontalStrut(20));
        welcomePanel.add(lblRecipeCount);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchField);
        
        topPanel.add(welcomePanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        // Category panel
        JPanel categoryContainer = new JPanel(new BorderLayout());
        categoryContainer.setBackground(Color.WHITE);
        categoryContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        categoryContainer.add(categoryPanel, BorderLayout.CENTER);
        
        // Main content panel with table
        JScrollPane tableScrollPane = new JScrollPane(recipeTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnAddRecipe);
        buttonPanel.add(btnEditRecipe);
        buttonPanel.add(btnDeleteRecipe);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnSettings);
        buttonPanel.add(btnLogout);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add all panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(categoryContainer, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.SOUTH);
        
        // Adjust layout weights
        topPanel.setPreferredSize(new Dimension(0, 60));
        categoryContainer.setPreferredSize(new Dimension(0, 80));
    }
    
    private void setupEventListeners() {
        // Add recipe button
        btnAddRecipe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRecipeEditor();
            }
        });
        
        // Edit recipe button
        btnEditRecipe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedRecipe();
            }
        });
        
        // Delete recipe button
        btnDeleteRecipe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedRecipe();
            }
        });
        
        // Settings button
        btnSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettings();
            }
        });
        
        // Logout button
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
        
        // Table selection listener
        recipeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = recipeTable.getSelectedRow() != -1;
                btnEditRecipe.setEnabled(hasSelection);
                btnDeleteRecipe.setEnabled(hasSelection);
            }
        });
        
        // Double-click to edit recipe
        recipeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && recipeTable.getSelectedRow() != -1) {
                    editSelectedRecipe();
                }
            }
        });
        
        // Search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
    }
    
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadRecipes();
                loadCategories();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    updateRecipeTable();
                    updateCategoryButtons();
                    updateRecipeCount();
                } catch (Exception e) {
                    showError("Failed to load data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void loadRecipes() {
        try {
            allRecipes = recipeManager.getRecipesByUserId(currentUserId);
            if (allRecipes == null) {
                allRecipes = new ArrayList<>();
            }
        } catch (Exception e) {
            showError("Failed to load recipes: " + e.getMessage());
            allRecipes = new ArrayList<>();
            e.printStackTrace();
        }
    }
    
    private void loadCategories() {
        try {
            Set<String> categorySet = new HashSet<>();
            for (Recipe recipe : allRecipes) {
                if (recipe.getCategory() != null && !recipe.getCategory().trim().isEmpty()) {
                    categorySet.add(recipe.getCategory().trim());
                }
            }
            allCategories = new ArrayList<>(categorySet);
            allCategories.sort(String::compareToIgnoreCase);
        } catch (Exception e) {
            showError("Failed to load categories: " + e.getMessage());
            allCategories = new ArrayList<>();
            e.printStackTrace();
        }
    }
    
    private void updateRecipeTable() {
        tableModel.setRowCount(0); // Clear existing data
        
        List<Recipe> recipesToShow = getFilteredRecipes();
        
        for (Recipe recipe : recipesToShow) {
            Object[] rowData = {
                recipe.getName(),
                recipe.getCategory(),
                recipe.getPrepTime(),
                recipe.getCookTime(),
                recipe.getPrepTime() + recipe.getCookTime()
            };
            tableModel.addRow(rowData);
        }
        
        updateRecipeCount();
    }
    
    private void updateCategoryButtons() {
        categoryPanel.removeAll();
        
        // Add "All" category button
        JButton allButton = createCategoryButton("All");
        allButton.setSelected(selectedCategory.equals("All"));
        categoryPanel.add(allButton);
        
        // Add category buttons
        for (String category : allCategories) {
            JButton categoryButton = createCategoryButton(category);
            categoryButton.setSelected(selectedCategory.equals(category));
            categoryPanel.add(categoryButton);
        }
        
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }
    
    private JButton createCategoryButton(String category) {
        JButton button = new JButton(category);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setFocusable(false);
        
        // Style based on selection
        if (selectedCategory.equals(category)) {
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        
        // Add action listener
        button.addActionListener(e -> filterRecipesByCategory(category));
        
        return button;
    }
    
    private void filterRecipesByCategory(String category) {
        selectedCategory = category;
        updateRecipeTable();
        updateCategoryButtons();
    }
    
    private List<Recipe> getFilteredRecipes() {
        List<Recipe> filteredRecipes = new ArrayList<>();
        
        for (Recipe recipe : allRecipes) {
            // Filter by category
            if (!selectedCategory.equals("All") && 
                !selectedCategory.equals(recipe.getCategory())) {
                continue;
            }
            
            filteredRecipes.add(recipe);
        }
        
        return filteredRecipes;
    }
    
    private void filterTable() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }
    
    private void updateRecipeCount() {
        int totalRecipes = allRecipes.size();
        int visibleRecipes = recipeTable.getRowCount();
        
        if (totalRecipes == visibleRecipes) {
            lblRecipeCount.setText(totalRecipes + " recipe" + (totalRecipes == 1 ? "" : "s"));
        } else {
            lblRecipeCount.setText(visibleRecipes + " of " + totalRecipes + " recipes");
        }
    }
    
    private void openRecipeEditor() {
        openRecipeEditor(null);
    }
    
    private void openRecipeEditor(Recipe recipe) {
        try {
            RecipeEditorPage editorPage = new RecipeEditorPage(currentUserId, recipe, this);
            editorPage.setVisible(true);
        } catch (Exception e) {
            showError("Failed to open recipe editor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editSelectedRecipe() {
        int selectedRow = recipeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a recipe to edit.");
            return;
        }
        
        try {
            // Convert view row to model row (important for filtered tables)
            int modelRow = recipeTable.convertRowIndexToModel(selectedRow);
            String recipeName = (String) tableModel.getValueAt(modelRow, 0);
            
            // Find the recipe object
            Recipe selectedRecipe = null;
            for (Recipe recipe : allRecipes) {
                if (recipe.getName().equals(recipeName)) {
                    selectedRecipe = recipe;
                    break;
                }
            }
            
            if (selectedRecipe != null) {
                openRecipeEditor(selectedRecipe);
            } else {
                showError("Could not find the selected recipe.");
            }
        } catch (Exception e) {
            showError("Failed to edit recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deleteSelectedRecipe() {
        int selectedRow = recipeTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a recipe to delete.");
            return;
        }
        
        try {
            // Convert view row to model row
            int modelRow = recipeTable.convertRowIndexToModel(selectedRow);
            String recipeName = (String) tableModel.getValueAt(modelRow, 0);
            
            // Confirmation dialog
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the recipe '" + recipeName + "'?\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Find the recipe object and delete it
                Recipe recipeToDelete = null;
                for (Recipe recipe : allRecipes) {
                    if (recipe.getName().equals(recipeName)) {
                        recipeToDelete = recipe;
                        break;
                    }
                }
                
                if (recipeToDelete != null) {
                    boolean success = recipeManager.deleteRecipe(recipeToDelete.getRecipeId());
                    if (success) {
                        showSuccess("Recipe deleted successfully!");
                        refreshData();
                    } else {
                        showError("Failed to delete recipe from database.");
                    }
                } else {
                    showError("Could not find the selected recipe.");
                }
            }
        } catch (Exception e) {
            showError("Failed to delete recipe: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void openSettings() {
        try {
            SettingsPage settingsPage = new SettingsPage(currentUserId);
            settingsPage.setVisible(true);
        } catch (Exception e) {
            showError("Failed to open settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // Close current window and return to sign-in
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    new SignInPage().setVisible(true);
                });
            } catch (Exception e) {
                showError("Failed to logout: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Method to refresh data (called by RecipeEditorPage after saving)
    public void refreshData() {
        loadData();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}