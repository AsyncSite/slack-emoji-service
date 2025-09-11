# Slack Emoji Service

One-click Slack emoji pack installation service backend for SlackDori.

> Important
> - Server-based emoji installation via Slack Admin API requires an Enterprise Grid organization and org-admin approval with `user_scope=admin.emoji:write`.
> - For non‑enterprise workspaces, server-side installation is not supported by Slack. Use the Chrome Extension-based client installer instead (see frontend docs).
> - Production server will be taken down for now. Keep this service for future Enterprise support and internal development only.

## 🎯 Overview

This microservice handles (Enterprise-only path):
- Slack OAuth2 authentication
- Emoji pack management (CRUD operations)
- Bulk emoji installation to Slack workspaces
- Installation progress tracking
- Usage analytics

## 🏗️ Architecture

The service follows **Hexagonal Architecture** (Ports & Adapters):

```
├── domain/          # Pure business logic
├── application/     # Use case implementations
└── adapter/         # External integrations
    ├── in/         # Controllers, REST APIs
    └── out/        # Database, Slack API, Redis
```

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- MySQL 8.0
- Redis

### Local Development

1. **Build the project:**
```bash
./gradlew clean build
```

2. **Run with Docker:**
```bash
./gradlew dockerRebuildAndRunSlackEmojiOnly
docker logs -f asyncsite-slack-emoji-service
```

3. **Check health:**
```bash
curl http://localhost:8084/api/v1/health
```

4. **Access Swagger UI:**
```
http://localhost:8084/swagger-ui.html
```

## 📡 API Endpoints

### Emoji Pack Management
- `GET /api/v1/packs` - Get all packs
- `GET /api/v1/packs/{id}` - Get pack by ID
- `GET /api/v1/packs/featured` - Get featured packs
- `GET /api/v1/packs/category/{category}` - Get packs by category

### Slack Integration (Enterprise only)
- `GET /api/v1/slack/auth` - Start OAuth flow (requires user_scope=admin.emoji:write)
- `GET /api/v1/slack/callback` - OAuth callback
- `POST /api/v1/install/{packId}` - Install pack to workspace

## 🔧 Configuration

### Environment Variables (note: client ID/secret may be hardcoded per directive)
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/slackemojidb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=asyncsite_root_2024!

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Slack OAuth (Enterprise only)
SLACK_CLIENT_ID=...  # hardcoded in config for now
SLACK_CLIENT_SECRET=...  # hardcoded in config for now
SLACK_REDIRECT_URI=http://localhost:8084/api/v1/slack/callback

# JWT (shared with User Service)
JWT_SECRET=your-jwt-secret
```

## 🐳 Docker Commands

```bash
# Build and run with tests
./gradlew dockerRebuildAndRunSlackEmojiOnly

# Quick rebuild (skip tests - dev only)
./gradlew dockerQuickRebuildSlackEmojiOnly

# View logs
docker logs -f asyncsite-slack-emoji-service

# Stop service
./gradlew dockerStop
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## 📝 Development Guidelines

1. **Follow Hexagonal Architecture** - Keep domain logic pure
2. **Test Coverage** - Maintain >80% coverage
3. **API Documentation** - Update Swagger annotations
4. **Error Handling** - Use proper HTTP status codes
5. **Logging** - Use structured logging with correlation IDs

## 🔍 Monitoring

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

## 📚 Related Documentation

- [CLAUDE.md](./CLAUDE.md) - Detailed development guidelines (updated with Enterprise-only scope)
- [docs/slack-emoji/BACKEND_SCOPE_AND_INSTALL_STRATEGY.md](./docs/slack-emoji/BACKEND_SCOPE_AND_INSTALL_STRATEGY.md)
- [Slack API Docs](https://api.slack.com/)
- [SlackDori Frontend](../web/slackdori-frontend/)

## 🤝 Contributing

1. Follow the coding conventions in CLAUDE.md
2. Write tests for new features
3. Update API documentation
4. Create small, focused commits

## 📄 License

Proprietary - AsyncSite Team