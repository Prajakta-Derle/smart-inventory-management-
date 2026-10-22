import axios from 'axios';

/**
 * In-memory JWT accessor. AuthContext registers a getter here so the request
 * interceptor can read the current token at call time without importing React
 * state. The token is NEVER read from localStorage/sessionStorage/cookies
 * (design: in-memory-only JWT policy).
 */
let tokenGetter = () => null;
let unauthorizedHandler = () => {};

export function registerTokenGetter(getter) {
  tokenGetter = getter;
}

export function registerUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

const axiosInstance = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach the Bearer token if present.
axiosInstance.interceptors.request.use((config) => {
  const token = tokenGetter();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  } else {
    delete config.headers.Authorization;
  }
  return config;
});

// Response interceptor — unwrap the backend envelope and handle 401 globally.
axiosInstance.interceptors.response.use(
  (response) => {
    // Backend wraps every success in { success, statusCode, message, data }.
    const body = response.data;
    if (body && typeof body === 'object' && 'data' in body && 'success' in body) {
      return body.data;
    }
    return body;
  },
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      unauthorizedHandler();
    }
    // Normalise the error envelope so callers can read a message reliably.
    const payload = error.response?.data;
    const normalised = {
      status,
      message: payload?.message || error.message || 'Request failed',
      fieldErrors: payload?.fieldErrors || null,
      raw: payload,
    };
    return Promise.reject(normalised);
  },
);

export default axiosInstance;
