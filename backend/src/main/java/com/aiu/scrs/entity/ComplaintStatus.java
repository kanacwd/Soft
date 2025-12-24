package com.aiu.scrs.entity;

/**
 * Complaint Status Enum - Tracks complaint lifecycle
 */
public enum ComplaintStatus {
    NEW("New - Complaint just submitted"),
    ASSIGNED("Assigned - Complaint assigned to staff member"),
    IN_PROGRESS("In Progress - Work is being done on the complaint"),
    RESOLUTION_ANNOUNCED("Resolution Announced - Staff announced resolution"),
    CONFIRMED_BY_STUDENT("Confirmed by Student - Student confirmed satisfaction"),
    CLOSED("Closed - Complaint is fully resolved");
    
    private final String description;
    
    ComplaintStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
