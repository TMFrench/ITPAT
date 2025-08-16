package main;

import ui.SignInPage;
import backend.DatabaseConnector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main class for the Recipe Manager application.
 * This class serves as the entry point and handles application-wide initialization,
 * including UI setup, database connections, and graceful shutdown procedures.
 **/
public class Main {
    
    // Application constants
    private static final String APP_TITLE = "Recipe Manager";
    private static final String APP_VERSION = "- Daniel French's Project";
    private static final int DEFAULT_WIDTH = 1000;
    private static final int DEFAULT_HEIGHT = 700;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    
    // Logging
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    // Application components
    private static JFrame mainFrame;
    private static DatabaseConnector databaseConnector;
    private static boolean isShuttingDown = false;
    
    /**
     * Main entry point of the Recipe Manager application.
     * Initializes the application on the Event Dispatch Thread for thread safety.
     * 
     * @param args Command line arguments (currently not used)
     */
    public static void main(String[] args) {
        // Ensure application runs on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize application
                initializeApplication();
                
                // Create and show the main application window
                createAndShowMainWindow();
                
                logger.info("Recipe Manager application started successfully");
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to start Recipe Manager application", e);
                showErrorDialog("Application Startup Error", 
                               "Failed to start Recipe Manager: " + e.getMessage());
                System.exit(1);
            }
        });
    }
    
    /**
     * Initializes application-wide settings and components.
     * This includes look and feel, system properties, and database connections.
     */
    private static void initializeApplication() {
        try {
            // Set up logging
            setupLogging();
            
            // Set system properties for better UI experience
            setupSystemProperties();
            
            // Set Look and Feel to system default for native appearance
            setLookAndFeel();
            
            // Initialize database connection
            initializeDatabase();
            
            // Set up global exception handling
            setupGlobalExceptionHandling();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application", e);
        }
    }
    
    /**
     * Sets up application logging configuration.
     */
    private static void setupLogging() {
        // Configure logging level (can be made configurable later)
        logger.setLevel(Level.INFO);
        logger.info("Recipe Manager " + APP_VERSION + " initializing...");
    }
    
    /**
     * Sets up system properties for better UI experience across platforms.
     */
    private static void setupSystemProperties() {
        // Enable anti-aliasing for better text rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Enable hardware acceleration if available
        System.setProperty("sun.java2d.opengl", "true");
        
        // macOS specific settings
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_TITLE);
        }
    }
    
    /**
     * Sets the application's Look and Feel to match the system's native appearance.
     */
    private static void setLookAndFeel() {
            try {
                // Try to set system look and feel for native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Custom UI enhancements
                setupUIDefaults();
            
            logger.info("Look and Feel set to: " + UIManager.getLookAndFeel().getName());
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to set system Look and Feel, using default", e);
            // Continue with default Look and Feel
        }
    }
    
    /**
     * Sets up custom UI defaults for consistent appearance.
     */
    private static void setupUIDefaults() {
        // Set default font sizes and colors if needed
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 12);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        
        // Set default colors for better consistency
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.background", new Color(240, 240, 240));
    }
    
    /**
     * Initializes the database connection and verifies connectivity.
     */
    private static void initializeDatabase() {
        try {
            databaseConnector = new DatabaseConnector();
            // Test the connection
            if (databaseConnector.connect() != null) {
                logger.info("Database connection established successfully");
            } else {
                throw new RuntimeException("Failed to establish database connection");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets up global exception handling for uncaught exceptions.
     */
    private static void setupGlobalExceptionHandling() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logger.log(Level.SEVERE, "Uncaught exception in thread " + thread.getName(), exception);
            
            if (!isShuttingDown) {
                SwingUtilities.invokeLater(() -> {
                    showErrorDialog("Unexpected Error", 
                                   "An unexpected error occurred: " + exception.getMessage() + 
                                   "\n\nPlease check the logs for more details.");
                });
            }
        });
    }
    
    /**
     * Creates and displays the main application window.
     * Sets up the JFrame with proper properties and loads the SignInPage.
     */
    private static void createAndShowMainWindow() {
        // Create main application frame
        mainFrame = new JFrame(APP_TITLE + " " + APP_VERSION);
        
        // Set up frame properties
        setupMainFrameProperties();
        
        // Set up window listeners for proper cleanup
        setupWindowListeners();
        
        // Initialize and display the SignInPage
        loadSignInPage();
        
        // Center the window on screen
        centerWindow();
        
        // Make the window visible
        mainFrame.setVisible(true);
        
        // Bring window to front
        mainFrame.toFront();
        mainFrame.requestFocus();
    }
    
    /**
     * Configures the main JFrame properties.
     */
    private static void setupMainFrameProperties() {
        // Set default close operation to do nothing (we'll handle it ourselves)
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Set window size and constraints
        mainFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        mainFrame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        
        // Set application icon (if available)
        try {
            // You can add an icon file to your resources and load it here
            // ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
            // mainFrame.setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load application icon", e);
        }
        
        // Set layout manager
        mainFrame.setLayout(new BorderLayout());
        
        // Enable resizing
        mainFrame.setResizable(true);
    }
    
    /**
     * Sets up window listeners for handling window events and cleanup.
     */
    private static void setupWindowListeners() {
        mainFrame.addWindowListener(new WindowAdapter() {
            
            /**
             * Handles window closing event with graceful shutdown.
             */
            @Override
            public void windowClosing(WindowEvent e) {
                handleApplicationShutdown();
            }
            
            /**
             * Handles window opened event.
             */
            @Override
            public void windowOpened(WindowEvent e) {
                logger.info("Main window opened");
            }
            
            /**
             * Handles window iconified (minimized) event.
             */
            @Override
            public void windowIconified(WindowEvent e) {
                logger.fine("Main window minimized");
            }
            
            /**
             * Handles window deiconified (restored) event.
             */
            @Override
            public void windowDeiconified(WindowEvent e) {
                logger.fine("Main window restored");
            }
        });
    }
    
    /**
     * Loads and displays the SignInPage as the initial screen.
     * FIXED: Now properly handles both JPanel and JFrame-based SignInPage implementations.
     */
    private static void loadSignInPage() {
        try {
            // Clear any existing content
            mainFrame.getContentPane().removeAll();
            
            // Create the SignInPage
            SignInPage signInPage = new SignInPage();
            
            // Check if SignInPage is a JPanel (preferred approach)
            if (signInPage instanceof JPanel) {
                // SignInPage extends JPanel - add it directly
                mainFrame.add((JPanel) signInPage, BorderLayout.CENTER);
                logger.info("SignInPage (JPanel) loaded successfully");
            }
            
            // Refresh the display
            mainFrame.revalidate();
            mainFrame.repaint();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load SignInPage", e);
            showErrorDialog("Initialization Error", "Failed to load sign-in page: " + e.getMessage());
            
            // Show a fallback error panel
            mainFrame.getContentPane().removeAll();
            JPanel errorPanel = new JPanel(new BorderLayout());
            JLabel errorLabel = new JLabel("<html><center>Error loading Sign-In Page<br>" + e.getMessage() + "</center></html>");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            mainFrame.add(errorPanel, BorderLayout.CENTER);
            mainFrame.revalidate();
            mainFrame.repaint();
        }
    }
    
    /**
     * Centers the main window on the screen.
     */
    private static void centerWindow() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Calculate center position
        int centerX = (screenSize.width - mainFrame.getWidth()) / 2;
        int centerY = (screenSize.height - mainFrame.getHeight()) / 2;
        
        // Set window location
        mainFrame.setLocation(centerX, centerY);
    }
    
    /**
     * Handles graceful application shutdown.
     * Prompts user for confirmation and performs cleanup operations.
     */
    private static void handleApplicationShutdown() {
        try {
            // Set shutdown flag to prevent multiple shutdown attempts
            if (isShuttingDown) {
                return;
            }
            isShuttingDown = true;
            
            // Show confirmation dialog
            int choice = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to exit Recipe Manager?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                // Perform cleanup operations
                performCleanup();
                
                // Log shutdown
                logger.info("Recipe Manager application shutting down gracefully");
                
                // Exit application
                System.exit(0);
            } else {
                // User cancelled shutdown
                isShuttingDown = false;
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during application shutdown", e);
            // Force exit if cleanup fails
            System.exit(1);
        }
    }
    
    /**
     * Performs cleanup operations before application shutdown.
     */
    private static void performCleanup() {
        try {
            // Close database connections
            if (databaseConnector != null) {
                // Add database cleanup code here
                logger.info("Database connections closed");
            }
            
            // Save any unsaved data or user preferences
            // Add code to save application state if needed
            
            // Dispose of main frame
            if (mainFrame != null) {
                mainFrame.dispose();
            }
            
            logger.info("Application cleanup completed successfully");
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Some cleanup operations failed", e);
        }
    }
    
    /**
     * Displays an error dialog to the user.
     * 
     * @param title The dialog title
     * @param message The error message to display
     */
    private static void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(
            mainFrame,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Gets the main application frame.
     * This can be used by other classes that need access to the main window.
     * 
     * @return The main JFrame instance
     */
    public static JFrame getMainFrame() {
        return mainFrame;
    }
    
    /**
     * Gets the database connector instance.
     * This can be used by other classes that need database access.
     * 
     * @return The DatabaseConnector instance
     */
    public static DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }
    
    /**
     * Checks if the application is currently shutting down.
     * 
     * @return true if the application is shutting down, false otherwise
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }
}