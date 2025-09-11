# Backend Scope and Install Strategy

## Summary
- Server-based one-click installation uses Slack Admin API and requires Enterprise Grid with org-admin approval and `user_scope=admin.emoji:write`.
- For non‑enterprise workspaces, server-side installation is not possible. Use the Chrome Extension-based client installer instead.
- Production server will be shut down for now; keep service for Enterprise support and internal development.

## Server Path (Enterprise)
- OAuth authorize uses `user_scope=admin.emoji:write` and redirects back with session.
- Install worker fetches pack metadata and calls `admin.emoji.add`, `admin.emoji.addAlias` with retry/backoff.
- Redis keeps session and job states.

## Non-Enterprise Path
- Chrome Extension injects into `https://*.slack.com/customize/emoji` and simulates drag&drop uploads using the user's session.
- Frontend exposes “Install via Extension” and hides server OAuth for non-enterprise.

## Build & Run (Local)
- Follow CLAUDE.md. Use Gradle tasks to build Docker image: `./gradlew dockerRebuildAndRunSlackEmojiOnly`.

## Decision Log
- 2025-09: Backend is optional for non‑enterprise; primary path is the extension installer. Server decommissioned in production context.
