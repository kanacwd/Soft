package com.aiu.scrs.repository;

import com.aiu.scrs.entity.Complaint;
import com.aiu.scrs.entity.ComplaintComment;
import com.aiu.scrs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ComplaintComment Repository - Data access layer for ComplaintComment entity
 */
@Repository
public interface ComplaintCommentRepository extends JpaRepository<ComplaintComment, Long> {
    
    /**
     * Find comments by complaint
     */
    List<ComplaintComment> findByComplaint(Complaint complaint);
    
    /**
     * Find comments by complaint ID
     */
    List<ComplaintComment> findByComplaintId(Long complaintId);
    
    /**
     * Find comments by user
     */
    List<ComplaintComment> findByUser(User user);
    
    /**
     * Find comments by user ID
     */
    List<ComplaintComment> findByUserId(Long userId);
    
    /**
     * Find internal comments
     */
    List<ComplaintComment> findByIsInternal(Boolean isInternal);
    
    /**
     * Find internal comments for a specific complaint
     */
    List<ComplaintComment> findByComplaintAndIsInternal(Complaint complaint, Boolean isInternal);
    
    /**
     * Find comments by complaint ordered by creation date
     */
    List<ComplaintComment> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
    
    /**
     * Find comments by complaint ordered by creation date descending
     */
    List<ComplaintComment> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    
    /**
     * Find recent comments
     */
    List<ComplaintComment> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find internal comments by date range
     */
    List<ComplaintComment> findByIsInternalAndCreatedAtBetween(Boolean isInternal, 
                                                               LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all comments with details
     */
    @Query("SELECT cc FROM ComplaintComment cc JOIN FETCH cc.complaint JOIN FETCH cc.user")
    List<ComplaintComment> findAllWithDetails();
    
    /**
     * Find comments by complaint with user details
     */
    @Query("SELECT cc FROM ComplaintComment cc JOIN FETCH cc.user WHERE cc.complaint = :complaint")
    List<ComplaintComment> findByComplaintWithUser(@Param("complaint") Complaint complaint);
    
    /**
     * Find internal comments with complaint details
     */
    @Query("SELECT cc FROM ComplaintComment cc JOIN FETCH cc.complaint WHERE cc.isInternal = true")
    List<ComplaintComment> findInternalCommentsWithComplaints();
    
    /**
     * Count comments for a complaint
     */
    @Query("SELECT COUNT(cc) FROM ComplaintComment cc WHERE cc.complaint = :complaint")
    Long countByComplaint(@Param("complaint") Complaint complaint);
    
    /**
     * Count internal comments for a complaint
     */
    @Query("SELECT COUNT(cc) FROM ComplaintComment cc WHERE cc.complaint = :complaint AND cc.isInternal = true")
    Long countInternalCommentsByComplaint(@Param("complaint") Complaint complaint);
    
    /**
     * Count comments by user
     */
    @Query("SELECT COUNT(cc) FROM ComplaintComment cc WHERE cc.user = :user")
    Long countByUser(@Param("user") User user);
    
    /**
     * Delete comments by complaint ID
     */
    void deleteByComplaintId(Long complaintId);
}
