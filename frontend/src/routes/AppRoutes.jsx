import { Routes, Route, Navigate } from 'react-router-dom';

import AuthLayout from '../layouts/AuthLayout';
import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';

import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import NotFound from '../pages/NotFound';
import Forbidden from '../pages/Forbidden';
import CategoriesPage from '../pages/categories/CategoriesPage';
import SuppliersPage from '../pages/suppliers/SuppliersPage';
import SupplierDetailPage from '../pages/suppliers/SupplierDetailPage';
import ProductsPage from '../pages/products/ProductsPage';
import ProductDetailPage from '../pages/products/ProductDetailPage';
import PurchaseOrdersPage from '../pages/purchase-orders/PurchaseOrdersPage';
import PurchaseOrderDetailPage from '../pages/purchase-orders/PurchaseOrderDetailPage';
import CreatePurchaseOrderPage from '../pages/purchase-orders/CreatePurchaseOrderPage';
import StockMovementsPage from '../pages/stock-movements/StockMovementsPage';
import LowStockPage from '../pages/low-stock/LowStockPage';
import DashboardPage from '../pages/dashboard/DashboardPage';
import ReportsPage from '../pages/reports/ReportsPage';
import AIAssistantPage from '../pages/ai-assistant/AIAssistantPage';
import UsersPage from '../pages/users/UsersPage';

/**
 * Central route table.
 *  - Public:   /login, /register (AuthLayout, no guard)
 *  - Protected (any authenticated user): dashboard, products, purchase orders,
 *    stock movements, low stock, reports, AI assistant
 *  - Admin-only: categories, suppliers, users
 */
export default function AppRoutes() {
  return (
    <Routes>
      {/* Public auth routes */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      {/* Authenticated area */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/purchase-orders" element={<PurchaseOrdersPage />} />
          <Route path="/purchase-orders/new" element={<CreatePurchaseOrderPage />} />
          <Route path="/purchase-orders/:id" element={<PurchaseOrderDetailPage />} />
          <Route path="/stock-movements" element={<StockMovementsPage />} />
          <Route path="/low-stock" element={<LowStockPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/ai-assistant" element={<AIAssistantPage />} />

          {/* Admin-only */}
          <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="/suppliers" element={<SuppliersPage />} />
            <Route path="/suppliers/:id" element={<SupplierDetailPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Route>
        </Route>
      </Route>

      {/* Error pages */}
      <Route path="/forbidden" element={<Forbidden />} />

      {/* Root + catch-all */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
