import api from './axiosInstance';

export const stockMovementsApi = {
  list: (params) => api.get('/stock-movements', { params }),
  record: (payload) => api.post('/stock-movements', payload),
};

export default stockMovementsApi;
