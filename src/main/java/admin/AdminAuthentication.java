package admin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminAuthentication handles simple authentication for admin users
 * In production, this should be replaced with more secure authentication
 */
public class AdminAuthentication {
    private static final Map<String, String> adminUsers = new HashMap<>();
    
    // Initialize with default admin credentials
    static {
        // Default admin username: "admin", password: "admin123"
        // In production, use environment variables or a database
        adminUsers.put("admin", hashPassword("admin123"));
        adminUsers.put("superadmin", hashPassword("super123"));
    }

    /**
     * Hash password using SHA-256
     * In production, use bcrypt or similar
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Authenticate admin user
     * @param username Admin username
     * @param password Admin password
     * @return true if authentication successful, false otherwise
     */
    public static boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        String hashedPassword = hashPassword(password);
        String storedHash = adminUsers.get(username);
        
        if (storedHash != null && storedHash.equals(hashedPassword)) {
            System.out.println("Admin authenticated: " + username);
            return true;
        }
        
        System.out.println("Authentication failed for: " + username);
        return false;
    }

    /**
     * Add a new admin user
     * @param username New admin username
     * @param password New admin password
     * @return true if user added successfully
     */
    public static boolean addAdminUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }
        
        if (adminUsers.containsKey(username)) {
            System.out.println("Admin user already exists: " + username);
            return false;
        }
        
        adminUsers.put(username, hashPassword(password));
        System.out.println("Added new admin user: " + username);
        return true;
    }

    /**
     * Remove an admin user
     * @param username Admin username to remove
     * @return true if user removed successfully
     */
    public static boolean removeAdminUser(String username) {
        if (adminUsers.remove(username) != null) {
            System.out.println("Removed admin user: " + username);
            return true;
        }
        return false;
    }

    /**
     * Check if a user is an admin
     * @param username Username to check
     * @return true if user exists in admin list
     */
    public static boolean isAdmin(String username) {
        return adminUsers.containsKey(username);
    }

    /**
     * Get number of admin users
     * @return count of admin users
     */
    public static int getAdminCount() {
        return adminUsers.size();
    }
}
