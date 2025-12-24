/**
 * Staff Dashboard JavaScript Module
 * Handles all staff functionality including complaint management, status updates, and reporting
 */

// Global variables
let staffComplaints = [];
let currentPage = 1;
let itemsPerPage = 10;
let currentFilter = 'all';
let searchTerm = '';
let selectedComplaint = null;

// Initialize staff dashboard
document.addEventListener('DOMContentLoaded', function() {
    initializeStaffDashboard();
    loadStaffComplaints();
    setupEventListeners();
});

/**
 * Initialize staff dashboard
 */
async function initializeStaffDashboard() {
    try {
        await checkAuth();
        await loadDashboardStats();
        await loadRecentActivity();
        setupDashboardEventListeners();
    } catch (error) {
        console.error('Error initializing staff dashboard:', error);
        showError('Failed to initialize dashboard');
    }
}

/**
 * Check authentication and user role
 */
async function checkAuth() {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    
    if (!token || !user.role || (user.role !== 'STAFF' && user.role !== 'ADMIN')) {
        window.location.href = '/frontend/pages/auth/login.html';
        return;
    }
    
    // Update user info in header
    updateUserHeader(user);
}

/**
 * Update user information in header
 */
function updateUserHeader(user) {
    const userNameElement = document.getElementById('user-name');
    const userRoleElement = document.getElementById('user-role');
    
    if (userNameElement) userNameElement.textContent = user.fullName || user.username;
    if (userRoleElement) userRoleElement.textContent = user.role;
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Filter dropdown
    const filterSelect = document.getElementById('statusFilter');
    if (filterSelect) {
        filterSelect.addEventListener('change', handleFilterChange);
    }
    
    // Search input
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    // Pagination
    document.addEventListener('click', handlePaginationClick);
    
    // Modal handlers
    setupModalHandlers();
}

/**
 * Setup dashboard specific event listeners
 */
function setupDashboardEventListeners() {
    // Quick action buttons
    const pendingBtn = document.getElementById('pendingCount');
    const inProgressBtn = document.getElementById('inProgressCount');
    const resolvedBtn = document.getElementById('resolvedCount');
    
    if (pendingBtn) pendingBtn.addEventListener('click', () => filterByStatus('PENDING'));
    if (inProgressBtn) inProgressBtn.addEventListener('click', () => filterByStatus('IN_PROGRESS'));
    if (resolvedBtn) resolvedBtn.addEventListener('click', () => filterByStatus('RESOLVED'));
}

/**
 * Load dashboard statistics
 */
async function loadDashboardStats() {
    try {
        const response = await api.get('/api/complaints/stats');
        const stats = response.data;
        
        // Update dashboard cards
        updateStatsCard('totalCount', stats.total || 0);
        updateStatsCard('pendingCount', stats.pending || 0);
        updateStatsCard('inProgressCount', stats.inProgress || 0);
        updateStatsCard('resolvedCount', stats.resolved || 0);
        
        // Update charts if canvas elements exist
        updateStatusChart(stats);
        updateTrendChart(stats);
        
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
        showError('Failed to load dashboard statistics');
    }
}

/**
 * Update statistics card
 */
function updateStatsCard(elementId, value) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = value;
        // Add animation
        element.style.transform = 'scale(1.1)';
        setTimeout(() => {
            element.style.transform = 'scale(1)';
        }, 200);
    }
}

/**
 * Update status distribution chart
 */
function updateStatusChart(stats) {
    const canvas = document.getElementById('statusChart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    const data = [
        stats.pending || 0,
        stats.inProgress || 0,
        stats.resolved || 0,
        stats.rejected || 0
    ];
    
    // Simple chart implementation (you can replace with Chart.js)
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const colors = ['#ffc107', '#17a2b8', '#28a745', '#dc3545'];
    
    const total = data.reduce((sum, val) => sum + val, 0);
    let startAngle = -Math.PI / 2;
    
    data.forEach((value, index) => {
        const sliceAngle = (value / total) * 2 * Math.PI;
        ctx.beginPath();
        ctx.arc(100, 100, 80, startAngle, startAngle + sliceAngle);
        ctx.lineTo(100, 100);
        ctx.fillStyle = colors[index];
        ctx.fill();
        startAngle += sliceAngle;
    });
}

