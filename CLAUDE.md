# Working on this repo

## Commits and pull requests
- No AI attribution anywhere: no "Generated with Claude Code" line or footer, no `Co-Authored-By` trailer, no session links. This covers commit messages, PR titles and descriptions, and the squash/merge commit message. Some tools add a footer on their own; remove it before merging.
- Branch names use a type prefix: `fix/…`, `feat/…`, `chore/…`, `ci/…`, `test/…`.
- One related change per PR.

## Before pushing
Run what CI runs (`.github/workflows/ci.yml`):

```bash
cd backend && ./gradlew check     # needs Postgres: docker compose up -d db
cd frontend && npm run check
```

Fix formatting with `./gradlew spotlessApply` (backend) or `npm run format` (frontend).
