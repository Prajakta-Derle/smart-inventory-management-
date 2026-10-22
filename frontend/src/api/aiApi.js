import api from './axiosInstance';

export const aiApi = {
  query: (question) => api.post('/ai/query', { question }),
};

export default aiApi;
