/**
 * SCRS API Client
 * Handles all backend communication, JWT token management, and API utilities
 */

class SCRSApiClient {
    constructor() {
        this.baseURL = 'http://localhost:8080/api'; // Default backend URL
        this.token = localStorage.getItem('scrs_token');
        this.user = null;
    }

    // Token Management
    setToken(token) {
        this.token = token;
        localStorage.setItem('scrs_token', token);
    }

    removeToken() {
        this.token = null;
        localStorage.removeItem('scrs_token');
        this.user = null;
    }

    getAuthHeaders() {
        return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    }

    // HTTP Request Utilities
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...this.getAuthHeaders(),
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || `HTTP error! status: ${response.status}`);
            }

            return data;
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // Authentication Methods
    async login(credentials) {
        try {
            const response = await this.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify(credentials)
            });

            if (response.success && response.data.token) {
                this.setToken(response.data.token);
                this.user = response.data.user;
                return response.data;
            } else {
                throw new Error(response.message || 'Login failed');
            }
        } catch (error) {
            throw error;
        }
    }

    async register(userData) {
        try {
            const response = await this.request('/auth/register', {
                method: 'POST',
                body: JSON.stringify(userData)
            });

            if (response.success && response.data.token) {
                this.setToken(response.data.token);
                this.user = response.data.user;
                return response.data;
            } else {
                throw new Error(response.message || 'Registration failed');
            }
        } catch (error) {
            throw error;
        }
    }

    async logout() {
        this.removeToken();
        // Optional: Call backend logout endpoint if needed
    }

    async validateToken() {
        try {
            const response = await this.request('/auth/validate');
            return response.success;
        } catch (error) {
            this.removeToken();
            return false;
        }
    }

    async getCurrentUser() {
        try {
            const response = await this.request('/auth/me');
            if (response.success) {
                this.user = response.data;
                return response.data;
            }
        } catch (error) {
            this.removeToken();
            throw error;
        }
    }

    // User Management
    async getUsers(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const response = await this.request(`/users?${queryString}`);
        return response.success ? response.data : [];
    }

    async getUserById(id) {
        const response = await this.request(`/users/${id}`);
        return response.success ? response.data : null;
    }

    async updateUser(id, userData) {
        const response = await this.request(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
        return response.success ? response.data : null;
    }

    async deleteUser(id) {
        const response = await this.request(`/users/${id}`, {
            method: 'DELETE'
        });
        return response.success;
    }

    // Complaint Management
    async getComplaints(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const response = await this.request(`/complaints?${queryString}`);
        return response.success ? response.data : [];
    }

    async getComplaintById(id) {
        const response = await this.request(`/complaints/${id}`);
        return response.success ? response.data : null;
    }

    async createComplaint(complaintData) {
        const response = await this.request('/complaints', {
            method: 'POST',
            body: JSON.stringify(complaintData)
        });
        return response.success ? response.data : null;
    }

    async updateComplaint(id, complaintData) {
        const response = await this.request(`/complaints/${id}`, {
            method: 'PUT',
            body: JSON.stringify(complaintData)
        });
        return response.success ? response.data : null;
    }

    async deleteComplaint(id) {
        const response = await this.request(`/complaints/${id}`, {
            method: 'DELETE'
        });
        return response.success;
    }

    // Voting System
    async voteComplaint(complaintId, voteType) {
        const response = await this.request(`/votes`, {
            method: 'POST',
            body: JSON.stringify({ complaintId, voteType })
        });
        return response.success ? response.data : null;
    }

    async getComplaintVotes(complaintId) {
        const response = await this.request(`/votes/complaint/${complaintId}`);
        return response.success ? response.data : [];
    }

    // Department Management
    async getDepartments() {
        const response = await this.request('/departments');
        return response.success ? response.data : [];
    }

    async getDepartmentById(id) {
        const response = await this.request(`/departments/${id}`);
        return response.success ? response.data : null;
    }

    async createDepartment(departmentData) {
        const response = await this.request('/departments', {
            method: 'POST',
            body: JSON.stringify(departmentData)
        });
        return response.success ? response.data : null;
    }

    async updateDepartment(id, departmentData) {
        const response = await this.request(`/departments/${id}`, {
            method: 'PUT',
            body: JSON.stringify(departmentData)
        });
        return response.success ? response.data : null;
    }

    async deleteDepartment(id) {
        const response = await this.request(`/departments/${id}`, {
            method: 'DELETE'
        });
        return response.success;
    }

    // Admin Methods
    async getSystemStats() {
        const response = await this.request('/admin/stats');
        return response.success ? response.data : null;
    }

    async getAllUsers() {
        const response = await this.request('/admin/users');
        return response.success ? response.data : [];
    }

    async updateUserRole(userId, role) {
        const response = await this.request(`/admin/users/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ role })
        });
        return response.success;
    }

    // Utility Methods
    isAuthenticated() {
        return !!this.token;
    }

    getCurrentUserRole() {
        return this.user?.role || null;
    }

    hasRole(role) {
        return this.user?.role === role;
    }

    hasAnyRole(roles) {
        return roles.includes(this.user?.role);
    }

    // Error Handling
    handleApiError(error) {
        if (error.message.includes('401') || error.message.includes('Unauthorized')) {
            this.removeToken();
            window.location.href = '/pages/auth/login.html';
        }
        return {
            success: false,
            message: error.message,
            data: null
        };
    }
}

// Create global API instance
const api = new SCRSApiClient();

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SCRSApiClient;
}
