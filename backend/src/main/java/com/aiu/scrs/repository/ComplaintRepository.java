package com.aiu.scrs.repository;

import com.aiu.scrs.entity.Complaint;
import com.aiu.scrs.entity.ComplaintStatus;
import com.aiu.scrs.entity.ComplaintType;
import com.aiu.scrs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complaint Repository - Data access layer for Complaint entity
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    /**
     * Find complaints by creator
     */
    List<Complaint> findByCreatedBy(User createdBy);
    
    /**
     * Find complaints by creator ID
     */
    List<Complaint> findByCreatedById(Long userId);
    
    /**
     * Find complaints by type
     */
    List<Complaint> findByType(ComplaintType type);
    
    /**
     * Find complaints by status
     */
    List<Complaint> findByStatus(ComplaintStatus status);
    
    /**
     * Find complaints by assigned staff member
     */
    List<Complaint> findByAssignedTo(User assignedTo);
    
    /**
     * Find complaints by assigned staff member ID
     */
    List<Complaint> findByAssignedToId(Long userId);
    
    /**
     * Find complaints by target department
     */
    List<Complaint> findByTargetDepartmentId(Long departmentId);
    
    /**
     * Find complaints ordered by total votes (descending) - for prioritization
     */
    List<Complaint> findAllByOrderByTotalVotesDesc();
    
    /**
     * Find complaints by type ordered by votes
     */
    List<Complaint> findByTypeOrderByTotalVotesDesc(ComplaintType type);
    
    /**
     * Find complaints by status ordered by votes
     */
    List<Complaint> findByStatusOrderByTotalVotesDesc(ComplaintStatus status);
    
    /**
     * Find complaints requiring student confirmation
     */
    @Query("SELECT c FROM Complaint c WHERE c.status = :status AND c.studentConfirmation = false")
    List<Complaint> findComplaintsRequiringConfirmation(@Param("status") ComplaintStatus status);
    
    /**
     * Find complaints by date range
     */
    List<Complaint> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find complaints by type and date range
     */
    List<Complaint> findByTypeAndCreatedAtBetween(ComplaintType type, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find recent complaints (last N days)
     */
    @Query("SELECT c FROM Complaint c WHERE c.createdAt >= :sinceDate ORDER BY c.createdAt DESC")
    List<Complaint> findRecentComplaints(@Param("sinceDate") LocalDateTime sinceDate);
    
    /**
     * Find top voted complaints
     */
    @Query("SELECT c FROM Complaint c WHERE c.totalVotes > 0 ORDER BY c.totalVotes DESC, c.createdAt DESC")
    List<Complaint> findTopVotedComplaints();
    
    /**
     * Find complaints by department and status
     */
    @Query("SELECT c FROM Complaint c WHERE c.targetDepartment.id = :departmentId AND c.status = :status")
    List<Complaint> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                              @Param("status") ComplaintStatus status);
    
    /**
     * Count complaints by type
     */
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.type = :type")
    Long countByType(@Param("type") ComplaintType type);
    
    /**
     * Count complaints by status
     */
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    Long countByStatus(@Param("status") ComplaintStatus status);
    
    /**
     * Get complaint statistics by type
     */
    @Query("SELECT c.type, COUNT(c) as count FROM Complaint c GROUP BY c.type")
    List<Object[]> getComplaintStatisticsByType();
    
    /**
     * Get complaint statistics by status
     */
    @Query("SELECT c.status, COUNT(c) as count FROM Complaint c GROUP BY c.status")
    List<Object[]> getComplaintStatisticsByStatus();
}
