package com.aiu.scrs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Complaint Entity - Main complaint tracking system
 * Supports both Academic and Facility complaint types
 */
@Entity
@Table(name = "complaints")
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintType type; // ACADEMIC, FACILITY
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status = ComplaintStatus.NEW;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes = 0;
    
    @Column(name = "student_confirmation", nullable = false)
    private Boolean studentConfirmation = false;
    
    @Column(name = "resolution_announced_at")
    private LocalDateTime resolutionAnnouncedAt;
    
    @Column(name = "confirmed_by_student_at")
    private LocalDateTime confirmedByStudentAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComplaintVote> votes = new ArrayList<>();
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComplaintStatusHistory> statusHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ComplaintComment> comments = new ArrayList<>();
    
    // Constructors
    public Complaint() {}
    
    public Complaint(String title, String description, ComplaintType type, User createdBy, Department targetDepartment) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.createdBy = createdBy;
        this.targetDepartment = targetDepartment;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ComplaintType getType() {
        return type;
    }
    
    public void setType(ComplaintType type) {
        this.type = type;
    }
    
    public ComplaintStatus getStatus() {
        return status;
    }
    
    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public Department getTargetDepartment() {
        return targetDepartment;
    }
    
    public void setTargetDepartment(Department targetDepartment) {
        this.targetDepartment = targetDepartment;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Integer getTotalVotes() {
        return totalVotes;
    }
    
    public void setTotalVotes(Integer totalVotes) {
        this.totalVotes = totalVotes;
    }
    
    public Boolean getStudentConfirmation() {
        return studentConfirmation;
    }
    
    public void setStudentConfirmation(Boolean studentConfirmation) {
        this.studentConfirmation = studentConfirmation;
    }
    
    public LocalDateTime getResolutionAnnouncedAt() {
        return resolutionAnnouncedAt;
    }
    
    public void setResolutionAnnouncedAt(LocalDateTime resolutionAnnouncedAt) {
        this.resolutionAnnouncedAt = resolutionAnnouncedAt;
    }
    
    public LocalDateTime getConfirmedByStudentAt() {
        return confirmedByStudentAt;
    }
    
    public void setConfirmedByStudentAt(LocalDateTime confirmedByStudentAt) {
        this.confirmedByStudentAt = confirmedByStudentAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ComplaintVote> getVotes() {
        return votes;
    }
    
    public void setVotes(List<ComplaintVote> votes) {
        this.votes = votes;
    }
    
    public List<ComplaintStatusHistory> getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(List<ComplaintStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public List<ComplaintComment> getComments() {
        return comments;
    }
    
    public void setComments(List<ComplaintComment> comments) {
        this.comments = comments;
    }
    
    // Utility methods
    public void addVote(ComplaintVote vote) {
        votes.add(vote);
        vote.setComplaint(this);
        this.totalVotes = votes.size();
    }
    
    public void removeVote(ComplaintVote vote) {
        votes.remove(vote);
        vote.setComplaint(null);
        this.totalVotes = votes.size();
    }
    
    public void addStatusHistory(ComplaintStatusHistory history) {
        statusHistory.add(history);
        history.setComplaint(this);
    }
    
    public void addComment(ComplaintComment comment) {
        comments.add(comment);
        comment.setComplaint(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(id, complaint.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", totalVotes=" + totalVotes +
                ", createdAt=" + createdAt +
                '}';
    }
}
