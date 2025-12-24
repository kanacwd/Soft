package com.aiu.scrs.repository;

import com.aiu.scrs.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Department Repository - Data access layer for Department entity
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by name
     */
    Optional<Department> findByName(String name);
    
    /**
     * Check if department name exists
     */
    boolean existsByName(String name);
    
    /**
     * Find active departments
     */
    List<Department> findByIsActive(Boolean isActive);
    
    /**
     * Find departments with staff members
     */
    @Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.staff WHERE d.isActive = :isActive")
    List<Department> findActiveDepartmentsWithStaff(@Param("isActive") Boolean isActive);
    
    /**
     * Find departments with complaints
     */
    @Query("SELECT DISTINCT d FROM Department d JOIN FETCH d.complaints WHERE d.isActive = :isActive")
    List<Department> findActiveDepartmentsWithComplaints(@Param("isActive") Boolean isActive);
    
    /**
     * Find departments by complaint type
     */
    @Query("SELECT DISTINCT d FROM Department d JOIN d.complaints c WHERE c.type = :type AND d.isActive = true")
    List<Department> findDepartmentsByComplaintType(@Param("type") com.aiu.scrs.entity.ComplaintType type);
}
