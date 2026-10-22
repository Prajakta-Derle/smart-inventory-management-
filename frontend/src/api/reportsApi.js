import api from './axiosInstance';

export const reportsApi = {
  purchase: (startDate, endDate) =>
    api.get('/reports/purchase', { params: { startDate, endDate } }),
  inventory: (startDate, endDate) =>
    api.get('/reports/inventory', { params: { startDate, endDate } }),
  supplierPurchase: (startDate, endDate) =>
    api.get('/reports/supplier-purchase', { params: { startDate, endDate } }),
  stockMovements: (startDate, endDate, movementType) =>
    api.get('/reports/stock-movements', { params: { startDate, endDate, movementType } }),
  lowStock: () => api.get('/reports/low-stock'),
};

export default reportsApi;
