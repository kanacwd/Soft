/**
 * Admin Dashboard JavaScript
 * Complete admin functionality for SCRS system
 */

// Global admin state
const AdminState = {
    currentUser: null,
    users: [],
    departments: [],
    complaints: [],
    currentPage: {
        users: 1,
        complaints: 1
    },
    pageSize: 10,
    filters: {
        users: { role: '', status: '', search: '' },
        complaints: { status: '', department: '', search: '' }
    }
};

// Initialize Admin Dashboard
async function initializeAdminDashboard() {
    try {
        showLoading();

        // Verify admin role
        if (!await verifyAdminRole()) {
            showError('Access denied. Admin privileges required.');
            return;
        }

        // Load current user info
        await loadCurrentUser();

        // Initialize navigation
        initializeNavigation();

        // Initialize modals
        initializeModals();

        // Initialize event listeners
        initializeEventListeners();

        // Load dashboard data
        await loadDashboardData();

        hideLoading();

    } catch (error) {
        console.error('Error initializing admin dashboard:', error);
        showError('Failed to initialize dashboard');
        hideLoading();
    }
}

// Verify admin role
async function verifyAdminRole() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return false;

        const response = await fetch('/api/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) return false;

        const userData = await response.json();
        return userData.role === 'ADMIN';
    } catch (error) {
        console.error('Error verifying admin role:', error);
        return false;
    }
}

// Load current user info
async function loadCurrentUser() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/auth/me', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            AdminState.currentUser = await response.json();
            const adminNameElement = document.getElementById('admin-name');
            if (adminNameElement) {
                adminNameElement.textContent = AdminState.currentUser.fullName;
            }
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

