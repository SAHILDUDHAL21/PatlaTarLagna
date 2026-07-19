import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to inject JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor to handle expired tokens and automatic refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          // Attempt token refresh
          const res = await axios.post(`${API_BASE_URL}/auth/refresh-token`, {
            refreshToken: refreshToken,
          });
          
          if (res.data && res.data.data) {
            const { token, refreshToken: newRefreshToken } = res.data.data;
            localStorage.setItem('token', token);
            localStorage.setItem('refreshToken', newRefreshToken);
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return api(originalRequest); // Retry original request
          }
        } catch (refreshError) {
          // If refresh token is expired, log out
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    if (response.data && response.data.data) {
      const { token, refreshToken, email: userEmail, roles } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('user', JSON.stringify({ email: userEmail, roles }));
    }
    return response.data;
  },
  register: async (registerData) => {
    const response = await api.post('/auth/register', registerData);
    return response.data;
  },
  verifyEmail: async (email, otp) => {
    const response = await api.post(`/auth/verify-email?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`);
    return response.data;
  },
  forgotPassword: async (email) => {
    const response = await api.post('/auth/forgot-password', { email });
    return response.data;
  },
  resetPassword: async (email, otp, newPassword) => {
    const response = await api.post('/auth/reset-password', { email, otp, newPassword });
    return response.data;
  },
  changePassword: async (oldPassword, newPassword) => {
    const response = await api.post('/auth/change-password', { oldPassword, newPassword });
    return response.data;
  },
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  isAdmin: () => {
    const user = authService.getCurrentUser();
    return user && user.roles && user.roles.includes('ADMIN');
  }
};

export const profileService = {
  getMyProfile: async () => {
    const response = await api.get('/profiles/my');
    return response.data;
  },
  createProfile: async (profileData) => {
    const response = await api.post('/profiles/my', profileData);
    return response.data;
  },
  updateProfile: async (profileData) => {
    const response = await api.put('/profiles/my', profileData);
    return response.data;
  },
  getUserProfile: async (userId) => {
    const response = await api.get(`/profiles/user/${userId}`);
    return response.data;
  },
  uploadPhoto: async (file, isMain = false) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('isMain', isMain);
    const response = await api.post('/profiles/photos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  deletePhoto: async (photoId) => {
    const response = await api.delete(`/profiles/photos/${photoId}`);
    return response.data;
  },
  getVisitors: async () => {
    const response = await api.get('/profiles/visitors');
    return response.data;
  },
};

export const preferenceService = {
  getMyPreference: async () => {
    const response = await api.get('/preferences/my');
    return response.data;
  },
  updateMyPreference: async (prefData) => {
    const response = await api.put('/preferences/my', prefData);
    return response.data;
  },
};

export const matchingService = {
  sendInterest: async (receiverId) => {
    const response = await api.post(`/matches/interests/send/${receiverId}`);
    return response.data;
  },
  acceptInterest: async (interestId) => {
    const response = await api.post(`/matches/interests/accept/${interestId}`);
    return response.data;
  },
  rejectInterest: async (interestId) => {
    const response = await api.post(`/matches/interests/reject/${interestId}`);
    return response.data;
  },
  getSentInterests: async () => {
    const response = await api.get('/matches/interests/sent');
    return response.data;
  },
  getReceivedInterests: async () => {
    const response = await api.get('/matches/interests/received');
    return response.data;
  },
  getMutualMatches: async () => {
    const response = await api.get('/matches/mutual');
    return response.data;
  },
  blockUser: async (targetUserId) => {
    const response = await api.post(`/matches/block/${targetUserId}`);
    return response.data;
  },
  reportUser: async (targetUserId, reason, details = '') => {
    const response = await api.post(`/matches/report/${targetUserId}?reason=${encodeURIComponent(reason)}&details=${encodeURIComponent(details)}`);
    return response.data;
  },
  getCompatibility: async (targetUserId) => {
    const response = await api.get(`/matches/compatibility/${targetUserId}`);
    return response.data;
  },
};

export const searchService = {
  search: async (params) => {
    const response = await api.get('/search', { params });
    return response.data;
  },
};

export const chatService = {
  sendMessage: async (receiverId, content) => {
    const response = await api.post(`/chat/send/${receiverId}?content=${encodeURIComponent(content)}`);
    return response.data;
  },
  getHistory: async (receiverId) => {
    const response = await api.get(`/chat/history/${receiverId}`);
    return response.data;
  },
};

export const notificationService = {
  getNotifications: async () => {
    const response = await api.get('/notifications');
    return response.data;
  },
  getUnreadNotifications: async () => {
    const response = await api.get('/notifications/unread');
    return response.data;
  },
  markAsRead: async (notificationId) => {
    const response = await api.post(`/notifications/read/${notificationId}`);
    return response.data;
  },
};

export const adminService = {
  getStats: async () => {
    const response = await api.get('/admin/stats');
    return response.data;
  },
  getPendingVerifications: async () => {
    const response = await api.get('/admin/verifications/pending');
    return response.data;
  },
  approveProfile: async (profileId) => {
    const response = await api.post(`/admin/verifications/approve/${profileId}`);
    return response.data;
  },
  getReports: async () => {
    const response = await api.get('/admin/reports');
    return response.data;
  },
  resolveReport: async (reportId, action = 'RESOLVE') => {
    const response = await api.post(`/admin/reports/resolve/${reportId}?action=${action}`);
    return response.data;
  },
};

export default api;
