# SlackDori One-Click Install (Minimal E2E)

## Overview
- User clicks Install → Slack OAuth → callback redirects to `/oauth/callback` → backend starts install job → worker adds emojis using Slack Admin API → UI polls status.

## Redis Keys
- `oauth:state:{state}` → `{ packId }` (TTL 5m)
- `session:{sessionId}` → `{ accessToken, teamId, teamName, createdAt }` (TTL 24h)
- `install:job:{jobId}` → `{ status, progress, total, startedAt, completedAt, errors? }` (TTL 1h)
- `install:queue` (LIST) → job IDs

## OAuth
- GET `/api/public/v1/slack/auth?packId=...` stores state→packId and redirects to Slack.
- GET `/api/public/v1/slack/callback?code&state` exchanges code for token, stores session in Redis, redirects to frontend `/oauth/callback?sessionId&packId`.

## Install API
- POST `/api/v1/install/{packId}` with header `X-Session-Id` → creates job and enqueues.
- GET `/api/v1/install/status/{jobId}` → returns job state.

## Worker
- Scheduled worker pops job ID from `install:queue`, loads pack from GitHub, loops emojis:
  - `admin.emoji.add(name, url, team_id)`
  - `admin.emoji.addAlias(name, alias, team_id)`
  - Updates `progress` and marks `completed` or `failed`.

## Frontend
- `/oauth/callback` page reads `sessionId` and `packId`, stores session, calls install API, polls status every 1.5s.

## Notes
- Requires Slack Admin API (`admin.emoji:write`) with appropriate workspace/org permissions.
- Non‑enterprise workspaces may not support these endpoints. The job will fail accordingly.
