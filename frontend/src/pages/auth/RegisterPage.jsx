import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authApi from '../../api/authApi';
import useToast from '../../hooks/useToast';
import FormField from '../../components/FormField';
import ErrorMessage from '../../components/ErrorMessage';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
// Mirrors the backend rule: 8+ chars, at least one upper, one lower, one digit.
const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

export default function RegisterPage() {
  const toast = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const validate = () => {
    const errors = {};
    if (!form.username.trim() || form.username.trim().length < 3) {
      errors.username = 'Username must be at least 3 characters';
    }
    if (!EMAIL_RE.test(form.email.trim())) {
      errors.email = 'Enter a valid email address';
    }
    if (!PASSWORD_RE.test(form.password)) {
      errors.password =
        'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, and a digit';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);
    if (!validate()) return;

    setSubmitting(true);
    try {
      await authApi.register({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      toast.success('Account created — you can sign in now');
      navigate('/login', { replace: true });
    } catch (err) {
      // Surface backend field errors (e.g. duplicate username/email) if present.
      if (err.fieldErrors) setFieldErrors(err.fieldErrors);
      setApiError(err);
    } finally {
      setSubmitting(false);
    }
  };

  const inputClass = 'input';

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-800">Create account</h2>
        <p className="text-sm text-slate-500">New accounts are created with the STAFF role.</p>
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

        <FormField label="Email" htmlFor="email" required error={fieldErrors.email}>
          <input
            id="email"
            type="email"
            autoComplete="email"
            className={inputClass}
            value={form.email}
            onChange={update('email')}
          />
        </FormField>

        <FormField
          label="Password"
          htmlFor="password"
          required
          error={fieldErrors.password}
          hint="At least 8 characters, with upper, lower, and a digit."
        >
          <input
            id="password"
            type="password"
            autoComplete="new-password"
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
          {submitting ? 'Creating…' : 'Create account'}
        </button>
      </form>

      <p className="text-center text-sm text-slate-600">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-brand-600 hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
