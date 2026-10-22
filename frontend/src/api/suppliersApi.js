import api from './axiosInstance';

export const suppliersApi = {
  list: (params) => api.get('/suppliers', { params }),
  getById: (id) => api.get(`/suppliers/${id}`),
  create: (payload) => api.post('/suppliers', payload),
  update: (id, payload) => api.put(`/suppliers/${id}`, payload),
  deactivate: (id) => api.patch(`/suppliers/${id}/deactivate`),
};

export default suppliersApi;
