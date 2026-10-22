import api from './axiosInstance';

export const dashboardApi = {
  summary: () => api.get('/dashboard/summary'),
  recentMovements: () => api.get('/dashboard/recent-movements'),
  purchaseTrend: (startDate, endDate) =>
    api.get('/dashboard/purchase-trend', { params: { startDate, endDate } }),
  inventoryTrend: (startDate, endDate) =>
    api.get('/dashboard/inventory-trend', { params: { startDate, endDate } }),
};

export default dashboardApi;
