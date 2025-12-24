package com.aiu.scrs.controller;

import com.aiu.scrs.dto.ApiResponse;
import com.aiu.scrs.dto.user.UserResponse;
import com.aiu.scrs.entity.User;
import com.aiu.scrs.service.ComplaintService;
import com.aiu.scrs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getAllUsers(pageable, role, status, search);

            Map<String, Object> response = new HashMap<>();
            response.put("content", users.getContent().stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList()));
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("number", users.getNumber());
            response.put("size", users.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(convertToUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            User updatedUser = userService.updateUser(id, updates);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> status) {
        try {
            userService.toggleUserStatus(id, status.get("enabled"));
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update user status: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // User stats
            stats.put("totalUsers", userService.getTotalUsers());
            stats.put("activeUsers", userService.getActiveUsersCount());

            // Complaint stats
            Map<String, Object> complaintStats = complaintService.getComplaintStats();
            stats.put("totalComplaints", complaintStats.get("total"));
            stats.put("pendingComplaints", complaintStats.get("pending"));
            stats.put("inProgressComplaints", complaintStats.get("inProgress"));
            stats.put("resolvedComplaints", complaintStats.get("resolved"));
            stats.put("rejectedComplaints", complaintStats.get("rejected"));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve stats: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/avg-resolution-time")
    public ResponseEntity<?> getAverageResolutionTime() {
        try {
            Double avgTime = complaintService.getAverageResolutionTime();
            Map<String, Object> response = new HashMap<>();
            response.put("averageDays", avgTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to calculate average resolution time: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/most-active-department")
    public ResponseEntity<?> getMostActiveDepartment() {
        try {
            Map<String, Object> result = complaintService.getMostActiveDepartment();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get most active department: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/satisfaction-rate")
    public ResponseEntity<?> getSatisfactionRate() {
        try {
            Double rate = complaintService.getSatisfactionRate();
            Map<String, Object> response = new HashMap<>();
            response.put("rate", rate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to calculate satisfaction rate: " + e.getMessage()));
        }
    }

    @GetMapping("/activity/recent")
    public ResponseEntity<?> getRecentActivity() {
        try {
            List<Map<String, Object>> activities = userService.getRecentActivity();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve recent activity: " + e.getMessage()));
        }
    }

    @GetMapping("/users/stats/trends")
    public ResponseEntity<?> getUserRegistrationTrends() {
        try {
            Map<String, Object> trends = userService.getUserRegistrationTrends();
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve user trends: " + e.getMessage()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        try {
            Map<String, Object> settings = new HashMap<>();
            // TODO: Implement settings retrieval from database
            settings.put("systemName", "Student Complaint Resolution System");
            settings.put("defaultRole", "STUDENT");
            settings.put("emailNotifications", true);
            settings.put("autoAssignment", true);
            settings.put("sessionTimeout", 30);
            settings.put("requirePasswordChange", false);

            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve settings: " + e.getMessage()));
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settings) {
        try {
            // TODO: Implement settings update in database
            return ResponseEntity.ok(ApiResponse.success("Settings updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update settings: " + e.getMessage()));
        }
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt().toString());

        if (user.getDepartment() != null) {
            response.setDepartment(user.getDepartment().getName());
        }

        return response;
    }
}
