package com.aiu.scrs.dto.complaint;

import com.aiu.scrs.entity.ComplaintStatus;
import com.aiu.scrs.entity.ComplaintType;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Complaint Response DTO for complaint information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintResponse {
    
    private Long id;
    private String title;
    private String description;
    private ComplaintType type;
    private ComplaintStatus status;
    private String location;
    private String attachments;
    private String createdAt;
    private String updatedAt;
    
    // User information
    private Long submittedById;
    private String submittedByUsername;
    private String submittedByFullName;
    
    // Department information
    private Long departmentId;
    private String departmentName;
    
    // Additional fields
    private int voteCount;
    private boolean userHasVoted;
    private int commentCount;
    
    // Constructors
    public ComplaintResponse() {}
    
    public ComplaintResponse(Long id, String title, String description, ComplaintType type, 
                           ComplaintStatus status, String location, String attachments, 
                           String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.location = location;
        this.attachments = attachments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getAttachments() {
        return attachments;
    }
    
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getSubmittedById() {
        return submittedById;
    }
    
    public void setSubmittedById(Long submittedById) {
        this.submittedById = submittedById;
    }
    
    public String getSubmittedByUsername() {
        return submittedByUsername;
    }
    
    public void setSubmittedByUsername(String submittedByUsername) {
        this.submittedByUsername = submittedByUsername;
    }
    
    public String getSubmittedByFullName() {
        return submittedByFullName;
    }
    
    public void setSubmittedByFullName(String submittedByFullName) {
        this.submittedByFullName = submittedByFullName;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public int getVoteCount() {
        return voteCount;
    }
    
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
    
    public boolean isUserHasVoted() {
        return userHasVoted;
    }
    
    public void setUserHasVoted(boolean userHasVoted) {
        this.userHasVoted = userHasVoted;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
