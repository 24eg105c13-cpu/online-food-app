const API_BASE = '/api';

const Storage = {
    TOKEN_KEY: 'food_auth_token',
    USER_KEY: 'food_user_profile',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    setAuth(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    getUser() {
        const data = localStorage.getItem(this.USER_KEY);
        try {
            return data ? JSON.parse(data) : null;
        } catch (e) {
            return null;
        }
    },

    clearAuth() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    isAdmin() {
        const user = this.getUser();
        return user && user.role === 'ADMIN';
    }
};

async function apiRequest(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
    const token = Storage.getToken();

    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, {
            ...options,
            headers
        });

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            if (response.status === 401 && !endpoint.includes('/auth/login')) {
                Storage.clearAuth();
                showToast('Session expired. Please log in again.', 'error');
                setTimeout(() => {
                    if (window.location.pathname.includes('admin')) {
                        window.location.href = '/admin-login.html';
                    } else {
                        window.location.href = '/login.html';
                    }
                }, 1200);
            }
            const errorMsg = data && (data.message || data.error) ? (data.message || data.error) : `Error ${response.status}: Request failed`;
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        console.error('API Error on ' + endpoint, err);
        throw err;
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function(m) {
        return {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        }[m];
    });
}

function formatCurrency(amount) {
    if (amount === undefined || amount === null) return '$0.00';
    return '$' + Number(amount).toFixed(2);
}

function handleImageError(img) {
    img.onerror = null;
    img.src = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80';
}

// Header Auth and Cart Synchronization
async function syncHeader() {
    const user = Storage.getUser();
    const isAuth = Storage.isAuthenticated();

    const authActions = document.getElementById('header-auth-actions');
    if (authActions) {
        if (isAuth && user) {
            authActions.innerHTML = `
                <div style="display:flex; align-items:center; gap:12px;">
                    <a href="${user.role === 'ADMIN' ? '/admin-dashboard.html' : '/profile.html'}" class="btn btn-secondary btn-sm">
                        👤 ${escapeHtml(user.name.split(' ')[0])} (${user.role})
                    </a>
                    <button onclick="handleLogout()" class="btn btn-outline btn-sm">Logout</button>
                </div>
            `;
        } else {
            authActions.innerHTML = `
                <a href="/login.html" class="btn btn-secondary btn-sm">Log In</a>
                <a href="/register.html" class="btn btn-primary btn-sm">Sign Up</a>
            `;
        }
    }

    // Update cart badge
    const badge = document.getElementById('header-cart-badge');
    if (badge) {
        if (isAuth && !Storage.isAdmin()) {
            try {
                const res = await apiRequest('/cart');
                const totalQty = res?.data?.totalQuantity || 0;
                badge.textContent = totalQty;
                badge.style.display = totalQty > 0 ? 'flex' : 'none';
            } catch (e) {
                badge.textContent = '0';
                badge.style.display = 'none';
            }
        } else {
            badge.textContent = '0';
            badge.style.display = 'none';
        }
    }
}

function handleLogout() {
    Storage.clearAuth();
    showToast('You have been logged out.', 'info');
    setTimeout(() => {
        window.location.href = '/index.html';
    }, 800);
}

document.addEventListener('DOMContentLoaded', () => {
    syncHeader();
});
