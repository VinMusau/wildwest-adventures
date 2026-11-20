# Wild West Adventures

Spring Boot API (`backend/`) and React + Vite app (`frontend/`).

## Checks

CI (`.github/workflows/ci.yml`) runs these on every pull request and on pushes to `main`. Run them locally before pushing.

### Backend

The tests need Postgres: `docker compose up -d db`.

```bash
cd backend
./gradlew check          # format check, compile with warnings as errors, tests
./gradlew spotlessApply  # fix formatting
```

### Frontend

```bash
cd frontend
npm run check            # format check, lint, typecheck, tests
npm run format           # fix formatting
```

Individually: `npm run format:check`, `npm run lint`, `npm run typecheck`, `npm test`.