// Load dashboard data
async function loadDashboardData() {
    try {
        await Promise.all([
            loadDashboardStats(),
            loadRecentActivity(),
            loadDepartments()
        ]);
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        const token = localStorage.getItem('token');

        // Load users count
        const usersResponse = await fetch('/api/admin/users?size=1', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (usersResponse.ok) {
            const usersData = await usersResponse.json();
            const totalUsersElement = document.getElementById('totalUsers');
            if (totalUsersElement) {
                totalUsersElement.textContent = usersData.totalElements || 0;
            }
        }

        // Load departments count
        const deptsResponse = await fetch('/api/departments', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (deptsResponse.ok) {
            AdminState.departments = await deptsResponse.json();
            const totalDepartmentsElement = document.getElementById('totalDepartments');
            if (totalDepartmentsElement) {
                totalDepartmentsElement.textContent = deptsData.length || 0;
            }
        }

        // Load complaints stats
        const complaintsResponse = await fetch('/api/complaints/stats', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (complaintsResponse.ok) {
            const stats = await complaintsResponse.json();
            const totalComplaintsElement = document.getElementById('totalComplaints');
            const resolvedComplaintsElement = document.getElementById('resolvedComplaints');

            if (totalComplaintsElement) totalComplaintsElement.textContent = stats.total || 0;
            if (resolvedComplaintsElement) resolvedComplaintsElement.textContent = stats.resolved || 0;

            // Create status chart
            createStatusChart(stats);
        }

        // Load user trend data
        await loadUserTrends();

        // Load quick stats
        await loadQuickStats();

    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

// Create status chart
function createStatusChart(stats) {
    const ctx = document.getElementById('statusChart');
    if (!ctx || typeof Chart === 'undefined') return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Pending', 'In Progress', 'Resolved', 'Rejected'],
            datasets: [{
                data: [
                    stats.pending || 0,
                    stats.inProgress || 0,
                    stats.resolved || 0,
                    stats.rejected || 0
                ],
                backgroundColor: [
                    '#ffc107',
                    '#17a2b8',
                    '#28a745',
                    '#dc3545'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// Load user trends
async function loadUserTrends() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/admin/users/stats/trends', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const trends = await response.json();
            createUserTrendChart(trends);
        }
    } catch (error) {
        console.error('Error loading user trends:', error);
    }
}

// Create user trend chart
function createUserTrendChart(trends) {
    const ctx = document.getElementById('userTrendChart');
    if (!ctx || typeof Chart === 'undefined') return;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: trends.labels || [],
            datasets: [{
                label: 'New Users',
                data: trends.data || [],
                borderColor: '#007bff',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// Load recent activity
async function loadRecentActivity() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/admin/activity/recent', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const activities = await response.json();
            displayRecentActivity(activities);
        }
    } catch (error) {
        console.error('Error loading recent activity:', error);
    }
}

// Display recent activity
function displayRecentActivity(activities) {
    const container = document.getElementById('recentActivity');
    if (!container) return;

    container.innerHTML = '';

    if (!activities || activities.length === 0) {
        container.innerHTML = '<p class="text-muted">No recent activity</p>';
        return;
    }

    activities.forEach(activity => {
        const activityItem = document.createElement('div');
        activityItem.className = 'activity-item d-flex align-items-center mb-2 p-2 border-bottom';
        activityItem.innerHTML = `
            <div class="activity-icon me-3">
                <i class="fas ${getActivityIcon(activity.type)} text-primary"></i>
            </div>
            <div class="activity-content flex-grow-1">
                <p class="activity-description mb-0">${activity.description}</p>
                <small class="activity-time text-muted">${formatDate(activity.timestamp)}</small>
            </div>
        `;
        container.appendChild(activityItem);
    });
}

// Get activity icon
function getActivityIcon(type) {
    const icons = {
        'USER_REGISTERED': 'fa-user-plus',
        'COMPLAINT_CREATED': 'fa-file-plus',
        'COMPLAINT_RESOLVED': 'fa-check-circle',
        'USER_ACTIVATED': 'fa-user-check',
        'DEPARTMENT_CREATED': 'fa-building'
    };
    return icons[type] || 'fa-info-circle';
}

// Load quick stats
async function loadQuickStats() {
    try {
        const token = localStorage.getItem('token');

        // Average resolution time
        const resolutionResponse = await fetch('/api/admin/stats/avg-resolution-time', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (resolutionResponse.ok) {
            const data = await resolutionResponse.json();
            const element = document.getElementById('avgResolutionTime');
            if (element) element.textContent = data.averageDays ? `${data.averageDays} days` : 'N/A';
        }

        // Most active department
        const deptResponse = await fetch('/api/admin/stats/most-active-department', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (deptResponse.ok) {
            const data = await deptResponse.json();
            const element = document.getElementById('mostActiveDepartment');
            if (element) element.textContent = data.departmentName || 'N/A';
        }

        // Satisfaction rate
        const satisfactionResponse = await fetch('/api/admin/stats/satisfaction-rate', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (satisfactionResponse.ok) {
            const data = await satisfactionResponse.json();
            const element = document.getElementById('satisfactionRate');
            if (element) element.textContent = data.rate ? `${data.rate}%` : 'N/A';
        }

    } catch (error) {
        console.error('Error loading quick stats:', error);
    }
}

// Navigation
function initializeNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const section = item.dataset.section;
            showSection(section);

            // Update active nav item
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            // Load section data
            loadSectionData(section);
        });
    });
}

// Show section
function showSection(sectionName) {
    // Hide all sections
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => section.classList.remove('active'));

    // Show target section
    const targetSection = document.getElementById(`${sectionName}-section`);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

// Load section data
async function loadSectionData(sectionName) {
    switch (sectionName) {
        case 'users':
            await loadUsers();
            break;
        case 'departments':
            await loadDepartments();
            break;
        case 'complaints':
            await loadAllComplaints();
            break;
        case 'reports':
            // Reports section is static
            break;
        case 'settings':
            await loadSettings();
            break;
    }
}

// Load users
async function loadUsers() {
    try {
        showLoading();

        const token = localStorage.getItem('token');
        const params = new URLSearchParams({
            page: AdminState.currentPage.users - 1,
            size: AdminState.pageSize,
            role: AdminState.filters.users.role,
            status: AdminState.filters.users.status,
            search: AdminState.filters.users.search
        });

        const response = await fetch(`/api/admin/users?${params}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();
            AdminState.users = data.content;
            displayUsers(data.content);
            displayPagination('users', data);
        }

        hideLoading();
    } catch (error) {
        console.error('Error loading users:', error);
        showError('Failed to load users');
        hideLoading();
    }
}

// Display users
function displayUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">No users found</td></tr>';
        return;
    }

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.fullName}</td>
            <td>${user.email}</td>
            <td><span class="badge badge-${getRoleBadgeClass(user.role)}">${user.role}</span></td>
            <td>${user.department ? user.department.name : 'No Department'}</td>
            <td><span class="badge badge-${user.enabled ? 'success' : 'danger'}">
                ${user.enabled ? 'Active' : 'Inactive'}
            </span></td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="viewUser(${user.id})" title="View">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline" onclick="editUser(${user.id})" title="Edit">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-${user.enabled ? 'warning' : 'success'}" 
                        onclick="toggleUserStatus(${user.id}, ${!user.enabled})" title="${user.enabled ? 'Deactivate' : 'Activate'}">
                    <i class="fas fa-${user.enabled ? 'ban' : 'check'}"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Get role badge class
function getRoleBadgeClass(role) {
    const classes = {
        'STUDENT': 'primary',
        'STAFF': 'info',
        'ADMIN': 'danger'
    };
    return classes[role] || 'secondary';
}

// Load departments
async function loadDepartments() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/departments', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            AdminState.departments = await response.json();
            displayDepartments(AdminState.departments);
        }
    } catch (error) {
        console.error('Error loading departments:', error);
        showError('Failed to load departments');
    }
}

// Display departments
function displayDepartments(departments) {
    const tbody = document.getElementById('departmentsTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!departments || departments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No departments found</td></tr>';
        return;
    }

    departments.forEach(dept => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${dept.id}</td>
            <td>${dept.name}</td>
            <td>${dept.description || 'No description'}</td>
            <td><span class="badge badge-${dept.active ? 'success' : 'danger'}">
                ${dept.active ? 'Active' : 'Inactive'}
            </span></td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="viewDepartment(${dept.id})" title="View">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline" onclick="editDepartment(${dept.id})" title="Edit">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-${dept.active ? 'warning' : 'success'}" 
                        onclick="toggleDepartmentStatus(${dept.id}, ${!dept.active})" title="${dept.active ? 'Deactivate' : 'Activate'}">
                    <i class="fas fa-${dept.active ? 'ban' : 'check'}"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load all complaints
async function loadAllComplaints() {
    try {
        showLoading();

        const token = localStorage.getItem('token');
        const params = new URLSearchParams({
            page: AdminState.currentPage.complaints - 1,
            size: AdminState.pageSize,
            status: AdminState.filters.complaints.status,
            department: AdminState.filters.complaints.department,
            search: AdminState.filters.complaints.search
        });

        const response = await fetch(`/api/complaints?${params}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();
            AdminState.complaints = data.content;
            displayComplaints(data.content);
            displayPagination('complaints', data);
        }

        hideLoading();
    } catch (error) {
        console.error('Error loading complaints:', error);
        showError('Failed to load complaints');
        hideLoading();
    }
}

// Display complaints
function displayComplaints(complaints) {
    const tbody = document.getElementById('complaintsTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!complaints || complaints.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No complaints found</td></tr>';
        return;
    }

    complaints.forEach(complaint => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${complaint.id}</td>
            <td>${complaint.title}</td>
            <td>${complaint.student ? complaint.student.fullName : 'Unknown'}</td>
            <td>${complaint.department ? complaint.department.name : 'No Department'}</td>
            <td><span class="badge badge-${getStatusBadgeClass(complaint.status)}">${complaint.status}</span></td>
            <td>${formatDate(complaint.createdAt)}</td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="viewComplaint(${complaint.id})" title="View">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Get status badge class
function getStatusBadgeClass(status) {
    const classes = {
        'PENDING': 'warning',
        'IN_PROGRESS': 'info',
        'RESOLVED': 'success',
        'REJECTED': 'danger'
    };
    return classes[status] || 'secondary';
}

// Load settings
async function loadSettings() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/admin/settings', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const settings = await response.json();
            displaySettings(settings);
        }
    } catch (error) {
        console.error('Error loading settings:', error);
    }
}

// Display settings
function displaySettings(settings) {
    const form = document.getElementById('settingsForm');
    if (!form) return;

    // Populate form fields
    Object.keys(settings).forEach(key => {
        const input = form.querySelector(`[name="${key}"]`);
        if (input) {
            input.value = settings[key];
        }
    });
}

// User management functions
async function viewUser(userId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/users/${userId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const user = await response.json();
            showUserModal(user);
        }
    } catch (error) {
        console.error('Error viewing user:', error);
        showError('Failed to load user details');
    }
}

async function editUser(userId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/users/${userId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const user = await response.json();
            showEditUserModal(user);
        }
    } catch (error) {
        console.error('Error editing user:', error);
        showError('Failed to load user for editing');
    }
}

async function toggleUserStatus(userId, enabled) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/users/${userId}/status`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ enabled })
        });

        if (response.ok) {
            showSuccess(`User ${enabled ? 'activated' : 'deactivated'} successfully`);
            await loadUsers();
        } else {
            showError('Failed to update user status');
        }
    } catch (error) {
        console.error('Error toggling user status:', error);
        showError('Failed to update user status');
    }
}

// Department management functions
async function viewDepartment(deptId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/departments/${deptId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const department = await response.json();
            showDepartmentModal(department);
        }
    } catch (error) {
        console.error('Error viewing department:', error);
        showError('Failed to load department details');
    }
}

async function editDepartment(deptId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/departments/${deptId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const department = await response.json();
            showEditDepartmentModal(department);
        }
    } catch (error) {
        console.error('Error editing department:', error);
        showError('Failed to load department for editing');
    }
}

async function toggleDepartmentStatus(deptId, active) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/departments/${deptId}/status`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ active })
        });

        if (response.ok) {
            showSuccess(`Department ${active ? 'activated' : 'deactivated'} successfully`);
            await loadDepartments();
        } else {
            showError('Failed to update department status');
        }
    } catch (error) {
        console.error('Error toggling department status:', error);
        showError('Failed to update department status');
    }
}

