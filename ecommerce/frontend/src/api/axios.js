import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Expired/invalid token -> back to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401 && localStorage.getItem('token')) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

/** Turn an axios error into a readable message (uses the API's fieldErrors when present). */
export const errMsg = (e) => {
  const data = e.response?.data;
  if (data?.fieldErrors) return Object.values(data.fieldErrors).join(', ');
  return data?.message || e.message || 'Something went wrong';
};

export default api;
