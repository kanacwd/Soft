package com.aiu.scrs.repository;

import com.aiu.scrs.entity.Complaint;
import com.aiu.scrs.entity.ComplaintVote;
import com.aiu.scrs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ComplaintVote Repository - Data access layer for ComplaintVote entity
 */
@Repository
public interface ComplaintVoteRepository extends JpaRepository<ComplaintVote, Long> {
    
    /**
     * Find votes by user
     */
    List<ComplaintVote> findByUser(User user);
    
    /**
     * Find votes by user ID
     */
    List<ComplaintVote> findByUserId(Long userId);
    
    /**
     * Find votes by complaint
     */
    List<ComplaintVote> findByComplaint(Complaint complaint);
    
    /**
     * Find votes by complaint ID
     */
    List<ComplaintVote> findByComplaintId(Long complaintId);
    
    /**
     * Check if user has voted for a complaint
     */
    boolean existsByUserAndComplaint(User user, Complaint complaint);
    
    /**
     * Check if user has voted for a complaint by IDs
     */
    boolean existsByUserIdAndComplaintId(Long userId, Long complaintId);
    
    /**
     * Find vote by user and complaint
     */
    Optional<ComplaintVote> findByUserAndComplaint(User user, Complaint complaint);
    
    /**
     * Find vote by user ID and complaint ID
     */
    Optional<ComplaintVote> findByUserIdAndComplaintId(Long userId, Long complaintId);
    
    /**
     * Count votes for a complaint
     */
    @Query("SELECT COUNT(cv) FROM ComplaintVote cv WHERE cv.complaint = :complaint")
    Long countByComplaint(@Param("complaint") Complaint complaint);
    
    /**
     * Count votes for a complaint by ID
     */
    @Query("SELECT COUNT(cv) FROM ComplaintVote cv WHERE cv.complaint.id = :complaintId")
    Long countByComplaintId(@Param("complaintId") Long complaintId);
    
    /**
     * Get top voted complaints by vote count
     */
    @Query("SELECT cv.complaint, COUNT(cv) as voteCount FROM ComplaintVote cv " +
           "GROUP BY cv.complaint ORDER BY voteCount DESC")
    List<Object[]> getTopVotedComplaints();
    
    /**
     * Find votes with complaint details
     */
    @Query("SELECT cv FROM ComplaintVote cv JOIN FETCH cv.complaint JOIN FETCH cv.user")
    List<ComplaintVote> findAllWithDetails();
    
    /**
     * Find user's votes with complaint details
     */
    @Query("SELECT cv FROM ComplaintVote cv JOIN FETCH cv.complaint WHERE cv.user = :user")
    List<ComplaintVote> findByUserWithComplaints(@Param("user") User user);
    
    /**
     * Find complaint votes ordered by creation date
     */
    List<ComplaintVote> findAllByOrderByCreatedAtDesc();
    
    /**
     * Delete votes by complaint ID
     */
    void deleteByComplaintId(Long complaintId);
}
