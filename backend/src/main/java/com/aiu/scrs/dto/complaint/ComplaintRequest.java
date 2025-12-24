package com.aiu.scrs.dto.complaint;

import com.aiu.scrs.entity.ComplaintStatus;
import com.aiu.scrs.entity.ComplaintType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Complaint Request DTO for creating/updating complaints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Complaint type is required")
    private ComplaintType type;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String location;
    private String attachments;
    
    // Constructors
    public ComplaintRequest() {}
    
    public ComplaintRequest(String title, String description, ComplaintType type, Long departmentId, String location, String attachments) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.departmentId = departmentId;
        this.location = location;
        this.attachments = attachments;
    }
    
    // Getters and Setters
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
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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
}
