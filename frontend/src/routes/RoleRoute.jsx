import { Navigate, Outlet } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

/**
 * Restricts a route subtree to users whose role is in `allowedRoles`.
 * Unauthenticated users go to /login; authenticated-but-unauthorised users
 * go to the 403 page.
 */
export function RoleRoute({ allowedRoles }) {
  const { isAuthenticated, role } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to="/forbidden" replace />;
  }
  return <Outlet />;
}

export default RoleRoute;