/**
 * Update trend chart
 */
function updateTrendChart(stats) {
    const canvas = document.getElementById('trendChart');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Simple line chart implementation
    const data = stats.weekly || [5, 8, 12, 15, 10, 18, 22];
    const maxValue = Math.max(...data);
    
    ctx.strokeStyle = '#007bff';
    ctx.lineWidth = 2;
    ctx.beginPath();
    
    data.forEach((value, index) => {
        const x = (index / (data.length - 1)) * (canvas.width - 40) + 20;
        const y = canvas.height - 20 - (value / maxValue) * (canvas.height - 40);
        
        if (index === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
        
        // Draw point
        ctx.fillStyle = '#007bff';
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, 2 * Math.PI);
        ctx.fill();
    });
    
    ctx.stroke();
}

/**
 * Load recent activity
 */
async function loadRecentActivity() {
    try {
        const response = await api.get('/api/complaints/recent?limit=5');
        const activities = response.data;
        
        const activityList = document.getElementById('recentActivity');
        if (activityList) {
            activityList.innerHTML = activities.map(activity => `
                <div class="activity-item">
                    <div class="activity-content">
                        <p><strong>${activity.complainerName}</strong> submitted a complaint</p>
                        <small>${formatDate(activity.createdAt)} - ${activity.department}</small>
                    </div>
                    <span class="status-badge status-${activity.status.toLowerCase()}">${activity.status}</span>
                </div>
            `).join('');
        }
        
    } catch (error) {
        console.error('Error loading recent activity:', error);
    }
}

/**
 * Load staff complaints with pagination and filtering
 */
async function loadStaffComplaints(page = 1, filter = 'all', search = '') {
    try {
        showLoading(true);
        
        const params = new URLSearchParams({
            page: page,
            size: itemsPerPage,
            filter: filter,
            search: search
        });
        
        const response = await api.get(`/api/complaints/staff?${params}`);
        staffComplaints = response.data.content || [];
        currentPage = page;
        currentFilter = filter;
        searchTerm = search;
        
        displayComplaints(staffComplaints);
        updatePagination(response.data);
        
    } catch (error) {
        console.error('Error loading complaints:', error);
        showError('Failed to load complaints');
    } finally {
        showLoading(false);
    }
}

/**
 * Display complaints in table
 */
