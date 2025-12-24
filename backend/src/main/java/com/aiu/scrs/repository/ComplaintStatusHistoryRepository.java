package com.aiu.scrs.repository;

import com.aiu.scrs.entity.Complaint;
import com.aiu.scrs.entity.ComplaintStatusHistory;
import com.aiu.scrs.entity.ComplaintStatus;
import com.aiu.scrs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ComplaintStatusHistory Repository - Data access layer for ComplaintStatusHistory entity
 */
@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
    
    /**
     * Find status history by complaint
     */
    List<ComplaintStatusHistory> findByComplaint(Complaint complaint);
    
    /**
     * Find status history by complaint ID
     */
    List<ComplaintStatusHistory> findByComplaintId(Long complaintId);
    
    /**
     * Find status history by user who changed the status
     */
    List<ComplaintStatusHistory> findByChangedBy(User changedBy);
    
    /**
     * Find status history by user ID
     */
    List<ComplaintStatusHistory> findByChangedById(Long userId);
    
    /**
     * Find recent status changes
     */
    List<ComplaintStatusHistory> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find status history by complaint ordered by change date
     */
    List<ComplaintStatusHistory> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
    
    /**
     * Find status changes to a specific status
     */
    List<ComplaintStatusHistory> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);
    
    /**
     * Find status changes by specific status
     */
    List<ComplaintStatusHistory> findByStatus(ComplaintStatus status);
    
    /**
     * Get status change statistics
     */
    @Query("SELECT csh.status, COUNT(csh) as count FROM ComplaintStatusHistory csh GROUP BY csh.status")
    List<Object[]> getStatusChangeStatistics();
    
    /**
     * Find all status changes with details
     */
    @Query("SELECT csh FROM ComplaintStatusHistory csh JOIN FETCH csh.complaint JOIN FETCH csh.changedBy")
    List<ComplaintStatusHistory> findAllWithDetails();
    
    /**
     * Find status changes by complaint with user details
     */
    @Query("SELECT csh FROM ComplaintStatusHistory csh JOIN FETCH csh.changedBy WHERE csh.complaint = :complaint")
    List<ComplaintStatusHistory> findByComplaintWithUser(@Param("complaint") Complaint complaint);
    
    /**
     * Delete status history by complaint ID
     */
    void deleteByComplaintId(Long complaintId);
}
