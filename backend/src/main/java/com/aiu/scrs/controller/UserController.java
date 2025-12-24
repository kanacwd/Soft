package com.aiu.scrs.controller;

import com.aiu.scrs.dto.ApiResponse;
import com.aiu.scrs.dto.user.UserResponse;
import com.aiu.scrs.entity.User;
import com.aiu.scrs.entity.UserRole;
import com.aiu.scrs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Controller - Handles user management operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", convertToUserResponse(user)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        try {
            List<User> users = userService.getUsersByRole(role);
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get students
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStudents() {
        try {
            List<User> users = userService.getStudents();
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve students: " + e.getMessage()));
        }
    }

    /**
     * Get staff members
     */
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getStaff() {
        try {
            List<User> users = userService.getStaff();
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve staff: " + e.getMessage()));
        }
    }

    /**
     * Get administrators
     */
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAdmins() {
        try {
            List<User> users = userService.getAdmins();
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve admins: " + e.getMessage()));
        }
    }

    /**
     * Get users by department
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByDepartment(@PathVariable Long departmentId) {
        try {
            List<User> users = userService.getUsersByDepartment(departmentId);
            List<UserResponse> userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Deactivate user
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to deactivate user: " + e.getMessage()));
        }
    }

    /**
     * Activate user
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to activate user: " + e.getMessage()));
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Change user password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Long id, 
                                                            @RequestParam String newPassword) {
        try {
            userService.changePassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        
        // Combine first and last name into fullName
        String fullName = "";
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            fullName += user.getFirstName();
        }
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            if (!fullName.isEmpty()) {
                fullName += " ";
            }
            fullName += user.getLastName();
        }
        response.setFullName(fullName.isEmpty() ? user.getUsername() : fullName);
        
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
