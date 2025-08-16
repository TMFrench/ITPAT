package backend;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TimerManager handles cooking timer operations for the Recipe Manager application.
 * This class supports multiple concurrent timers with individual state management,
 * callback mechanisms for UI updates, and comprehensive error handling.
 * 
 * Features:
 * - Multiple concurrent timers
 * - Timer state management (Running, Paused, Stopped, Completed)
 * - Callback mechanism for real-time updates
 * - Pause/Resume functionality
 * - Thread-safe operations
 */
public class TimerManager {
    
    private static final Logger logger = Logger.getLogger(TimerManager.class.getName());
    
    // Timer ID generator for unique timer identification
    private static final AtomicInteger timerIdGenerator = new AtomicInteger(1);
    
    // Thread-safe storage for multiple timers
    private final Map<Integer, CookingTimer> activeTimers = new ConcurrentHashMap<>();
    
    // Default timer ID for single timer operations (backward compatibility)
    private Integer defaultTimerId = null;
    
    /**
     * Enum representing the various states a timer can be in.
     */
    public enum TimerState {
        /** Timer is currently running and counting down */
        RUNNING,
        /** Timer is paused and can be resumed */
        PAUSED,
        /** Timer has been manually stopped */
        STOPPED,
        /** Timer has completed its countdown */
        COMPLETED,
        /** Timer encountered an error */
        ERROR
    }
    
    /**
     * Callback interface for timer events and updates.
     * Implementations should handle these events to update UI or perform actions.
     */
    public interface TimerCallback {
        /**
         * Called every second while the timer is running.
         * 
         * @param timerId Unique identifier for the timer
         * @param remainingSeconds Number of seconds remaining
         * @param totalSeconds Total duration of the timer
         */
        void onTick(int timerId, int remainingSeconds, int totalSeconds);
        
        /**
         * Called when the timer completes its countdown.
         * 
         * @param timerId Unique identifier for the timer
         * @param name Name or description of the timer
         */
        void onTimerCompleted(int timerId, String name);
        
        /**
         * Called when the timer state changes (paused, resumed, stopped).
         * 
         * @param timerId Unique identifier for the timer
         * @param oldState Previous timer state
         * @param newState New timer state
         */
        void onStateChanged(int timerId, TimerState oldState, TimerState newState);
        
        /**
         * Called when an error occurs with the timer.
         * 
         * @param timerId Unique identifier for the timer
         * @param error Error message describing what went wrong
         */
        void onTimerError(int timerId, String error);
    }
    
    /**
     * Internal class representing a single cooking timer with its own state and management.
     */
    private class CookingTimer {
        private final int timerId;
        private final String name;
        private final int totalSeconds;
        private int remainingSeconds;
        private TimerState state;
        private Timer javaTimer;
        private TimerTask currentTask;
        private final TimerCallback callback;
        private final Object stateLock = new Object();
        
        /**
         * Creates a new cooking timer.
         * 
         * @param timerId Unique identifier for this timer
         * @param name Name or description for this timer
         * @param totalSeconds Total duration in seconds
         * @param callback Callback for timer events
         */
        public CookingTimer(int timerId, String name, int totalSeconds, TimerCallback callback) {
            this.timerId = timerId;
            this.name = name != null ? name : "Timer " + timerId;
            this.totalSeconds = totalSeconds;
            this.remainingSeconds = totalSeconds;
            this.state = TimerState.STOPPED;
            this.callback = callback;
        }
        
        /**
         * Starts or resumes the timer.
         */
        public void start() {
            synchronized (stateLock) {
                if (state == TimerState.RUNNING) {
                    logger.warning("Timer " + timerId + " is already running");
                    return;
                }
                
                if (state == TimerState.COMPLETED) {
                    logger.warning("Timer " + timerId + " has already completed");
                    return;
                }
                
                TimerState oldState = state;
                state = TimerState.RUNNING;
                
                // Create new Java Timer and TimerTask
                javaTimer = new Timer("CookingTimer-" + timerId, true);
                currentTask = new TimerTask() {
                    @Override
                    public void run() {
                        tick();
                    }
                };
                
                // Schedule the timer to run every second
                javaTimer.scheduleAtFixedRate(currentTask, 0, 1000);
                
                if (callback != null) {
                    callback.onStateChanged(timerId, oldState, TimerState.RUNNING);
                }
                
                logger.info("Timer " + timerId + " started with " + remainingSeconds + " seconds");
            }
        }
        
