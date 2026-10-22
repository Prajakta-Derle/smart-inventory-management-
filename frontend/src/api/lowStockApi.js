import api from './axiosInstance';

export const lowStockApi = {
  list: (params) => api.get('/low-stock', { params }),
  outOfStock: (params) => api.get('/low-stock/out-of-stock', { params }),
  summary: () => api.get('/low-stock/summary'),
};

export default lowStockApi;
