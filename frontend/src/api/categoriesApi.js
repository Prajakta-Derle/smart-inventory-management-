import api from './axiosInstance';

export const categoriesApi = {
  list: () => api.get('/categories'),
  create: (payload) => api.post('/categories', payload),
  update: (id, payload) => api.put(`/categories/${id}`, payload),
  deactivate: (id) => api.patch(`/categories/${id}/deactivate`),
  productsByCategory: (id, params) => api.get(`/categories/${id}/products`, { params }),
};

export default categoriesApi;