        /**
         * Pauses the timer, preserving remaining time.
         */
        public void pause() {
            synchronized (stateLock) {
                if (state != TimerState.RUNNING) {
                    logger.warning("Timer " + timerId + " is not running, cannot pause");
                    return;
                }
                
                TimerState oldState = state;
                state = TimerState.PAUSED;
                
                // Cancel current timer
                if (currentTask != null) {
                    currentTask.cancel();
                }
                if (javaTimer != null) {
                    javaTimer.cancel();
                }
                
                if (callback != null) {
                    callback.onStateChanged(timerId, oldState, TimerState.PAUSED);
                }
                
                logger.info("Timer " + timerId + " paused with " + remainingSeconds + " seconds remaining");
            }
        }
        
        /**
         * Stops the timer completely.
         */
        public void stop() {
            synchronized (stateLock) {
                if (state == TimerState.STOPPED || state == TimerState.COMPLETED) {
                    return;
                }
                
                TimerState oldState = state;
                state = TimerState.STOPPED;
                
                // Cancel current timer
                if (currentTask != null) {
                    currentTask.cancel();
                }
                if (javaTimer != null) {
                    javaTimer.cancel();
                }
                
                // Reset remaining time to original value
                remainingSeconds = totalSeconds;
                
                if (callback != null) {
                    callback.onStateChanged(timerId, oldState, TimerState.STOPPED);
                }
                
                logger.info("Timer " + timerId + " stopped");
            }
        }
        
        /**
         * Handles each timer tick (called every second).
         */
        private void tick() {
            synchronized (stateLock) {
                if (state != TimerState.RUNNING) {
                    return;
                }
                
                try {
                    // Update UI with current time
                    if (callback != null) {
                        callback.onTick(timerId, remainingSeconds, totalSeconds);
                    }
                    
                    // Check if timer completed
                    if (remainingSeconds <= 0) {
                        completeTimer();
                        return;
                    }
                    
                    // Decrement remaining time
                    remainingSeconds--;
                    
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error in timer tick for timer " + timerId, e);
                    handleError("Timer tick error: " + e.getMessage());
                }
            }
        }
        
        /**
         * Handles timer completion.
         */
        private void completeTimer() {
            synchronized (stateLock) {
                state = TimerState.COMPLETED;
                
                // Cancel timer
                if (currentTask != null) {
                    currentTask.cancel();
                }
                if (javaTimer != null) {
                    javaTimer.cancel();
                }
                
                if (callback != null) {
                    callback.onTimerCompleted(timerId, name);
                }
                
                logger.info("Timer " + timerId + " completed: " + name);
            }
        }
        
        /**
         * Handles timer errors.
         */
        private void handleError(String errorMessage) {
            synchronized (stateLock) {
                TimerState oldState = state;
                state = TimerState.ERROR;
                
                // Cancel timer
                if (currentTask != null) {
                    currentTask.cancel();
                }
                if (javaTimer != null) {
                    javaTimer.cancel();
                }
                
                if (callback != null) {
                    callback.onTimerError(timerId, errorMessage);
                    callback.onStateChanged(timerId, oldState, TimerState.ERROR);
                }
                
                logger.severe("Timer " + timerId + " error: " + errorMessage);
            }
        }
        
        // Getters
        public int getTimerId() { return timerId; }
        public String getName() { return name; }
        public int getTotalSeconds() { return totalSeconds; }
        public int getRemainingSeconds() { return remainingSeconds; }
        public TimerState getState() { return state; }
        public boolean isRunning() { return state == TimerState.RUNNING; }
        public boolean isPaused() { return state == TimerState.PAUSED; }
        public boolean isCompleted() { return state == TimerState.COMPLETED; }
        public boolean isStopped() { return state == TimerState.STOPPED; }
    }
    
