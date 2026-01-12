import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle response errors
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/signin';
    }
    return Promise.reject(error);
  }
);

export const apiService = {
  // Auth endpoints
  register: async (userData) => {
    const response = await axiosInstance.post('/auth/register', userData);
    return response.data;
  },

  login: async (credentials) => {
    const response = await axiosInstance.post('/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('userId', response.data.userId);
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
  },

  validateToken: async (token) => {
    const response = await axiosInstance.get('/auth/validate', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },

  // User endpoints
  getUserDashboard: async () => {
    const response = await axiosInstance.get('/user/dashboard');
    return response.data;
  },

  getEnrolledCourses: async () => {
    const response = await axiosInstance.get('/user/enrolled-courses');
    return response.data;
  },

  getCourseDetails: async (courseId) => {
    const response = await axiosInstance.get(`/course/${courseId}`);
    return response.data;
  },

  getProfile: async () => {
    const response = await axiosInstance.get('/user/profile');
    return response.data;
  },

  enrollCourse: async (courseId) => {
    const response = await axiosInstance.post(`/user/enroll/${courseId}`, {});
    return response.data;
  },

  getCourseProgress: async (courseId) => {
    const response = await axiosInstance.get(`/course/${courseId}/progress`);
    return response.data;
  },

  updateCourseProgress: async (courseId, progressData) => {
    const response = await axiosInstance.put(
      `/course/${courseId}/progress`,
      progressData
    );
    return response.data;
  },

  // Like/Unlike operations
  likeCourse: async (courseId) => {
    const response = await axiosInstance.post(`/course/${courseId}/like`);
    return response.data;
  },

  unlikeCourse: async (courseId) => {
    const response = await axiosInstance.delete(`/course/${courseId}/like`);
    return response.data;
  },

  getLikedCourses: async () => {
    const response = await axiosInstance.get('/course/liked');
    return response.data;
  },

  unenrollCourse: async (courseId) => {
    const response = await axiosInstance.delete(`/course/${courseId}/unenroll`);
    return response.data;
  },

  // Game endpoint
  startGame: async (category) => {
    const response = await axiosInstance.get(`/game/start/${category}`);
    return response.data;
  },

  // Admin endpoints
  getAllUsers: async () => {
    const response = await axiosInstance.get('/admin/users');
    return response.data;
  },

  getAllCourses: async () => {
    const response = await axiosInstance.get('/admin/courses');
    return response.data;
  },

  getUserById: async (userId) => {
    const response = await axiosInstance.get(`/admin/users/${userId}`);
    return response.data;
  },

  deleteUser: async (userId) => {
    const response = await axiosInstance.delete(`/admin/users/${userId}`);
    return response.data;
  },

  createCourse: async (courseData) => {
    const response = await axiosInstance.post('/admin/courses', courseData);
    return response.data;
  },

  updateCourse: async (courseId, courseData) => {
    const response = await axiosInstance.put(
      `/admin/courses/${courseId}`,
      courseData
    );
    return response.data;
  },

  deleteCourse: async (courseId) => {
    const response = await axiosInstance.delete(`/admin/courses/${courseId}`);
    return response.data;
  },

  updateUserProfile: async (profileData) => {
    const response = await axiosInstance.put(
      '/course/user/profile',
      profileData
    );
    return response.data;
  },
};
