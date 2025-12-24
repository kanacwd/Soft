package com.aiu.scrs.service;

import com.aiu.scrs.entity.ComplaintType;
import com.aiu.scrs.entity.Department;
import com.aiu.scrs.entity.User;
import com.aiu.scrs.repository.DepartmentRepository;
import com.aiu.scrs.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Department Service - Business logic for department management
 */
@Service
@Transactional
public class DepartmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new department
     */
    public Department createDepartment(Department department) {
        logger.info("Creating department with name: {}", department.getName());
        
        // Check if department name already exists
        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department name already exists: " + department.getName());
        }
        
        // Set default values
        department.setIsActive(true);
        
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department created successfully with ID: {}", savedDepartment.getId());
        
        return savedDepartment;
    }
    
    /**
     * Update an existing department
     */
    public Department updateDepartment(Department department) {
        logger.info("Updating department with ID: {}", department.getId());
        
        Optional<Department> existingDepartmentOpt = departmentRepository.findById(department.getId());
        if (existingDepartmentOpt.isEmpty()) {
            throw new RuntimeException("Department not found with ID: " + department.getId());
        }
        
        Department existingDepartment = existingDepartmentOpt.get();
        
        // Check name uniqueness (excluding current department)
        if (!existingDepartment.getName().equals(department.getName()) && 
            departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department name already exists: " + department.getName());
        }
        
        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department updated successfully with ID: {}", savedDepartment.getId());
        
        return savedDepartment;
    }
    
    /**
     * Get department by ID
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }
    
    /**
     * Get department by name
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }
    
    /**
     * Get all departments
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    /**
     * Get active departments
     */
    @Transactional(readOnly = true)
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByIsActive(true);
    }
    
    /**
     * Get departments with staff members
     */
    @Transactional(readOnly = true)
    public List<Department> getActiveDepartmentsWithStaff() {
        return departmentRepository.findActiveDepartmentsWithStaff(true);
    }
    
    /**
     * Get departments with complaints
     */
    @Transactional(readOnly = true)
    public List<Department> getActiveDepartmentsWithComplaints() {
        return departmentRepository.findActiveDepartmentsWithComplaints(true);
    }
    
    /**
     * Get department by complaint type
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByComplaintType(ComplaintType type) {
        List<Department> departments = departmentRepository.findDepartmentsByComplaintType(type);
        return departments.isEmpty() ? Optional.empty() : Optional.of(departments.get(0));
    }
    
    /**
     * Get staff members in a department
     */
    @Transactional(readOnly = true)
    public List<User> getStaffByDepartment(Long departmentId) {
        return userRepository.findByDepartmentIdAndIsActive(departmentId, true);
    }
    
    /**
     * Get complaints assigned to department
     */
    @Transactional(readOnly = true)
    public List<com.aiu.scrs.entity.Complaint> getComplaintsByDepartment(Long departmentId) {
        // This would require ComplaintRepository injection, but to avoid circular dependency,
        // we'll handle this in the ComplaintService
        return null; // Placeholder
    }
    
    /**
     * Deactivate department
     */
    public void deactivateDepartment(Long departmentId) {
        logger.info("Deactivating department with ID: {}", departmentId);
        
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            department.setIsActive(false);
            departmentRepository.save(department);
            logger.info("Department deactivated successfully with ID: {}", departmentId);
        } else {
            throw new RuntimeException("Department not found with ID: " + departmentId);
        }
    }
    
    /**
     * Activate department
     */
    public void activateDepartment(Long departmentId) {
        logger.info("Activating department with ID: {}", departmentId);
        
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            department.setIsActive(true);
            departmentRepository.save(department);
            logger.info("Department activated successfully with ID: {}", departmentId);
        } else {
            throw new RuntimeException("Department not found with ID: " + departmentId);
        }
    }
    
    /**
     * Delete department
     */
    public void deleteDepartment(Long departmentId) {
        logger.info("Deleting department with ID: {}", departmentId);
        
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Department not found with ID: " + departmentId);
        }
        
        departmentRepository.deleteById(departmentId);
        logger.info("Department deleted successfully with ID: {}", departmentId);
    }
    
    /**
     * Check if department name exists
     */
    @Transactional(readOnly = true)
    public boolean departmentNameExists(String name) {
        return departmentRepository.existsByName(name);
    }
    
    /**
     * Get department statistics
     */
    @Transactional(readOnly = true)
    public Object[] getDepartmentStatistics() {
        long totalDepartments = departmentRepository.count();
        long activeDepartments = departmentRepository.findByIsActive(true).size();
        long inactiveDepartments = departmentRepository.findByIsActive(false).size();
        
        return new Object[]{
            totalDepartments,
            activeDepartments,
            inactiveDepartments
        };
    }
}
