import { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import authApi from '../../api/authApi';
import useAuth from '../../hooks/useAuth';
import useToast from '../../hooks/useToast';
import FormField from '../../components/FormField';
import ErrorMessage from '../../components/ErrorMessage';

/**
 * Translates any login error into a friendly, non-technical message.
 * Never surfaces raw "status code 500" style text to the user.
 */
function friendlyLoginError(err) {
  const status = err?.status;
  if (status === 401) {
    return 'Invalid username or password. Please try again.';
  }
  if (status === 400) {
    return 'Please enter a valid username and password.';
  }
  if (status === 403) {
    return 'Your account does not have access. Contact an administrator.';
  }
  if (status >= 500 || status == null) {
    return 'We could not sign you in right now. Please try again in a moment.';
  }
  return 'Sign in failed. Please try again.';
}

export default function LoginPage() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/dashboard';

  const [form, setForm] = useState({ username: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const validate = () => {
    const errors = {};
    if (!form.username.trim()) errors.username = 'Username is required';
    if (!form.password) errors.password = 'Password is required';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      const data = await authApi.login({
        username: form.username.trim(),
        password: form.password,
      });
      login(data.token, data.role, data.username);
      toast.success('Signed in successfully');
      navigate(from, { replace: true });
    } catch (err) {
      setApiError(friendlyLoginError(err));
    } finally {
      setSubmitting(false);
    }
  };

  const inputClass = 'input';

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-800">Sign in</h2>
        <p className="text-sm text-slate-500">Access your inventory workspace.</p>
      </div>

      {apiError ? <ErrorMessage error={apiError} /> : null}

      <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
        <FormField label="Username" htmlFor="username" required error={fieldErrors.username}>
          <input
            id="username"
            type="text"
            autoComplete="username"
            className={inputClass}
            value={form.username}
            onChange={update('username')}
          />
        </FormField>

        <FormField label="Password" htmlFor="password" required error={fieldErrors.password}>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className={inputClass}
            value={form.password}
            onChange={update('password')}
          />
        </FormField>

        <button
          type="submit"
          disabled={submitting}
          className="btn-primary mt-1 w-full"
        >
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="text-center text-sm text-slate-600">
        No account?{' '}
        <Link to="/register" className="font-medium text-brand-600 hover:underline">
          Register
        </Link>
      </p>
    </div>
  );
}
