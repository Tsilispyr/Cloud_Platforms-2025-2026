import axios from 'axios';
import { useAuthStore } from './stores/auth';

const api = axios.create({
  baseURL: '/api'
});

api.interceptors.request.use(config => {
  const authStore = useAuthStore();
  const token = authStore.getToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Ensure Content-Type is set for POST/PUT requests
  if (config.method === 'post' || config.method === 'put' || config.method === 'patch') {
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json';
    }
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api; 