    /**
     * Constructor initializes the TimerManager.
     */
    public TimerManager() {
        logger.info("TimerManager initialized");
    }
    
    /**
     * Starts a new timer with the specified duration in seconds.
     * This method is for backward compatibility and creates a default timer.
     * 
     * @param seconds Duration of the timer in seconds
     * @return true if timer started successfully, false otherwise
     * @throws IllegalArgumentException if seconds is less than or equal to 0
     * 
     * @example
     * <pre>
     * TimerManager timerManager = new TimerManager();
     * boolean started = timerManager.startTimer(300); // 5 minutes
     * </pre>
     */
    public boolean startTimer(int seconds) {
        return startTimer(seconds, null, null) > 0;
    }
    
    /**
     * Starts a new timer with specified duration, name, and callback.
     * 
     * @param seconds Duration of the timer in seconds
     * @param name Name or description for the timer (can be null)
     * @param callback Callback for timer events (can be null)
     * @return Timer ID if started successfully, -1 if failed
     * @throws IllegalArgumentException if seconds is less than or equal to 0
     * 
     * @example
     * <pre>
     * int timerId = timerManager.startTimer(600, "Pasta Cooking", new TimerCallback() {
     *     public void onTick(int id, int remaining, int total) {
     *         System.out.println("Time remaining: " + remaining + " seconds");
     *     }
     *     public void onTimerCompleted(int id, String name) {
     *         System.out.println("Timer completed: " + name);
     *     }
     *     // ... implement other callback methods
     * });
     * </pre>
     */
    public int startTimer(int seconds, String name, TimerCallback callback) {
        validateSeconds(seconds);
        
        try {
            int timerId = timerIdGenerator.getAndIncrement();
            CookingTimer timer = new CookingTimer(timerId, name, seconds, callback);
            
            activeTimers.put(timerId, timer);
            
            // Set as default timer if this is the first one
            if (defaultTimerId == null) {
                defaultTimerId = timerId;
            }
            
            timer.start();
            
            logger.info("Started new timer with ID " + timerId + " for " + seconds + " seconds");
            return timerId;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start timer", e);
            return -1;
        }
    }
    
    /**
     * Stops the currently running default timer.
     * This method is for backward compatibility with single timer operations.
     * 
     * @return true if timer stopped successfully, false otherwise
     * 
     * @example
     * <pre>
     * boolean stopped = timerManager.stopTimer();
     * </pre>
     */
    public boolean stopTimer() {
        if (defaultTimerId == null) {
            logger.warning("No default timer to stop");
            return false;
        }
        return stopTimer(defaultTimerId);
    }
    
    /**
     * Stops a specific timer by its ID.
     * 
     * @param timerId The ID of the timer to stop
     * @return true if timer stopped successfully, false otherwise
     * @throws IllegalArgumentException if timerId is invalid
     * 
     * @example
     * <pre>
     * boolean stopped = timerManager.stopTimer(timerId);
     * </pre>
     */
    public boolean stopTimer(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            logger.warning("Timer with ID " + timerId + " not found");
            return false;
        }
        
