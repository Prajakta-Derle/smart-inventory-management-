import api from './axiosInstance';

export const productsApi = {
  list: (params) => api.get('/products', { params }),
  getById: (id) => api.get(`/products/${id}`),
  create: (payload) => api.post('/products', payload),
  update: (id, payload) => api.put(`/products/${id}`, payload),
  deactivate: (id) => api.patch(`/products/${id}/deactivate`),
};

export default productsApi;
