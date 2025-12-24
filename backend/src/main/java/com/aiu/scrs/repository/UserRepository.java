package com.aiu.scrs.repository;

import com.aiu.scrs.entity.User;
import com.aiu.scrs.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository - Data access layer for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username (AIU Student ID or Staff ID)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find active users by role
     */
    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);
    
    /**
     * Find users by department
     */
    List<User> findByDepartmentId(Long departmentId);
    
    /**
     * Find active users by department
     */
    List<User> findByDepartmentIdAndIsActive(Long departmentId, Boolean isActive);
    
    /**
     * Find students (users with STUDENT role)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findStudents(@Param("role") UserRole role);
    
    /**
     * Find staff members (users with STAFF role)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findStaff(@Param("role") UserRole role);
    
    /**
     * Find administrators (users with ADMIN role)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAdmins(@Param("role") UserRole role);
    
    /**
     * Find users by role and department
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.department.id = :departmentId")
    List<User> findByRoleAndDepartmentId(@Param("role") UserRole role, 
                                       @Param("departmentId") Long departmentId);
    
    /**
     * Find active users
     */
    List<User> findByIsActive(Boolean isActive);
}
