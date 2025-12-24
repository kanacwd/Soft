package com.aiu.scrs.dto.department;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Department Response DTO for department information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentResponse {
    
    private Long id;
    private String name;
    private String description;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private String createdAt;
    private String updatedAt;
    
    // Head of Department information
    private Long headOfDepartmentId;
    private String headOfDepartmentName;
    private String headOfDepartmentUsername;
    
    // Statistics
    private int totalComplaints;
    private int pendingComplaints;
    private int resolvedComplaints;
    private int staffCount;
    
    // Constructors
    public DepartmentResponse() {}
    
    public DepartmentResponse(Long id, String name, String description, String location, 
                            String contactEmail, String contactPhone, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
    
    public Long getHeadOfDepartmentId() {
        return headOfDepartmentId;
    }
    
    public void setHeadOfDepartmentId(Long headOfDepartmentId) {
        this.headOfDepartmentId = headOfDepartmentId;
    }
    
    public String getHeadOfDepartmentName() {
        return headOfDepartmentName;
    }
    
    public void setHeadOfDepartmentName(String headOfDepartmentName) {
        this.headOfDepartmentName = headOfDepartmentName;
    }
    
    public String getHeadOfDepartmentUsername() {
        return headOfDepartmentUsername;
    }
    
    public void setHeadOfDepartmentUsername(String headOfDepartmentUsername) {
        this.headOfDepartmentUsername = headOfDepartmentUsername;
    }
    
    public int getTotalComplaints() {
        return totalComplaints;
    }
    
    public void setTotalComplaints(int totalComplaints) {
        this.totalComplaints = totalComplaints;
    }
    
    public int getPendingComplaints() {
        return pendingComplaints;
    }
    
    public void setPendingComplaints(int pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }
    
    public int getResolvedComplaints() {
        return resolvedComplaints;
    }
    
    public void setResolvedComplaints(int resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }
    
    public int getStaffCount() {
        return staffCount;
    }
    
    public void setStaffCount(int staffCount) {
        this.staffCount = staffCount;
    }
}
