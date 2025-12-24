package com.aiu.scrs.service;

import com.aiu.scrs.config.JwtTokenProvider;
import com.aiu.scrs.dto.auth.AuthResponse;
import com.aiu.scrs.dto.auth.LoginRequest;
import com.aiu.scrs.dto.auth.RegisterRequest;
import com.aiu.scrs.dto.user.UserResponse;
import com.aiu.scrs.entity.User;
import com.aiu.scrs.entity.UserRole;
import com.aiu.scrs.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * User Service - Business logic for user management
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    /**
     * Create a new user
     */
    public User createUser(User user) {
        logger.info("Creating user with username: {}", user.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Update an existing user
     */
    public User updateUser(User user) {
        logger.info("Updating user with ID: {}", user.getId());
        
        Optional<User> existingUserOpt = userRepository.findById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + user.getId());
        }
        
        User existingUser = existingUserOpt.get();
        
        // Check username uniqueness (excluding current user)
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Check email uniqueness (excluding current user)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        User savedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Get user by ID and throw exception if not found
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Get active users by role
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsActive(role, true);
    }
    
    /**
     * Get users by department
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId);
    }
    
    /**
     * Get students
     */
    @Transactional(readOnly = true)
    public List<User> getStudents() {
        return userRepository.findStudents(UserRole.STUDENT);
    }
    
    /**
     * Get staff members
     */
    @Transactional(readOnly = true)
    public List<User> getStaff() {
        return userRepository.findStaff(UserRole.STAFF);
    }
    
    /**
     * Get administrators
     */
    @Transactional(readOnly = true)
    public List<User> getAdmins() {
        return userRepository.findAdmins(UserRole.ADMIN);
    }
    
    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user with ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            userRepository.save(user);
            logger.info("User deactivated successfully with ID: {}", userId);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
    
    /**
     * Activate user
     */
    public void activateUser(Long userId) {
        logger.info("Activating user with ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            userRepository.save(user);
            logger.info("User activated successfully with ID: {}", userId);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
    
    /**
     * Delete user
     */
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
        logger.info("User deleted successfully with ID: {}", userId);
    }
    
    /**
     * Change user password
     */
    public void changePassword(Long userId, String newPassword) {
        logger.info("Changing password for user with ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password changed successfully for user with ID: {}", userId);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
    
    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Register a new user from RegisterRequest
     */
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        logger.info("Registering new user with username: {}", registerRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists: " + registerRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists: " + registerRequest.getEmail());
        }
        
        // Create user entity from request
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFullName().split(" ")[0]);
        user.setLastName(registerRequest.getFullName().split(" ").length > 1 ? registerRequest.getFullName().split(" ")[1] : "");
        user.setPassword(registerRequest.getPassword());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.STUDENT);
        user.setIsActive(true);
        
        // Save user (password will be encoded in createUser)
        User savedUser = createUser(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Generate JWT token
        String jwt = tokenProvider.generateToken(savedUser.getUsername());
        
        return new AuthResponse(jwt, savedUser.getId(), savedUser.getUsername(), 
            savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole(), savedUser.getIsActive());
    }

    /**
     * Authenticate user from LoginRequest
     */
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Find user by username or email
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                    .or(() -> userRepository.findByEmail(loginRequest.getUsernameOrEmail()));
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                throw new RuntimeException("User account is deactivated");
            }
            
            // Generate JWT token
            String jwt = tokenProvider.generateToken(user.getUsername());
            
            logger.info("User authenticated successfully: {}", user.getUsername());
            
            return new AuthResponse(jwt, user.getId(), user.getUsername(), 
                user.getEmail(), user.getFullName(), user.getRole(), user.getIsActive());
            
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsernameOrEmail(), e);
            throw new RuntimeException("Invalid username or password");
        }
    }

    /**
     * Find user by username (for AuthController)
     */
    public User findByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return userOpt.get();
    }

    /**
     * Get all users with pagination and filtering (for AdminController)
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable, String role, String status, String search) {
        // TODO: Implement proper filtering in UserRepository
        // For now, return all users as a page
        return userRepository.findAll(pageable);
    }

    /**
     * Update user with map of updates (for AdminController)
     */
    public User updateUser(Long id, Map<String, Object> updates) {
        User user = getUserById(id);

        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            if (!user.getUsername().equals(newUsername) && userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("Username already exists: " + newUsername);
            }
            user.setUsername(newUsername);
        }

        if (updates.containsKey("fullName")) {
            String fullName = (String) updates.get("fullName");
            String[] parts = fullName.split(" ", 2);
            user.setFirstName(parts[0]);
            if (parts.length > 1) {
                user.setLastName(parts[1]);
            }
        }

        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists: " + newEmail);
            }
            user.setEmail(newEmail);
        }

        if (updates.containsKey("role")) {
            user.setRole(UserRole.valueOf((String) updates.get("role")));
        }

        if (updates.containsKey("departmentId")) {
            // TODO: Set department if needed
        }

        return userRepository.save(user);
    }

    /**
     * Toggle user status (for AdminController)
     */
    public void toggleUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setIsActive(enabled);
        userRepository.save(user);
    }

    /**
     * Get total users count (for AdminController)
     */
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    /**
     * Get active users count (for AdminController)
     */
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.findByIsActive(true).size();
    }

    /**
     * Get recent activity (for AdminController)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentActivity() {
        List<Map<String, Object>> activities = new ArrayList<>();

        // Mock recent activities - in real implementation, this would come from an activity log table
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("type", "USER_REGISTERED");
        activity1.put("description", "New user registered");
        activity1.put("timestamp", LocalDateTime.now().minusHours(2));
        activities.add(activity1);

        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("type", "COMPLAINT_CREATED");
        activity2.put("description", "New complaint submitted");
        activity2.put("timestamp", LocalDateTime.now().minusHours(4));
        activities.add(activity2);

        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("type", "COMPLAINT_RESOLVED");
        activity3.put("description", "Complaint resolved");
        activity3.put("timestamp", LocalDateTime.now().minusHours(6));
        activities.add(activity3);

        return activities;
    }

    /**
     * Get user registration trends (for AdminController)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserRegistrationTrends() {
        Map<String, Object> trends = new HashMap<>();

        // Mock data - in real implementation, this would aggregate from database
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            labels.add(date.format(DateTimeFormatter.ofPattern("MMM dd")));
            data.add((long) (Math.random() * 10 + 1)); // Mock data
        }

        trends.put("labels", labels);
        trends.put("data", data);

        return trends;
    }
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setEnabled(user.getIsActive());
        if (user.getDepartment() != null) {
            response.setDepartment(user.getDepartment().getName());
        }
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        return response;
    }
}
