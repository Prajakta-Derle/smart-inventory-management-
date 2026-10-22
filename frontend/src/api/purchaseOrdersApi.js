import api from './axiosInstance';

export const purchaseOrdersApi = {
  list: (params) => api.get('/purchase-orders', { params }),
  getById: (id) => api.get(`/purchase-orders/${id}`),
  create: (payload) => api.post('/purchase-orders', payload),
  updateStatus: (id, status) => api.patch(`/purchase-orders/${id}/status`, { status }),
  items: (id) => api.get(`/purchase-orders/${id}/items`),
};

export default purchaseOrdersApi;