// Complaint management functions
async function viewComplaint(complaintId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`/api/complaints/${complaintId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const complaint = await response.json();
            showComplaintModal(complaint);
        }
    } catch (error) {
        console.error('Error viewing complaint:', error);
        showError('Failed to load complaint details');
    }
}

// Modal functions
function showUserModal(user) {
    const modal = document.getElementById('userModal');
    if (!modal) return;

    modal.querySelector('.modal-body').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <p><strong>ID:</strong> ${user.id}</p>
                <p><strong>Username:</strong> ${user.username}</p>
                <p><strong>Full Name:</strong> ${user.fullName}</p>
                <p><strong>Email:</strong> ${user.email}</p>
            </div>
            <div class="col-md-6">
                <p><strong>Role:</strong> <span class="badge badge-${getRoleBadgeClass(user.role)}">${user.role}</span></p>
                <p><strong>Department:</strong> ${user.department ? user.department.name : 'No Department'}</p>
                <p><strong>Status:</strong> <span class="badge badge-${user.enabled ? 'success' : 'danger'}">${user.enabled ? 'Active' : 'Inactive'}</span></p>
                <p><strong>Created:</strong> ${formatDate(user.createdAt)}</p>
            </div>
        </div>
    `;

    new bootstrap.Modal(modal).show();
}

