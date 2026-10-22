# Deployment Guide — Smart Inventory Management

This deploys the app for free using:

- **Database** → free hosted MySQL (Railway or Aiven)
- **Backend** (Spring Boot) → **Render** (Docker)
- **Frontend** (React/Vite) → **Vercel**

You need a **GitHub account** and the project pushed to a GitHub repo. Create
free accounts at railway.app (or aiven.io), render.com, and vercel.com.

---

## Step 0 — Push the project to GitHub

From the project root:

```bash
git init
git add .
git commit -m "Smart Inventory Management"
# create an empty repo on GitHub, then:
git remote add origin https://github.com/<you>/smart-inventory.git
git branch -M main
git push -u origin main
```

> The repo should contain both `backend/` and `frontend/` folders and the
> `render.yaml` at the root.

---

## Step 1 — Create a hosted MySQL database

**Using Railway (easiest):**
1. railway.app → New Project → **Add MySQL**.
2. Open the MySQL service → **Variables / Connect** tab. Note these values:
   - host, port, database name, username, password.
3. Build the JDBC URL you'll give the backend:
   ```
   jdbc:mysql://<host>:<port>/<database>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```

(Aiven works the same way — create a MySQL service, copy its connection details.)

---

## Step 2 — Deploy the backend on Render

1. render.com → **New → Blueprint** → connect your GitHub repo.
   Render reads `render.yaml` and creates the `smart-inventory-backend` web service
   (built from `backend/Dockerfile`).
2. When prompted (or in the service's **Environment** tab), set these variables:

   | Variable | Value |
   |---|---|
   | `DB_URL` | the JDBC URL from Step 1 |
   | `DB_USERNAME` | MySQL username |
   | `DB_PASSWORD` | MySQL password |
   | `JWT_SECRET` | a long random string (32+ characters) |
   | `CORS_ALLOWED_ORIGIN` | your Vercel URL (fill in after Step 3; can start with a placeholder) |
   | `OPENAI_API_KEY` | optional — leave blank if not using the AI feature |

3. Deploy. First build takes a few minutes. When it's live, note the URL,
   e.g. `https://smart-inventory-backend.onrender.com`.
4. Verify: open `https://<your-backend>.onrender.com/api-docs` — you should get JSON.

> Note: Render's free tier sleeps after inactivity; the first request after a
> while may take ~30–60s to wake up. That's normal for the free plan.

---

## Step 3 — Deploy the frontend on Vercel

1. vercel.com → **Add New → Project** → import your GitHub repo.
2. Set **Root Directory** to `frontend`.
3. Framework preset: **Vite** (auto-detected). Build command `npm run build`,
   output dir `dist` (defaults are correct).
4. Add an **Environment Variable**:

   | Name | Value |
   |---|---|
   | `VITE_API_BASE_URL` | `https://<your-backend>.onrender.com/api/v1` |

5. Deploy. Note your frontend URL, e.g. `https://smart-inventory.vercel.app`.

---

## Step 4 — Connect the two (CORS)

1. Go back to Render → backend service → Environment.
2. Set `CORS_ALLOWED_ORIGIN` to your exact Vercel URL (no trailing slash),
   e.g. `https://smart-inventory.vercel.app`.
   - To allow local dev too, use a comma-separated list:
     `https://smart-inventory.vercel.app,http://localhost:5173`
3. Save — Render redeploys automatically.

---

## Step 5 — Create the first ADMIN user

Registration always creates a STAFF account, so bootstrap one admin:

1. Open your Vercel site → **Register** a user (becomes STAFF).
2. Promote it to ADMIN in the hosted DB. Using Railway's MySQL console (or any
   MySQL client connected to the hosted DB):
   ```sql
   UPDATE users SET role='ADMIN' WHERE username='<your-username>';
   ```
3. Log out and back in — you now have full ADMIN access.

---

## Done

Visit your Vercel URL and log in. The frontend calls the Render backend, which
talks to the hosted MySQL.

### Notes / production hardening (optional, later)
- The backend uses `spring.jpa.hibernate.ddl-auto=update`, which auto-creates
  tables. For a long-lived production DB you may later switch to managed
  migrations (Flyway/Liquibase).
- Keep `JWT_SECRET` and `DB_PASSWORD` secret — they live only in the host's
  environment variables, never in the repo.
- The AI Assistant needs a paid `OPENAI_API_KEY`; without it, that one page
  shows a graceful "temporarily unavailable" message and everything else works.
