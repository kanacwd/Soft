package com.aiu.scrs.service;

import com.aiu.scrs.entity.*;
import com.aiu.scrs.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Complaint Service - Business logic for complaint management
 */
@Service
@Transactional
public class ComplaintService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    
    private final ComplaintRepository complaintRepository;
    private final ComplaintVoteRepository complaintVoteRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final ComplaintCommentRepository commentRepository;
    private final UserService userService;
    private final DepartmentService departmentService;
    
    @Autowired
    public ComplaintService(ComplaintRepository complaintRepository,
                          ComplaintVoteRepository complaintVoteRepository,
                          ComplaintStatusHistoryRepository statusHistoryRepository,
                          ComplaintCommentRepository commentRepository,
                          UserService userService,
                          DepartmentService departmentService) {
        this.complaintRepository = complaintRepository;
        this.complaintVoteRepository = complaintVoteRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.departmentService = departmentService;
    }
    
    /**
     * Create a new complaint
     */
    public Complaint createComplaint(Complaint complaint, Long createdByUserId) {
        logger.info("Creating complaint with title: {}", complaint.getTitle());
        
        // Validate creator
        User creator;
        try {
            creator = userService.getUserById(createdByUserId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Creator user not found with ID: " + createdByUserId);
        }
        
        if (!creator.getIsActive()) {
            throw new RuntimeException("Creator user is not active");
        }
        
        // Set initial values
        complaint.setCreatedBy(creator);
        complaint.setStatus(ComplaintStatus.NEW);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setStudentConfirmation(false);
        complaint.setTotalVotes(0);
        
        // Assign to appropriate department
        if (complaint.getTargetDepartment() == null) {
            // Auto-assign based on complaint type
            Optional<Department> departmentOpt = departmentService.getDepartmentByComplaintType(complaint.getType());
            if (departmentOpt.isPresent()) {
                complaint.setTargetDepartment(departmentOpt.get());
            }
        }
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        
        // Create initial status history
        createStatusHistory(savedComplaint, null, ComplaintStatus.NEW, creator, "Initial complaint submission");
        
        logger.info("Complaint created successfully with ID: {}", savedComplaint.getId());
        
        return savedComplaint;
    }
    
    /**
     * Update complaint
     */
    public Complaint updateComplaint(Complaint complaint) {
        logger.info("Updating complaint with ID: {}", complaint.getId());
        
        Optional<Complaint> existingComplaintOpt = complaintRepository.findById(complaint.getId());
        if (existingComplaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaint.getId());
        }
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        logger.info("Complaint updated successfully with ID: {}", savedComplaint.getId());
        
        return savedComplaint;
    }
    
    /**
     * Get complaint by ID
     */
    @Transactional(readOnly = true)
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }
    
    /**
     * Get all complaints
     */
    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }
    
    /**
     * Get complaints by creator
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByCreator(Long userId) {
        return complaintRepository.findByCreatedById(userId);
    }
    
    /**
     * Get complaints by type
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByType(ComplaintType type) {
        return complaintRepository.findByType(type);
    }
    
    /**
     * Get complaints by status
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status);
    }
    
    /**
     * Get complaints assigned to a staff member
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsAssignedTo(Long userId) {
        return complaintRepository.findByAssignedToId(userId);
    }
    
    /**
     * Get complaints by department
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByDepartment(Long departmentId) {
        return complaintRepository.findByTargetDepartmentId(departmentId);
    }
    
    /**
     * Get top voted complaints
     */
    @Transactional(readOnly = true)
    public List<Complaint> getTopVotedComplaints() {
        return complaintRepository.findTopVotedComplaints();
    }
    
    /**
     * Get complaints requiring student confirmation
     */
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsRequiringConfirmation() {
        return complaintRepository.findComplaintsRequiringConfirmation(ComplaintStatus.RESOLUTION_ANNOUNCED);
    }
    
    /**
     * Vote on a complaint
     */
    public void voteComplaint(Long complaintId, Long userId) {
        logger.info("User {} voting for complaint {}", userId, complaintId);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        User user;
        try {
            user = userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        // Check if user already voted
        if (complaintVoteRepository.existsByUserAndComplaint(user, complaint)) {
            throw new RuntimeException("User already voted for this complaint");
        }
        
        // Create vote
        ComplaintVote vote = new ComplaintVote(complaint, user);
        complaintVoteRepository.save(vote);
        
        // Update complaint vote count
        complaint.setTotalVotes(complaint.getTotalVotes() + 1);
        complaintRepository.save(complaint);
        
        logger.info("Vote recorded successfully. Total votes for complaint {}: {}", complaintId, complaint.getTotalVotes());
    }
    
    /**
     * Remove vote from complaint
     */
    public void removeVote(Long complaintId, Long userId) {
        logger.info("User {} removing vote from complaint {}", userId, complaintId);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        User user;
        try {
            user = userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        Optional<ComplaintVote> voteOpt = complaintVoteRepository.findByUserAndComplaint(user, complaint);
        if (voteOpt.isPresent()) {
            complaintVoteRepository.delete(voteOpt.get());
            
            // Update complaint vote count
            complaint.setTotalVotes(Math.max(0, complaint.getTotalVotes() - 1));
            complaintRepository.save(complaint);
            
            logger.info("Vote removed successfully. Total votes for complaint {}: {}", complaintId, complaint.getTotalVotes());
        }
    }
    
    /**
     * Change complaint status
     */
    public void changeComplaintStatus(Long complaintId, ComplaintStatus newStatus, Long changedByUserId, String notes) {
        logger.info("Changing complaint {} status to {}", complaintId, newStatus);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        User changedBy;
        try {
            changedBy = userService.getUserById(changedByUserId);
        } catch (RuntimeException e) {
            throw new RuntimeException("User not found with ID: " + changedByUserId);
        }
        
        Complaint complaint = complaintOpt.get();
        ComplaintStatus oldStatus = complaint.getStatus();
        
        // Update complaint status
        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // Handle special status changes
        if (newStatus == ComplaintStatus.CONFIRMED_BY_STUDENT) {
            complaint.setStudentConfirmation(true);
        } else if (newStatus == ComplaintStatus.RESOLUTION_ANNOUNCED) {
            complaint.setStudentConfirmation(false); // Reset for student confirmation
        }
        
        complaintRepository.save(complaint);
        
        // Create status history
        createStatusHistory(complaint, oldStatus, newStatus, changedBy, notes);
        
        logger.info("Complaint status changed from {} to {}", oldStatus, newStatus);
    }
    
    /**
     * Assign complaint to staff member
     */
    public void assignComplaint(Long complaintId, Long staffUserId, Long assignedByUserId) {
        logger.info("Assigning complaint {} to staff {}", complaintId, staffUserId);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        User staff;
        try {
            staff = userService.getUserById(staffUserId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Staff user not found with ID: " + staffUserId);
        }
        if (staff.getRole() != UserRole.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }
        
        Complaint complaint = complaintOpt.get();
        complaint.setAssignedTo(staff);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        // Auto-change status to ASSIGNED if currently NEW
        if (complaint.getStatus() == ComplaintStatus.NEW) {
            changeComplaintStatus(complaintId, ComplaintStatus.ASSIGNED, assignedByUserId, "Auto-assigned to staff member");
        } else {
            complaintRepository.save(complaint);
        }
        
        logger.info("Complaint assigned to staff member: {}", staff.getUsername());
    }
    
    /**
     * Add comment to complaint
     */
    public void addComment(Long complaintId, Long userId, String comment, Boolean isResolutionAnnouncement) {
        logger.info("Adding comment to complaint {}", complaintId);
        
        Optional<Complaint> complaintOpt = complaintRepository.findById(complaintId);
        if (complaintOpt.isEmpty()) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        User user;
        try {
            user = userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        Complaint complaint = complaintOpt.get();
        
        ComplaintComment complaintComment = new ComplaintComment(comment, complaint, user, isResolutionAnnouncement);
        commentRepository.save(complaintComment);
        
        logger.info("Comment added to complaint {}", complaintId);
    }
    
    /**
     * Delete complaint
     */
    public void deleteComplaint(Long complaintId) {
        logger.info("Deleting complaint with ID: {}", complaintId);
        
        if (!complaintRepository.existsById(complaintId)) {
            throw new RuntimeException("Complaint not found with ID: " + complaintId);
        }
        
        // Delete related records first
        commentRepository.deleteByComplaintId(complaintId);
        complaintVoteRepository.deleteByComplaintId(complaintId);
        statusHistoryRepository.deleteByComplaintId(complaintId);
        
        // Delete complaint
        complaintRepository.deleteById(complaintId);
        
        logger.info("Complaint deleted successfully with ID: {}", complaintId);
    }
    
    /**
     * Get complaint statistics for admin dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplaintStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get total complaints by status
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.NEW);
        long inProgressComplaints = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long resolvedComplaints = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long rejectedComplaints = complaintRepository.countByStatus(ComplaintStatus.CONFIRMED_BY_STUDENT);
        
        stats.put("total", totalComplaints);
        stats.put("pending", pendingComplaints);
        stats.put("inProgress", inProgressComplaints);
        stats.put("resolved", resolvedComplaints);
        stats.put("rejected", rejectedComplaints);
        
        // Get complaints by type
        List<Object[]> typeStats = complaintRepository.getComplaintStatisticsByType();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] stat : typeStats) {
            typeMap.put(((ComplaintType) stat[0]).name(), (Long) stat[1]);
        }
        stats.put("byType", typeMap);
        
        return stats;
    }
    
    /**
     * Get average resolution time in hours
     */
    @Transactional(readOnly = true)
    public Double getAverageResolutionTime() {
        // Calculate average resolution time from resolved complaints
        List<Complaint> resolvedComplaints = complaintRepository.findByStatus(ComplaintStatus.CLOSED);
        
        if (resolvedComplaints.isEmpty()) {
            return 0.0;
        }
        
        double totalHours = 0.0;
        int count = 0;
        
        for (Complaint complaint : resolvedComplaints) {
            if (complaint.getCreatedAt() != null && complaint.getUpdatedAt() != null) {
                long hours = java.time.Duration.between(complaint.getCreatedAt(), complaint.getUpdatedAt()).toHours();
                totalHours += hours;
                count++;
            }
        }
        
        return count > 0 ? totalHours / count : 0.0;
    }
    
    /**
     * Get most active department
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMostActiveDepartment() {
        // Get all complaints and count by department
        List<Complaint> allComplaints = complaintRepository.findAll();
        
        Map<String, Long> departmentCounts = new HashMap<>();
        
        for (Complaint complaint : allComplaints) {
            if (complaint.getTargetDepartment() != null) {
                String deptName = complaint.getTargetDepartment().getName();
                departmentCounts.put(deptName, departmentCounts.getOrDefault(deptName, 0L) + 1);
            }
        }
        
        if (departmentCounts.isEmpty()) {
            return Map.of("departmentName", "N/A", "complaintCount", 0L);
        }
        
        // Find department with most complaints
        String mostActiveDept = departmentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
            
        Long complaintCount = departmentCounts.getOrDefault(mostActiveDept, 0L);
        
        return Map.of(
            "departmentName", mostActiveDept,
            "complaintCount", complaintCount
        );
    }
    
    /**
     * Get satisfaction rate (percentage of resolved complaints vs total)
     */
    @Transactional(readOnly = true)
    public Double getSatisfactionRate() {
        long totalComplaints = complaintRepository.count();
        long resolvedComplaints = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        
        if (totalComplaints == 0) {
            return 0.0;
        }
        
        return (double) resolvedComplaints / totalComplaints * 100;
    }
    
    /**
     * Get complaint statistics
     */
    @Transactional(readOnly = true)
    public Object[] getComplaintStatistics() {
        List<Object[]> typeStats = complaintRepository.getComplaintStatisticsByType();
        List<Object[]> statusStats = complaintRepository.getComplaintStatisticsByStatus();
        
        return new Object[]{
            typeStats,
            statusStats,
            complaintRepository.count()
        };
    }
    
    /**
     * Create status history record
     */
    private void createStatusHistory(Complaint complaint, ComplaintStatus fromStatus, 
                                   ComplaintStatus toStatus, User changedBy, String notes) {
        String comment = "Status changed from " + fromStatus + " to " + toStatus;
        if (notes != null && !notes.isEmpty()) {
            comment += ": " + notes;
        }
        ComplaintStatusHistory history = new ComplaintStatusHistory(complaint, toStatus, comment, changedBy);
        statusHistoryRepository.save(history);
    }
}