function showEditUserModal(user) {
    const modal = document.getElementById('editUserModal');
    if (!modal) return;

    // Populate form
    const userIdInput = modal.querySelector('[name="userId"]');
    const usernameInput = modal.querySelector('[name="username"]');
    const fullNameInput = modal.querySelector('[name="fullName"]');
    const emailInput = modal.querySelector('[name="email"]');
    const roleInput = modal.querySelector('[name="role"]');
    const departmentInput = modal.querySelector('[name="departmentId"]');

    if (userIdInput) userIdInput.value = user.id;
    if (usernameInput) usernameInput.value = user.username;
    if (fullNameInput) fullNameInput.value = user.fullName;
    if (emailInput) emailInput.value = user.email;
    if (roleInput) roleInput.value = user.role;
    if (departmentInput) departmentInput.value = user.department ? user.department.id : '';

    new bootstrap.Modal(modal).show();
}

function showDepartmentModal(department) {
    const modal = document.getElementById('departmentModal');
    if (!modal) return;

    modal.querySelector('.modal-body').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <p><strong>ID:</strong> ${department.id}</p>
                <p><strong>Name:</strong> ${department.name}</p>
            </div>
            <div class="col-md-6">
                <p><strong>Description:</strong> ${department.description || 'No description'}</p>
                <p><strong>Status:</strong> <span class="badge badge-${department.active ? 'success' : 'danger'}">${department.active ? 'Active' : 'Inactive'}</span></p>
            </div>
        </div>
    `;

    new bootstrap.Modal(modal).show();
}

function showEditDepartmentModal(department) {
    const modal = document.getElementById('editDepartmentModal');
    if (!modal) return;

    // Populate form
    const departmentIdInput = modal.querySelector('[name="departmentId"]');
    const nameInput = modal.querySelector('[name="name"]');
    const descriptionInput = modal.querySelector('[name="description"]');

    if (departmentIdInput) departmentIdInput.value = department.id;
    if (nameInput) nameInput.value = department.name;
    if (descriptionInput) descriptionInput.value = department.description || '';

    new bootstrap.Modal(modal).show();
}

function showComplaintModal(complaint) {
    const modal = document.getElementById('complaintModal');
    if (!modal) return;

    modal.querySelector('.modal-body').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <p><strong>ID:</strong> ${complaint.id}</p>
                <p><strong>Title:</strong> ${complaint.title}</p>
                <p><strong>Description:</strong> ${complaint.description}</p>
                <p><strong>Student:</strong> ${complaint.student ? complaint.student.fullName : 'Unknown'}</p>
            </div>
            <div class="col-md-6">
                <p><strong>Department:</strong> ${complaint.department ? complaint.department.name : 'No Department'}</p>
                <p><strong>Status:</strong> <span class="badge badge-${getStatusBadgeClass(complaint.status)}">${complaint.status}</span></p>
                <p><strong>Priority:</strong> ${complaint.priority}</p>
                <p><strong>Created:</strong> ${formatDate(complaint.createdAt)}</p>
            </div>
        </div>
    `;

    new bootstrap.Modal(modal).show();
}

