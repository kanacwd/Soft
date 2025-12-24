package com.aiu.scrs.dto.department;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Department Request DTO for creating/updating departments
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentRequest {
    
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    private String contactEmail;
    private String contactPhone;
    
    private Long headOfDepartmentId;
    
    // Constructors
    public DepartmentRequest() {}
    
    public DepartmentRequest(String name, String description, String location, String contactEmail, String contactPhone, Long headOfDepartmentId) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.headOfDepartmentId = headOfDepartmentId;
    }
    
    // Getters and Setters
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
    
    public Long getHeadOfDepartmentId() {
        return headOfDepartmentId;
    }
    
    public void setHeadOfDepartmentId(Long headOfDepartmentId) {
        this.headOfDepartmentId = headOfDepartmentId;
    }
}