function displayComplaints(complaints) {
    const tableBody = document.getElementById('complaintsTableBody');
    if (!tableBody) return;
    
    if (complaints.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center">No complaints found</td>
            </tr>
        `;
        return;
    }
    
    tableBody.innerHTML = complaints.map(complaint => `
        <tr data-complaint-id="${complaint.id}">
            <td>${complaint.id}</td>
            <td>
                <div class="complaint-title">${complaint.title}</div>
                <div class="complaint-description">${truncateText(complaint.description, 100)}</div>
            </td>
            <td>${complaint.complainerName}</td>
            <td>${complaint.department}</td>
            <td>${complaint.type}</td>
            <td>
                <span class="status-badge status-${complaint.status.toLowerCase()}">${complaint.status}</span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-sm btn-primary" onclick="viewComplaint(${complaint.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="updateStatus(${complaint.id})" title="Update Status">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-info" onclick="addComment(${complaint.id})" title="Add Comment">
                        <i class="fas fa-comment"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

/**
 * Handle filter change
 */
function handleFilterChange(event) {
    const filter = event.target.value;
    loadStaffComplaints(1, filter, searchTerm);
}

/**
 * Handle search input
 */
function handleSearch(event) {
    const search = event.target.value;
    searchTerm = search;
    
    // Debounce search
    clearTimeout(window.searchTimeout);
    window.searchTimeout = setTimeout(() => {
        loadStaffComplaints(1, currentFilter, search);
    }, 500);
}

/**
 * Filter by status (from dashboard cards)
 */
function filterByStatus(status) {
    const filterSelect = document.getElementById('statusFilter');
    if (filterSelect) {
        filterSelect.value = status;
    }
    loadStaffComplaints(1, status, searchTerm);
}

/**
 * Handle pagination clicks
 */
function handlePaginationClick(event) {
    if (event.target.classList.contains('page-link')) {
        event.preventDefault();
        const page = parseInt(event.target.dataset.page);
        if (page && page !== currentPage) {
            loadStaffComplaints(page, currentFilter, searchTerm);
        }
    }
}

/**
 * Update pagination UI
 */
function updatePagination(pageData) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;
    
    const { totalPages, number, totalElements } = pageData;
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let paginationHTML = '';
    
    // Previous button
    paginationHTML += `
        <li class="page-item ${number === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${number}">Previous</a>
        </li>
    `;
    
    // Page numbers
    for (let i = Math.max(0, number - 2); i <= Math.min(totalPages - 1, number + 2); i++) {
        paginationHTML += `
            <li class="page-item ${i === number ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i + 1}">${i + 1}</a>
            </li>
        `;
    }
    
    // Next button
    paginationHTML += `
        <li class="page-item ${number === totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${number + 2}">Next</a>
        </li>
    `;
    
    pagination.innerHTML = paginationHTML;
}

/**
 * View complaint details
 */
async function viewComplaint(complaintId) {
    try {
        const response = await api.get(`/api/complaints/${complaintId}`);
        const complaint = response.data;
        
        // Populate modal with complaint details
        populateComplaintModal(complaint);
        
        // Show modal
        const modal = document.getElementById('complaintDetailModal');
        if (modal) {
            modal.style.display = 'block';
        }
        
    } catch (error) {
        console.error('Error loading complaint details:', error);
        showError('Failed to load complaint details');
    }
}

/**
 * Populate complaint detail modal
 */
function populateComplaintModal(complaint) {
    document.getElementById('modalComplaintId').textContent = complaint.id;
    document.getElementById('modalComplaintTitle').textContent = complaint.title;
    document.getElementById('modalComplaintDescription').textContent = complaint.description;
    document.getElementById('modalComplainerName').textContent = complaint.complainerName;
    document.getElementById('modalComplainerEmail').textContent = complaint.complainerEmail;
    document.getElementById('modalDepartment').textContent = complaint.department;
    document.getElementById('modalType').textContent = complaint.type;
    document.getElementById('modalStatus').textContent = complaint.status;
    document.getElementById('modalPriority').textContent = complaint.priority || 'Normal';
    document.getElementById('modalCreatedAt').textContent = formatDate(complaint.createdAt);
    document.getElementById('modalUpdatedAt').textContent = formatDate(complaint.updatedAt);
    
    // Load comments if any
    loadComplaintComments(complaint.id);
}

/**
 * Load complaint comments
 */
async function loadComplaintComments(complaintId) {
    try {
        const response = await api.get(`/api/complaints/${complaintId}/comments`);
        const comments = response.data;
        
        const commentsList = document.getElementById('modalComments');
        if (commentsList) {
            commentsList.innerHTML = comments.map(comment => `
                <div class="comment-item">
                    <div class="comment-header">
                        <strong>${comment.authorName}</strong>
                        <small>${formatDate(comment.createdAt)}</small>
                    </div>
                    <div class="comment-content">${comment.content}</div>
                </div>
            `).join('');
        }
        
    } catch (error) {
        console.error('Error loading comments:', error);
    }
}

/**
 * Update complaint status
 */
async function updateStatus(complaintId) {
    const complaint = staffComplaints.find(c => c.id === complaintId);
    if (!complaint) return;
    
    selectedComplaint = complaint;
    
    // Populate status update modal
    const statusSelect = document.getElementById('statusUpdateSelect');
    const commentTextarea = document.getElementById('statusComment');
    
    if (statusSelect) statusSelect.value = complaint.status;
    if (commentTextarea) commentTextarea.value = '';
    
    // Show modal
    const modal = document.getElementById('statusUpdateModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

/**
 * Submit status update
 */
async function submitStatusUpdate() {
    const statusSelect = document.getElementById('statusUpdateSelect');
    const commentTextarea = document.getElementById('statusComment');
    
    if (!statusSelect || !selectedComplaint) return;
    
    const newStatus = statusSelect.value;
    const comment = commentTextarea ? commentTextarea.value : '';
    
    try {
        showLoading(true);
        
        await api.put(`/api/complaints/${selectedComplaint.id}/status`, {
            status: newStatus,
            comment: comment
        });
        
        showSuccess('Status updated successfully');
        
        // Close modal
        closeModal('statusUpdateModal');
        
        // Refresh complaints list
        await loadStaffComplaints(currentPage, currentFilter, searchTerm);
        
        // Update dashboard stats
        await loadDashboardStats();
        
    } catch (error) {
        console.error('Error updating status:', error);
        showError('Failed to update status');
    } finally {
        showLoading(false);
    }
}

/**
 * Add comment to complaint
 */
function addComment(complaintId) {
    const complaint = staffComplaints.find(c => c.id === complaintId);
    if (!complaint) return;
    
    selectedComplaint = complaint;
    
    // Clear comment textarea
    const commentTextarea = document.getElementById('commentTextarea');
    if (commentTextarea) commentTextarea.value = '';
    
    // Show modal
    const modal = document.getElementById('commentModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

/**
 * Submit comment
 */
async function submitComment() {
    const commentTextarea = document.getElementById('commentTextarea');
    
    if (!commentTextarea || !selectedComplaint) return;
    
    const content = commentTextarea.value.trim();
    if (!content) {
        showError('Please enter a comment');
        return;
    }
    
    try {
        showLoading(true);
        
        await api.post(`/api/complaints/${selectedComplaint.id}/comments`, {
            content: content
        });
        
        showSuccess('Comment added successfully');
        
        // Close modal
        closeModal('commentModal');
        
        // Refresh complaint details if modal is open
        const detailModal = document.getElementById('complaintDetailModal');
        if (detailModal && detailModal.style.display === 'block') {
            await loadComplaintComments(selectedComplaint.id);
        }
        
    } catch (error) {
        console.error('Error adding comment:', error);
        showError('Failed to add comment');
    } finally {
        showLoading(false);
    }
}

/**
 * Export complaints data
 */
function exportComplaints() {
    try {
        const csvContent = generateCSV(staffComplaints);
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        
        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            link.setAttribute('download', `complaints_${new Date().toISOString().split('T')[0]}.csv`);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
        
        showSuccess('Data exported successfully');
        
    } catch (error) {
        console.error('Error exporting data:', error);
        showError('Failed to export data');
    }
}

/**
 * Generate CSV content from complaints data
 */
function generateCSV(complaints) {
    const headers = ['ID', 'Title', 'Description', 'Complainer', 'Department', 'Type', 'Status', 'Created At'];
    const csvRows = [headers.join(',')];
    
    complaints.forEach(complaint => {
        const row = [
            complaint.id,
            `"${complaint.title}"`,
            `"${complaint.description}"`,
            complaint.complainerName,
            complaint.department,
            complaint.type,
            complaint.status,
            formatDate(complaint.createdAt)
        ];
        csvRows.push(row.join(','));
    });
    
    return csvRows.join('\n');
}

/**
 * Setup modal event handlers
 */
function setupModalHandlers() {
    // Close modal buttons
    document.addEventListener('click', function(event) {
        if (event.target.classList.contains('close') || event.target.classList.contains('modal-close')) {
            const modal = event.target.closest('.modal');
            if (modal) {
                modal.style.display = 'none';
            }
        }
    });
    
    // Submit buttons
    const statusUpdateBtn = document.getElementById('statusUpdateBtn');
    const commentSubmitBtn = document.getElementById('commentSubmitBtn');
    
    if (statusUpdateBtn) statusUpdateBtn.addEventListener('click', submitStatusUpdate);
    if (commentSubmitBtn) commentSubmitBtn.addEventListener('click', submitComment);
    
    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    });
}

/**
 * Close modal by ID
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Utility functions
 */
function showLoading(show) {
    const loading = document.getElementById('loading');
    if (loading) {
        loading.style.display = show ? 'block' : 'none';
    }
}

function showError(message) {
    showNotification(message, 'error');
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <span>${message}</span>
        <button class="notification-close">&times;</button>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 3000);
    
    // Manual close
    notification.querySelector('.notification-close').addEventListener('click', () => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    });
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substr(0, maxLength) + '...';
}

/**
 * Logout function
 */
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/frontend/pages/auth/login.html';
}

// Make functions globally available
window.viewComplaint = viewComplaint;
window.updateStatus = updateStatus;
window.addComment = addComment;
window.exportComplaints = exportComplaints;
window.logout = logout;