// Initialize event listeners
function initializeEventListeners() {
    // Search filters
    const searchInputs = document.querySelectorAll('.search-input');
    searchInputs.forEach(input => {
        input.addEventListener('input', debounce(handleSearch, 300));
    });

    // Form submissions
    const editUserForm = document.getElementById('editUserForm');
    if (editUserForm) {
        editUserForm.addEventListener('submit', handleEditUser);
    }

    const editDepartmentForm = document.getElementById('editDepartmentForm');
    if (editDepartmentForm) {
        editDepartmentForm.addEventListener('submit', handleEditDepartment);
    }

    // Settings form
    const settingsForm = document.getElementById('settingsForm');
    if (settingsForm) {
        settingsForm.addEventListener('submit', handleSaveSettings);
    }
}

function handleSearch(e) {
    const searchType = e.target.dataset.searchType;
    const searchValue = e.target.value;

    if (searchType === 'users') {
        AdminState.filters.users.search = searchValue;
        AdminState.currentPage.users = 1;
        loadUsers();
    } else if (searchType === 'complaints') {
        AdminState.filters.complaints.search = searchValue;
        AdminState.currentPage.complaints = 1;
        loadAllComplaints();
    }
}

async function handleEditUser(e) {
    e.preventDefault();

    try {
        const formData = new FormData(e.target);
        const userData = Object.fromEntries(formData);

        const token = localStorage.getItem('token');
        const response = await fetch(`/api/admin/users/${userData.userId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            showSuccess('User updated successfully');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
            if (modal) modal.hide();
            await loadUsers();
        } else {
            showError('Failed to update user');
        }
    } catch (error) {
        console.error('Error updating user:', error);
        showError('Failed to update user');
    }
}

async function handleEditDepartment(e) {
    e.preventDefault();

    try {
        const formData = new FormData(e.target);
        const deptData = Object.fromEntries(formData);

        const token = localStorage.getItem('token');
        const response = await fetch(`/api/departments/${deptData.departmentId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(deptData)
        });

        if (response.ok) {
            showSuccess('Department updated successfully');
            const modal = bootstrap.Modal.getInstance(document.getElementById('editDepartmentModal'));
            if (modal) modal.hide();
            await loadDepartments();
        } else {
            showError('Failed to update department');
        }
    } catch (error) {
        console.error('Error updating department:', error);
        showError('Failed to update department');
    }
}