        try {
            timer.stop();
            activeTimers.remove(timerId);
            
            // Clear default timer ID if this was the default
            if (Objects.equals(defaultTimerId, timerId)) {
                defaultTimerId = findNextDefaultTimer();
            }
            
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to stop timer " + timerId, e);
            return false;
        }
    }
    
    /**
     * Pauses the currently running default timer, preserving the remaining time.
     * This method is for backward compatibility with single timer operations.
     * 
     * @return true if timer paused successfully, false otherwise
     * 
     * @example
     * <pre>
     * boolean paused = timerManager.pauseTimer();
     * </pre>
     */
    public boolean pauseTimer() {
        if (defaultTimerId == null) {
            logger.warning("No default timer to pause");
            return false;
        }
        return pauseTimer(defaultTimerId);
    }
    
    /**
     * Pauses a specific timer by its ID, preserving the remaining time.
     * 
     * @param timerId The ID of the timer to pause
     * @return true if timer paused successfully, false otherwise
     * @throws IllegalArgumentException if timerId is invalid
     * 
     * @example
     * <pre>
     * boolean paused = timerManager.pauseTimer(timerId);
     * </pre>
     */
    public boolean pauseTimer(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            logger.warning("Timer with ID " + timerId + " not found");
            return false;
        }
        
        try {
            timer.pause();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to pause timer " + timerId, e);
            return false;
        }
    }
    
    /**
     * Resumes a paused default timer, continuing from the remaining time.
     * This method is for backward compatibility with single timer operations.
     * 
     * @return true if timer resumed successfully, false otherwise
     * 
     * @example
     * <pre>
     * boolean resumed = timerManager.resumeTimer();
     * </pre>
     */
    public boolean resumeTimer() {
        if (defaultTimerId == null) {
            logger.warning("No default timer to resume");
            return false;
        }
        return resumeTimer(defaultTimerId);
    }
    
    /**
     * Resumes a paused timer by its ID, continuing from the remaining time.
     * 
     * @param timerId The ID of the timer to resume
     * @return true if timer resumed successfully, false otherwise
     * @throws IllegalArgumentException if timerId is invalid
     * 
     * @example
     * <pre>
     * boolean resumed = timerManager.resumeTimer(timerId);
     * </pre>
     */
    public boolean resumeTimer(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            logger.warning("Timer with ID " + timerId + " not found");
            return false;
        }
        
        if (!timer.isPaused()) {
            logger.warning("Timer " + timerId + " is not paused, cannot resume");
            return false;
        }
        
        try {
            timer.start(); // start() method handles resuming paused timers
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to resume timer " + timerId, e);
            return false;
        }
    }
    
    /**
     * Returns the remaining time in seconds for the default timer.
     * This method is for backward compatibility with single timer operations.
     * 
     * @return Remaining time in seconds, -1 if no default timer exists
     * 
     * @example
     * <pre>
     * int remaining = timerManager.getRemainingTime();
     * if (remaining > 0) {
     *     System.out.println("Time remaining: " + remaining + " seconds");
     * }
     * </pre>
     */
    public int getRemainingTime() {
        if (defaultTimerId == null) {
            return -1;
        }
        return getRemainingTime(defaultTimerId);
    }
    
    /**
     * Returns the remaining time in seconds for a specific timer.
     * 
     * @param timerId The ID of the timer to query
     * @return Remaining time in seconds, -1 if timer not found
     * @throws IllegalArgumentException if timerId is invalid
     * 
     * @example
     * <pre>
     * int remaining = timerManager.getRemainingTime(timerId);
     * </pre>
     */
    public int getRemainingTime(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            return -1;
        }
        return timer.getRemainingSeconds();
    }
    
    /**
     * Checks if the default timer is currently running.
     * This method is for backward compatibility with single timer operations.
     * 
     * @return true if default timer is running, false otherwise
     * 
     * @example
     * <pre>
     * if (timerManager.isRunning()) {
     *     System.out.println("Timer is currently running");
     * }
     * </pre>
     */
    public boolean isRunning() {
        if (defaultTimerId == null) {
            return false;
        }
        return isRunning(defaultTimerId);
    }
    
    /**
     * Checks if a specific timer is currently running.
     * 
     * @param timerId The ID of the timer to check
     * @return true if timer is running, false otherwise
     * @throws IllegalArgumentException if timerId is invalid
     * 
     * @example
     * <pre>
     * if (timerManager.isRunning(timerId)) {
     *     System.out.println("Timer " + timerId + " is running");
     * }
     * </pre>
     */
    public boolean isRunning(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            return false;
        }
        return timer.isRunning();
    }
    
    /**
     * Gets the current state of a specific timer.
     * 
     * @param timerId The ID of the timer to query
     * @return Current timer state, null if timer not found
     * 
     * @example
     * <pre>
     * TimerState state = timerManager.getTimerState(timerId);
     * if (state == TimerState.RUNNING) {
     *     System.out.println("Timer is running");
     * }
     * </pre>
     */
    public TimerState getTimerState(int timerId) {
        CookingTimer timer = activeTimers.get(timerId);
        if (timer == null) {
            return null;
        }
        return timer.getState();
    }
    
    /**
     * Gets information about all active timers.
     * 
     * @return List of TimerInfo objects for all active timers
     * 
     * @example
     * <pre>
     * List&lt;TimerInfo&gt; timers = timerManager.getAllTimers();
     * for (TimerInfo info : timers) {
     *     System.out.println("Timer " + info.getId() + ": " + info.getName());
     * }
     * </pre>
     */
    public List<TimerInfo> getAllTimers() {
        List<TimerInfo> timerInfos = new ArrayList<>();
        
        for (CookingTimer timer : activeTimers.values()) {
            timerInfos.add(new TimerInfo(
                timer.getTimerId(),
                timer.getName(),
                timer.getTotalSeconds(),
                timer.getRemainingSeconds(),
                timer.getState()
            ));
        }
        
        return timerInfos;
    }
    
    /**
     * Gets the count of active timers.
     * 
     * @return Number of active timers
     */
    public int getActiveTimerCount() {
        return activeTimers.size();
    }
    
    /**
     * Stops all active timers.
     * 
     * @return Number of timers that were stopped
     * 
     * @example
     * <pre>
     * int stopped = timerManager.stopAllTimers();
     * System.out.println("Stopped " + stopped + " timers");
     * </pre>
     */
    public int stopAllTimers() {
        int stoppedCount = 0;
        List<Integer> timerIds = new ArrayList<>(activeTimers.keySet());
        
        for (Integer timerId : timerIds) {
            if (stopTimer(timerId)) {
                stoppedCount++;
            }
        }
        
        defaultTimerId = null;
        logger.info("Stopped " + stoppedCount + " timers");
        return stoppedCount;
    }
    
    /**
     * Information class for timer details.
     */
    public static class TimerInfo {
        private final int id;
        private final String name;
        private final int totalSeconds;
        private final int remainingSeconds;
        private final TimerState state;
        
        public TimerInfo(int id, String name, int totalSeconds, int remainingSeconds, TimerState state) {
            this.id = id;
            this.name = name;
            this.totalSeconds = totalSeconds;
            this.remainingSeconds = remainingSeconds;
            this.state = state;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public int getTotalSeconds() { return totalSeconds; }
        public int getRemainingSeconds() { return remainingSeconds; }
        public TimerState getState() { return state; }
        
        @Override
        public String toString() {
            return String.format("TimerInfo{id=%d, name='%s', remaining=%d/%d, state=%s}", 
                id, name, remainingSeconds, totalSeconds, state);
        }
    }
    
    /**
     * Utility method to format seconds into MM:SS or HH:MM:SS format.
     * 
     * @param seconds Number of seconds to format
     * @return Formatted time string
     * 
     * @example
     * <pre>
     * String formatted = TimerManager.formatTime(125); // Returns "02:05"
     * String formatted = TimerManager.formatTime(3665); // Returns "01:01:05"
     * </pre>
     */
    public static String formatTime(int seconds) {
        if (seconds < 0) {
            return "00:00";
        }
        
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    /**
     * Finds the next available timer to set as default after the current default is removed.
     */
    private Integer findNextDefaultTimer() {
        return activeTimers.keySet().stream().findFirst().orElse(null);
    }
    
    /**
     * Validates that the seconds parameter is positive.
     */
    private void validateSeconds(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Timer duration must be greater than 0 seconds");
        }
        if (seconds > 86400) { // More than 24 hours
            logger.warning("Timer duration is very long: " + seconds + " seconds");
        }
    }
}