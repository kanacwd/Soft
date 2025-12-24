// SCRS Complaint Management JavaScript
// Handles all complaint-related functionality for students

const complaint = {
    // Configuration
    apiBase: '/api/complaints',
    voteApiBase: '/api/votes',
    commentApiBase: '/api/comments',
    
    // State management
    currentPage: 0,
    pageSize: 10,
    totalPages: 0,
    totalElements: 0,
    currentFilters: {},
    currentSort: 'createdAt,desc',
    
    // Current context
    currentView: 'dashboard', // 'dashboard', 'my-complaints', 'submit', 'public'
    currentComplaintId: null,
    
    /**
     * Initialize complaint management system
     */
    init() {
        this.setupEventListeners();
        this.loadDepartments();
        
        // Auto-detect current view from URL or page
        this.detectCurrentView();
        
        console.log('Complaint system initialized');
    },
    
    /**
     * Setup event listeners for complaint functionality
     */
    setupEventListeners() {
        // Form submissions
        document.addEventListener('submit', (e) => {
            if (e.target.id === 'complaintForm') {
                e.preventDefault();
                this.submitComplaint(e.target);
            }
        });
        
        // Filter changes
        document.addEventListener('change', (e) => {
            if (e.target.matches('.filter-control')) {
                this.applyFilters();
            }
        });
        
        // Search functionality
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(() => {
                this.applyFilters();
            }, 300));
        }
        
        // Modal events
        document.addEventListener('click', (e) => {
            if (e.target.matches('.vote-btn')) {
                const complaintId = e.target.closest('[data-complaint-id]').dataset.complaintId;
                this.showVoteModal(complaintId);
            }
            
            if (e.target.matches('.comment-btn')) {
                const complaintId = e.target.closest('[data-complaint-id]').dataset.complaintId;
                this.showCommentModal(complaintId);
            }
            
            if (e.target.matches('.edit-btn')) {
                const complaintId = e.target.closest('[data-complaint-id]').dataset.complaintId;
                this.editComplaint(complaintId);
            }
            
            if (e.target.matches('.delete-btn')) {
                const complaintId = e.target.closest('[data-complaint-id]').dataset.complaintId;
                this.deleteComplaint(complaintId);
            }
        });
    },
    
    /**
     * Detect current view from URL hash or page content
     */
    detectCurrentView() {
        const hash = window.location.hash.substring(1);
        
        if (hash.includes('submit-complaint')) {
            this.currentView = 'submit';
        } else if (hash.includes('my-complaints')) {
            this.currentView = 'my-complaints';
        } else if (hash.includes('public-complaints')) {
            this.currentView = 'public';
        } else {
            this.currentView = 'dashboard';
        }
        
        // Load data based on current view
        switch(this.currentView) {
            case 'dashboard':
                this.loadDashboardData();
                break;
            case 'my-complaints':
                this.loadMyComplaints();
                break;
            case 'submit':
                this.initializeSubmitForm();
                break;
            case 'public':
                this.loadPublicComplaints();
                break;
        }
    },
    
    /**
     * Load dashboard statistics and recent complaints
     */
    async loadDashboardData() {
        try {
            this.showLoading('dashboardContent');
            
            // Load user complaints for statistics
            const response = await api.get(`${this.apiBase}/my-complaints?page=0&size=100`);
            const complaints = response.data.content || response.data || [];
            
            // Update dashboard statistics
            this.updateDashboardStats(complaints);
            
            // Load recent complaints for dashboard
            const recentComplaints = complaints.slice(0, 5);
            this.renderRecentComplaints(recentComplaints);
            
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showError('dashboardContent', 'Failed to load dashboard data');
        } finally {
            this.hideLoading('dashboardContent');
        }
    },
    
    /**
     * Load user's complaints
     */
    async loadMyComplaints() {
        try {
            this.showLoading('myComplaintsList');
            
            const params = new URLSearchParams({
                page: this.currentPage,
                size: this.pageSize,
                ...this.currentFilters
            });
            
            // Add sorting
            params.append('sort', this.currentSort);
            
            const response = await api.get(`${this.apiBase}/my-complaints?${params}`);
            const data = response.data;
            
            this.totalPages = data.totalPages;
            this.totalElements = data.totalElements;
            
            this.renderComplaintsList(data.content, 'myComplaintsList');
            this.updatePagination('myComplaintsPagination');
            
        } catch (error) {
            console.error('Error loading my complaints:', error);
            this.showError('myComplaintsList', 'Failed to load your complaints');
        } finally {
            this.hideLoading('myComplaintsList');
        }
    },
    
    /**
     * Load public complaints with voting
     */
    async loadPublicComplaints() {
        try {
            this.showLoading('complaintsList');
            
            const params = new URLSearchParams({
                page: this.currentPage,
                size: this.pageSize,
                ...this.currentFilters
            });
            
            // Add sorting
            params.append('sort', this.currentSort);
            
            const response = await api.get(`${this.apiBase}/public?${params}`);
            const data = response.data;
            
            this.totalPages = data.totalPages;
            this.totalElements = data.totalElements;
            
            this.renderPublicComplaints(data.content);
            this.updatePagination('paginationContainer');
            this.updatePublicStats();
            
        } catch (error) {
            console.error('Error loading public complaints:', error);
            this.showError('complaintsList', 'Failed to load public complaints');
        } finally {
            this.hideLoading('complaintsList');
        }
    },
    
    /**
     * Submit a new complaint
     */
    async submitComplaint(form) {
        try {
            this.showSubmitLoading();
            
            const formData = new FormData(form);
            const complaintData = {
                title: formData.get('title'),
                description: formData.get('description'),
                type: formData.get('type'),
                departmentId: parseInt(formData.get('departmentId')),
                isPublic: formData.get('isPublic') === 'true',
                priority: formData.get('priority') || 'MEDIUM'
            };
            
            // Validate required fields
            if (!complaintData.title || !complaintData.description || !complaintData.type) {
                throw new Error('Please fill in all required fields');
            }
            
            const response = await api.post(this.apiBase, complaintData);
            
            if (response.success) {
                this.showSuccess('Complaint submitted successfully!');
                form.reset();
                
                // Redirect to my complaints after successful submission
                setTimeout(() => {
                    router.navigate('#my-complaints');
                }, 1500);
            } else {
                throw new Error(response.message || 'Failed to submit complaint');
            }
            
        } catch (error) {
            console.error('Error submitting complaint:', error);
            this.showError('submitError', error.message || 'Failed to submit complaint');
        } finally {
            this.hideSubmitLoading();
        }
    },
    
    /**
     * Show vote modal for a complaint
     */
    showVoteModal(complaintId) {
        this.currentComplaintId = complaintId;
        
        // Load current vote counts
        this.loadVoteCounts(complaintId).then(() => {
            const modal = document.getElementById('voteModal');
            if (modal) {
                modal.style.display = 'flex';
            }
        });
    },
    
    /**
     * Submit a vote on a complaint
     */
    async submitVote(voteType) {
        try {
            const voteData = {
                complaintId: parseInt(this.currentComplaintId),
                voteType: voteType
            };
            
            const response = await api.post(this.voteApiBase, voteData);
            
            if (response.success) {
                this.showSuccess('Vote submitted successfully!');
                this.closeModal('voteModal');
                
                // Refresh the current view to show updated vote counts
                this.refreshCurrentView();
            }
            
        } catch (error) {
            console.error('Error submitting vote:', error);
            this.showError('voteError', error.message || 'Failed to submit vote');
        }
    },
    
    /**
     * Show comment modal for a complaint
     */
    showCommentModal(complaintId) {
        this.currentComplaintId = complaintId;
        const modal = document.getElementById('commentModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    },
    
    /**
     * Submit a comment on a complaint
     */
    async submitComment() {
        try {
            const commentText = document.getElementById('commentText').value.trim();
            
            if (!commentText) {
                throw new Error('Please enter a comment');
            }
            
            const commentData = {
                complaintId: parseInt(this.currentComplaintId),
                comment: commentText
            };
            
            const response = await api.post(this.commentApiBase, commentData);
            
            if (response.success) {
                this.showSuccess('Comment added successfully!');
                document.getElementById('commentForm').reset();
                this.closeModal('commentModal');
                
                // Refresh to show new comment
                this.refreshCurrentView();
            }
            
        } catch (error) {
            console.error('Error submitting comment:', error);
            this.showError('commentError', error.message || 'Failed to submit comment');
        }
    },
    
    /**
     * Load vote counts for a complaint
     */
    async loadVoteCounts(complaintId) {
        try {
            const response = await api.get(`${this.voteApiBase}/complaint/${complaintId}`);
            const votes = response.data;
            
            const upvoteCount = document.getElementById('upvoteCount');
            const downvoteCount = document.getElementById('downvoteCount');
            
            if (upvoteCount && downvoteCount) {
                upvoteCount.textContent = votes.upvotes || 0;
                downvoteCount.textContent = votes.downvotes || 0;
            }
            
        } catch (error) {
            console.error('Error loading vote counts:', error);
        }
    },
    
    /**
     * Apply filters to complaint list
     */
    applyFilters() {
        // Update current filters
        this.currentFilters = {};
        
        // Get filter values from DOM
        const statusFilter = document.getElementById('statusFilter');
        const typeFilter = document.getElementById('typeFilter');
        const departmentFilter = document.getElementById('departmentFilter');
        const searchInput = document.getElementById('searchInput');
        const sortBy = document.getElementById('sortBy');
        
        if (statusFilter && statusFilter.value) {
            this.currentFilters.status = statusFilter.value;
        }
        
        if (typeFilter && typeFilter.value) {
            this.currentFilters.type = typeFilter.value;
        }
        
        if (departmentFilter && departmentFilter.value) {
            this.currentFilters.departmentId = departmentFilter.value;
        }
        
        if (searchInput && searchInput.value) {
            this.currentFilters.search = searchInput.value;
        }
        
        if (sortBy && sortBy.value) {
            this.currentSort = sortBy.value;
        }
        
        // Reset to first page and reload
        this.currentPage = 0;
        this.refreshCurrentView();
    },
    
    /**
     * Change page in pagination
     */
    changePage(pageChange) {
        const newPage = this.currentPage + pageChange;
        
        if (newPage >= 0 && newPage < this.totalPages) {
            this.currentPage = newPage;
            this.refreshCurrentView();
        }
    },
    
    /**
     * Go to specific page
     */
    goToPage(page) {
        if (page >= 0 && page < this.totalPages) {
            this.currentPage = page;
            this.refreshCurrentView();
        }
    },
    
    /**
     * Refresh current view
     */
    refreshCurrentView() {
        switch(this.currentView) {
            case 'my-complaints':
                this.loadMyComplaints();
                break;
            case 'public':
                this.loadPublicComplaints();
                break;
            case 'dashboard':
                this.loadDashboardData();
                break;
        }
    },
    
    /**
     * Load departments for dropdowns
     */
    async loadDepartments() {
        try {
            const response = await api.get('/api/departments');
            const departments = response.data;
            
            // Populate department dropdowns
            const departmentFilters = document.querySelectorAll('#departmentFilter');
            departmentFilters.forEach(filter => {
                if (filter.children.length <= 1) { // Only add if not already populated
                    departments.forEach(dept => {
                        const option = document.createElement('option');
                        option.value = dept.id;
                        option.textContent = dept.name;
                        filter.appendChild(option);
                    });
                }
            });
            
            // Populate submit form department dropdown
            const submitDepartment = document.getElementById('departmentId');
            if (submitDepartment && submitDepartment.children.length <= 1) {
                departments.forEach(dept => {
                    const option = document.createElement('option');
                    option.value = dept.id;
                    option.textContent = dept.name;
                    submitDepartment.appendChild(option);
                });
            }
            
        } catch (error) {
            console.error('Error loading departments:', error);
        }
    },
    
    /**
     * Initialize submit complaint form
     */
    initializeSubmitForm() {
        const form = document.getElementById('complaintForm');
        if (form) {
            // Set default values
            const isPublicField = form.querySelector('input[name="isPublic"]');
            if (isPublicField) {
                isPublicField.checked = true;
            }
            
            const priorityField = form.querySelector('select[name="priority"]');
            if (priorityField) {
                priorityField.value = 'MEDIUM';
            }
        }
    },
    
    /**
     * Render complaints list for "My Complaints" view
     */
    renderComplaintsList(complaints, containerId) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        if (complaints.length === 0) {
            container.innerHTML = this.getEmptyState('No complaints found', 'You haven\'t submitted any complaints yet.');
            return;
        }
        
        const complaintsHTML = complaints.map(complaint => `
            <div class="complaint-card" data-complaint-id="${complaint.id}">
                <div class="complaint-header">
                    <div class="complaint-meta">
                        <span class="complaint-id">#${complaint.id}</span>
                        <span class="complaint-status status-${complaint.status.toLowerCase()}">${this.formatStatus(complaint.status)}</span>
                    </div>
                    <div class="complaint-date">
                        ${this.formatDate(complaint.createdAt)}
                    </div>
                </div>
                
                <div class="complaint-content">
                    <h3 class="complaint-title">${complaint.title}</h3>
                    <p class="complaint-description">${complaint.description}</p>
                    
                    <div class="complaint-details">
                        <span class="complaint-type">${this.formatType(complaint.type)}</span>
                        <span class="complaint-department">${complaint.department?.name || 'N/A'}</span>
                        <span class="complaint-priority priority-${complaint.priority.toLowerCase()}">${complaint.priority}</span>
                        ${complaint.isPublic ? '<span class="public-badge">Public</span>' : '<span class="private-badge">Private</span>'}
                    </div>
                </div>
                
                <div class="complaint-actions">
                    <button class="btn btn-outline btn-sm" onclick="complaint.viewComplaint(${complaint.id})">
                        <i class="icon-eye"></i>
                        View Details
                    </button>
                    ${complaint.status === 'SUBMITTED' ? `
                        <button class="btn btn-secondary btn-sm edit-btn">
                            <i class="icon-edit"></i>
                            Edit
                        </button>
                        <button class="btn btn-danger btn-sm delete-btn">
                            <i class="icon-trash"></i>
                            Delete
                        </button>
                    ` : ''}
                </div>
            </div>
        `).join('');
        
        container.innerHTML = complaintsHTML;
    },
    
    /**
     * Render public complaints with voting
     */
    renderPublicComplaints(complaints) {
        const container = document.getElementById('complaintsList');
        if (!container) return;
        
        if (complaints.length === 0) {
            container.innerHTML = this.getEmptyState('No public complaints found', 'There are no public complaints that match your current filters.');
            return;
        }
        
        const complaintsHTML = complaints.map(complaint => `
            <div class="complaint-card public-complaint" data-complaint-id="${complaint.id}">
                <div class="complaint-header">
                    <div class="complaint-meta">
                        <span class="complaint-id">#${complaint.id}</span>
                        <span class="complaint-status status-${complaint.status.toLowerCase()}">${this.formatStatus(complaint.status)}</span>
                    </div>
                    <div class="complaint-date">
                        ${this.formatDate(complaint.createdAt)}
                    </div>
                </div>
                
                <div class="complaint-content">
                    <h3 class="complaint-title">${complaint.title}</h3>
                    <p class="complaint-description">${complaint.description}</p>
                    
                    <div class="complaint-details">
                        <span class="complaint-type">${this.formatType(complaint.type)}</span>
                        <span class="complaint-department">${complaint.department?.name || 'N/A'}</span>
                        <span class="complaint-priority priority-${complaint.priority.toLowerCase()}">${complaint.priority}</span>
                        <span class="vote-count">
                            <i class="icon-thumbs-up"></i>
                            ${complaint.upvoteCount || 0}
                            <i class="icon-thumbs-down"></i>
                            ${complaint.downvoteCount || 0}
                        </span>
                    </div>
                </div>
                
                <div class="complaint-actions">
                    <button class="btn btn-primary btn-sm vote-btn">
                        <i class="icon-thumbs-up"></i>
                        Vote
                    </button>
                    <button class="btn btn-outline btn-sm comment-btn">
                        <i class="icon-message"></i>
                        Comment
                    </button>
                    <button class="btn btn-outline btn-sm" onclick="complaint.viewComplaint(${complaint.id})">
                        <i class="icon-eye"></i>
                        Details
                    </button>
                </div>
            </div>
        `).join('');
        
        container.innerHTML = complaintsHTML;
    },
    
    /**
     * Render recent complaints for dashboard
     */
    renderRecentComplaints(complaints) {
        const container = document.getElementById('recentComplaints');
        if (!container) return;
        
        if (complaints.length === 0) {
            container.innerHTML = this.getEmptyState('No recent complaints', 'You haven\'t submitted any complaints yet.');
            return;
        }
        
        const complaintsHTML = complaints.map(complaint => `
            <div class="recent-complaint-item" onclick="complaint.viewComplaint(${complaint.id})">
                <div class="complaint-info">
                    <h4>${complaint.title}</h4>
                    <p>${this.truncateText(complaint.description, 100)}</p>
                </div>
                <div class="complaint-status status-${complaint.status.toLowerCase()}">
                    ${this.formatStatus(complaint.status)}
                </div>
            </div>
        `).join('');
        
        container.innerHTML = complaintsHTML;
    },
    
    /**
     * View complaint details
     */
    async viewComplaint(complaintId) {
        try {
            const response = await api.get(`${this.apiBase}/${complaintId}`);
            const complaint = response.data;
            
            this.renderComplaintDetail(complaint);
            this.showModal('complaintModal');
            
        } catch (error) {
            console.error('Error loading complaint details:', error);
            this.showError('detailError', 'Failed to load complaint details');
        }
    },
    
    /**
     * Render complaint detail modal
     */
    renderComplaintDetail(complaint) {
        const modal = document.getElementById('complaintDetail');
        if (!modal) return;
        
        const detailHTML = `
            <div class="complaint-detail">
                <div class="detail-header">
                    <h3>${complaint.title}</h3>
                    <div class="detail-meta">
                        <span class="detail-id">#${complaint.id}</span>
                        <span class="detail-status status-${complaint.status.toLowerCase()}">${this.formatStatus(complaint.status)}</span>
                        <span class="detail-date">${this.formatDate(complaint.createdAt)}</span>
                    </div>
                </div>
                
                <div class="detail-content">
                    <div class="detail-section">
                        <h4>Description</h4>
                        <p>${complaint.description}</p>
                    </div>
                    
                    <div class="detail-section">
                        <h4>Details</h4>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>Type:</label>
                                <span>${this.formatType(complaint.type)}</span>
                            </div>
                            <div class="detail-item">
                                <label>Department:</label>
                                <span>${complaint.department?.name || 'N/A'}</span>
                            </div>
                            <div class="detail-item">
                                <label>Priority:</label>
                                <span class="priority-${complaint.priority.toLowerCase()}">${complaint.priority}</span>
                            </div>
                            <div class="detail-item">
                                <label>Visibility:</label>
                                <span>${complaint.isPublic ? 'Public' : 'Private'}</span>
                            </div>
                        </div>
                    </div>
                    
                    ${complaint.statusHistory && complaint.statusHistory.length > 0 ? `
                        <div class="detail-section">
                            <h4>Status History</h4>
                            <div class="status-history">
                                ${complaint.statusHistory.map(history => `
                                    <div class="history-item">
                                        <span class="history-status">${this.formatStatus(history.status)}</span>
                                        <span class="history-date">${this.formatDate(history.changedAt)}</span>
                                        ${history.comment ? `<p class="history-comment">${history.comment}</p>` : ''}
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}
                    
                    ${complaint.comments && complaint.comments.length > 0 ? `
                        <div class="detail-section">
                            <h4>Comments (${complaint.comments.length})</h4>
                            <div class="comments-list">
                                ${complaint.comments.map(comment => `
                                    <div class="comment-item">
                                        <div class="comment-header">
                                            <span class="comment-author">${comment.user?.name || 'Anonymous'}</span>
                                            <span class="comment-date">${this.formatDate(comment.createdAt)}</span>
                                        </div>
                                        <p class="comment-text">${comment.comment}</p>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
        
        modal.innerHTML = detailHTML;
    },
    
    /**
     * Update dashboard statistics
     */
    updateDashboardStats(complaints) {
        const stats = {
            total: complaints.length,
            pending: complaints.filter(c => ['SUBMITTED', 'IN_REVIEW', 'ASSIGNED', 'IN_PROGRESS'].includes(c.status)).length,
            resolved: complaints.filter(c => c.status === 'RESOLVED').length,
            closed: complaints.filter(c => c.status === 'CLOSED').length
        };
        
        // Update DOM elements if they exist
        const totalElement = document.getElementById('totalComplaints');
        const pendingElement = document.getElementById('pendingComplaints');
        const resolvedElement = document.getElementById('resolvedComplaints');
        
        if (totalElement) totalElement.textContent = stats.total;
        if (pendingElement) pendingElement.textContent = stats.pending;
        if (resolvedElement) resolvedElement.textContent = stats.resolved;
    },
    
    /**
     * Update public complaints statistics
     */
    updatePublicStats() {
        // This would typically make another API call to get public complaint stats
        // For now, we'll use the loaded data
        const stats = {
            total: this.totalElements,
            pending: document.querySelectorAll('.status-submitted, .status-in_review, .status-assigned, .status-in_progress').length,
            recent: document.querySelectorAll('.complaint-card').length,
            topVoted: document.querySelectorAll('.vote-count').length
        };
        
        const totalElement = document.getElementById('totalPublicComplaints');
        const pendingElement = document.getElementById('pendingPublicComplaints');
        const recentElement = document.getElementById('recentComplaintsCount');
        const topVotedElement = document.getElementById('topVotedCount');
        
        if (totalElement) totalElement.textContent = stats.total;
        if (pendingElement) pendingElement.textContent = stats.pending;
        if (recentElement) recentElement.textContent = stats.recent;
        if (topVotedElement) topVotedElement.textContent = stats.topVoted;
    },
    
    /**
     * Update pagination controls
     */
    updatePagination(containerId) {
        const container = document.getElementById(containerId);
        if (!container || this.totalPages <= 1) {
            if (container) container.style.display = 'none';
            return;
        }
        
        container.style.display = 'block';
        
        // Update pagination info
        const info = document.getElementById('paginationInfo');
        if (info) {
            const start = this.currentPage * this.pageSize + 1;
            const end = Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
            info.textContent = `Showing ${start}-${end} of ${this.totalElements} complaints`;
        }
        
        // Update page numbers
        const pageNumbers = document.getElementById('pageNumbers');
        if (pageNumbers) {
            const pages = [];
            const maxVisiblePages = 5;
            let startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
            let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
            
            if (endPage - startPage < maxVisiblePages - 1) {
                startPage = Math.max(0, endPage - maxVisiblePages + 1);
            }
            
            for (let i = startPage; i <= endPage; i++) {
                pages.push(`
                    <button class="page-number ${i === this.currentPage ? 'active' : ''}" 
                            onclick="complaint.goToPage(${i})">${i + 1}</button>
                `);
            }
            
            pageNumbers.innerHTML = pages.join('');
        }
        
        // Update navigation buttons
        const prevBtn = document.getElementById('prevPageBtn');
        const nextBtn = document.getElementById('nextPageBtn');
        
        if (prevBtn) prevBtn.disabled = this.currentPage === 0;
        if (nextBtn) nextBtn.disabled = this.currentPage === this.totalPages - 1;
    },
    
    /**
     * Utility: Show loading state
     */
    showLoading(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="loading-state">
                    <div class="loading-spinner"></div>
                    <p>Loading...</p>
                </div>
            `;
        }
    },
    
    /**
     * Utility: Hide loading state
     */
    hideLoading(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            const loading = container.querySelector('.loading-state');
            if (loading) {
                loading.remove();
            }
        }
    },
    
    /**
     * Utility: Show submit loading
     */
    showSubmitLoading() {
        const submitBtn = document.querySelector('#complaintForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<div class="loading-spinner"></div> Submitting...';
        }
    },
    
    /**
     * Utility: Hide submit loading
     */
    hideSubmitLoading() {
        const submitBtn = document.querySelector('#complaintForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Submit Complaint';
        }
    },
    
    /**
     * Utility: Show error message
     */
    showError(containerId, message) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="error-message">
                    <i class="icon-alert"></i>
                    <span>${message}</span>
                </div>
            `;
        }
        
        // Also show toast notification
        this.showToast(message, 'error');
    },
    
    /**
     * Utility: Show success message
     */
    showSuccess(message) {
        this.showToast(message, 'success');
    },
    
    /**
     * Utility: Show toast notification
     */
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <i class="icon-${type === 'success' ? 'check' : type === 'error' ? 'alert' : 'info'}"></i>
                <span>${message}</span>
            </div>
        `;
        
        document.body.appendChild(toast);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            toast.remove();
        }, 3000);
    },
    
    /**
     * Utility: Show modal
     */
    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'flex';
        }
    },
    
    /**
     * Utility: Close modal
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
        }
    },
    
    /**
     * Utility: Get empty state HTML
     */
    getEmptyState(title, description) {
        return `
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="icon-inbox"></i>
                </div>
                <h3>${title}</h3>
                <p>${description}</p>
            </div>
        `;
    },
    
    /**
     * Utility: Format date
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },
    
    /**
     * Utility: Format status
     */
    formatStatus(status) {
        return status.replace(/_/g, ' ').toLowerCase()
                    .split(' ')
                    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                    .join(' ');
    },
    
    /**
     * Utility: Format type
     */
    formatType(type) {
        return type.replace(/_/g, ' ').toLowerCase()
                  .split(' ')
                  .map(word => word.charAt(0).toUpperCase() + word.slice(1))
                  .join(' ');
    },
    
    /**
     * Utility: Truncate text
     */
    truncateText(text, maxLength) {
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    },
    
    /**
     * Utility: Debounce function
     */
    debounce(func, wait) {
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
};

// Additional view-specific functions for public complaints
const publicComplaints = {
    init() {
        complaint.currentView = 'public';
        complaint.loadPublicComplaints();
    }
};

// Export for use in HTML
window.complaint = complaint;
window.publicComplaints = publicComplaints;