async function handleSaveSettings(e) {
    e.preventDefault();

    try {
        const formData = new FormData(e.target);
        const settingsData = Object.fromEntries(formData);

        const token = localStorage.getItem('token');
        const response = await fetch('/api/admin/settings', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(settingsData)
        });

        if (response.ok) {
            showSuccess('Settings saved successfully');
        } else {
            showError('Failed to save settings');
        }
    } catch (error) {
        console.error('Error saving settings:', error);
        showError('Failed to save settings');
    }
}

// Initialize modals
function initializeModals() {
    // Add event listeners to close modals
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('hidden.bs.modal', () => {
            const form = modal.querySelector('form');
            if (form) form.reset();
        });
    });
}

// Display pagination
function displayPagination(type, data) {
    const container = document.getElementById(`${type}Pagination`);
    if (!container) return;

    const currentPage = data.number + 1;
    const totalPages = data.totalPages;

    let paginationHTML = '';

    // Previous button
    paginationHTML += `
        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage('${type}', ${currentPage - 1})">Previous</a>
        </li>
    `;

    // Page numbers
    for (let i = Math.max(1, currentPage - 2); i <= Math.min(totalPages, currentPage + 2); i++) {
        paginationHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" onclick="changePage('${type}', ${i})">${i}</a>
            </li>
        `;
    }

    // Next button
    paginationHTML += `
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage('${type}', ${currentPage + 1})">Next</a>
        </li>
    `;

    container.innerHTML = `<ul class="pagination">${paginationHTML}</ul>`;
}

// Change page
function changePage(type, page) {
    if (type === 'users') {
        AdminState.currentPage.users = page;
        loadUsers();
    } else if (type === 'complaints') {
        AdminState.currentPage.complaints = page;
        loadAllComplaints();
    }
}

// Utility functions
function showLoading() {
    const loading = document.getElementById('loading');
    if (loading) loading.style.display = 'block';
}

function hideLoading() {
    const loading = document.getElementById('loading');
    if (loading) loading.style.display = 'none';
}

function showError(message) {
    showNotification(message, 'error');
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'error' ? 'danger' : 'success'} alert-dismissible fade show position-fixed`;
    notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Debounce function for search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Logout function
function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', initializeAdminDashboard);
