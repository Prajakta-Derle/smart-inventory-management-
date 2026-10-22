import api from './axiosInstance';

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (payload) => api.post('/auth/register', payload),
};

export default authApi;
