import api from './axiosInstance';

export const usersApi = {
  create: (payload) => api.post('/users', payload),
  list: (params) => api.get('/users', { params }),
};

export default usersApi;
