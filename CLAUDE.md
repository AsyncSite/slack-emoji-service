# Slack Emoji Service - 개발 가이드라인

## 🚨🚨🚨 절대 필수 규칙 🚨🚨🚨

### ⚠️ 빌드 및 파일 수정 시 반드시 준수할 규칙

**1. 빌드 시 개별 서비스 통과 100% 무조건 시키고 통과 안하면 다음 단계 진행하지 말 것**
- 모든 테스트가 100% 통과해야만 다음 단계로 진행
- 테스트 실패 시 반드시 문제를 해결한 후 진행
- 테스트 스킵이나 우회 절대 금지

**2. 파일 수정할 때는 일부분만 보고 하지 말고 해당 파일 전체를 무조건 읽고 전부 이해한 다음에 수정할 것**
- 파일의 일부만 읽고 수정하는 것 절대 금지
- 반드시 전체 파일을 읽고 전체 맥락을 이해한 후 수정
- 의존성과 연관성을 파악한 후 작업 진행

## 🚨 필독 - 로컬 빌드 전 반드시 확인!

### ⚠️ Docker 빌드 표준 준수 필수
**이 서비스를 빌드하기 전에 반드시 [Docker 빌드 표준 문서](../core-platform/docs/development/DOCKER_BUILD_STANDARDS.md)를 읽고 따르세요.**

### 🔨 Slack Emoji Service 빌드 방법
```bash
# Slack Emoji Service 빌드 (테스트 포함 - 필수)
cd ~/asyncsite/slack-emoji-service
./gradlew dockerRebuildAndRunSlackEmojiOnly
```

**절대 금지사항:**
- ❌ `./gradlew build -x test` (테스트 스킵 금지)
- ❌ `docker build/run` 수동 실행 금지
- ❌ 테스트 실패 무시하고 진행 금지

테스트가 실패하면 **반드시 테스트를 통과시킨 후** 빌드하세요.

---

### 🔌 로컬 MySQL 접속 방법
```bash
# Docker MySQL 컨테이너 접속
docker exec -it asyncsite-mysql mysql -uroot -pasyncsite_root_2024!

# Slack Emoji DB 선택
USE slackemojidb;

# 데이터 확인 예시
SELECT * FROM emoji_packs LIMIT 10;
SELECT COUNT(*) FROM installed_packs;
```

**데이터베이스 정보:**
- Host: `localhost` (로컬) / `asyncsite-mysql` (Docker)
- Port: `3306`
- Database: `slackemojidb`
- Username: `root`
- Password: `asyncsite_root_2024!`

---

## 1. 프로젝트 개요

SlackDori 서비스의 백엔드로, Slack 워크스페이스에 이모지 팩을 한 번에 설치할 수 있게 해주는 마이크로서비스입니다. 
Slack OAuth2와 Admin API를 활용하여 대량의 커스텀 이모지를 자동으로 추가합니다.

**제공 기능:**
- Slack OAuth2 인증 및 워크스페이스 연동
- 이모지 팩 관리 (CRUD)
- 대량 이모지 설치 자동화
- 설치 이력 및 통계 관리
- 인기 팩 추천 시스템

## 2. 기술 스택

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.5.3, Spring Cloud 2024.0.1
- **빌드 도구**: Gradle (Kotlin DSL)
- **Service Discovery**: Netflix Eureka
- **보안**: Spring Security, OAuth2 Client (Slack)
- **데이터베이스**: MySQL 8.0
- **캐시**: Redis (설치 진행 상태 추적)
- **메시징**: Kafka (이벤트 기반 통신)
- **컨테이너**: Docker, Docker Compose
- **테스트**: JUnit 5, Mockito

## 3. 코딩 컨벤션

### 3.1. Java 스타일
- 모던 Java 관용구와 컨벤션을 따릅니다
- **불변성(Immutability)**을 적극적으로 활용합니다 (`final` 키워드 사용)
- `Optional<T>`을 사용하여 Null을 안전하게 다룹니다
- Google Java Style Guide를 따릅니다

### 3.2. 패키지 구조
```
com.asyncsite.slackemojiservice.{domain}.{layer}.{feature}
```
- `domain`: `emoji` (이모지 팩 관리), `slack` (Slack 연동), `common` (공통)
- `layer`: `domain`, `application`, `adapter` (헥사고날 아키텍처)
- `feature`: 도메인 내 세부 기능

### 3.3. 네이밍 컨벤션
- **클래스**: `PascalCase` (e.g., `EmojiPackService`, `SlackAuthConfig`)
- **메서드**: `camelCase` (e.g., `installEmojiPack()`)
- **상수**: `UPPER_SNAKE_CASE` (e.g., `MAX_EMOJI_SIZE`)
- **패키지**: `lowercase` (e.g., `com.asyncsite.slackemojiservice`)

### 3.4. Spring Boot 컨벤션
- 생성자 주입(Constructor Injection)을 사용합니다 (`@Autowired` 필드 주입 지양)
- 설정 클래스는 `config` 패키지에 위치시킵니다
- 외부 설정은 `@ConfigurationProperties`를 사용합니다
- 프로필: `local`, `dev`, `staging`, `prod`

### 3.5. API 설계
- RESTful 엔드포인트를 지향합니다
- 적절한 HTTP 상태 코드를 사용합니다
- 일관된 에러 응답 형식을 유지합니다
- API 버전을 관리합니다 (`/api/v1/`)

### 3.6. 보안 가이드라인
- **비밀 정보(Secrets)를 절대 커밋하지 않습니다**
- Slack Client Secret 등 민감한 데이터는 환경 변수를 사용합니다
- 적절한 인증/인가를 구현합니다
- OAuth2 모범 사례를 따릅니다

## 4. 문제 해결 접근법 (Problem Solving Approach)

⚠️ **필수 준수 사항**: 모든 문제 해결 시 다음 5단계를 반드시 따라야 합니다.

1. **Think hard and deeply about the root cause**
   - 표면적 증상이 아닌 실제 문제의 근원을 파악하세요
   - "왜(Why)"를 최소 5번 반복하여 깊이 있게 분석하세요
   - 로그, 스택 트레이스, Slack API 응답을 꼼꼼히 확인하세요

2. **Do a global inspection to understand the full context**
   - 변경이 영향을 미칠 모든 서비스와 컴포넌트를 검토하세요
   - Gateway, User Service 등과의 의존성을 확인하세요
   - 기존 코드베이스의 패턴과 Clean Architecture 구조를 이해하세요

3. **Find a stable, best-practice solution**
   - 검증된 디자인 패턴과 Spring Boot 베스트 프랙티스를 활용하세요
   - 일회성 해결이 아닌 지속 가능하고 확장 가능한 솔루션을 구현하세요
   - 성능, 보안, 유지보수성을 항상 고려하세요

4. **Ensure consistency with other services**
   - 다른 마이크로서비스들의 구현 방식을 참고하세요
   - 공통 패턴과 코딩 규칙을 일관되게 적용하세요
   - 중복 코드는 core-platform의 common 모듈로 추출하세요

5. **Read CLAUDE.md if needed**
   - 불확실한 부분은 항상 이 가이드라인을 재확인하세요
   - 서비스별 특수 규칙과 제약사항을 체크하세요
   - 다른 서비스의 CLAUDE.md도 참고하세요

**이 접근법을 따르지 않으면 불완전하거나 일관성 없는 솔루션이 될 수 있습니다.**

## 5. 헥사고날 아키텍처 (Hexagonal Architecture)

우리 프로젝트는 헥사고날 아키텍처(Ports & Adapters)를 따릅니다. 
**가장 중요한 원칙**: **모든 의존성은 안쪽으로 향해야 합니다.** (`Adapter` → `Application` → `Domain`)

### 5.1. 패키지 구조

```
com.asyncsite.slackemojiservice
├── emoji                         // 이모지 팩 도메인
│   ├── domain
│   │   ├── model                // EmojiPack, Emoji
│   │   └── port
│   │       ├── in              // UseCase 인터페이스
│   │       └── out             // Repository 인터페이스
│   ├── application
│   │   └── service             // UseCase 구현체
│   └── adapter
│       ├── in
│       │   └── web            // REST Controller
│       └── out
│           └── persistence    // JPA Repository
│
├── slack                        // Slack 연동 도메인
│   ├── domain
│   │   ├── model               // SlackWorkspace, InstallationStatus
│   │   └── port
│   │       ├── in             // InstallEmojiUseCase
│   │       └── out            // SlackApiPort
│   ├── application
│   │   └── service            // Slack 연동 서비스
│   └── adapter
│       ├── in
│       │   └── web           // OAuth Controller
│       └── out
│           ├── client         // Slack API Client
│           └── cache          // Redis 진행 상태
│
└── common                      // 공통 모듈
    ├── config                  // Spring 설정
    ├── security               // 보안 설정
    └── exception             // 예외 처리
```

### 5.2. 계층별 책임

- **Domain**: 순수한 비즈니스 로직, 프레임워크 의존성 없음
- **Application**: Use Case 구현, 도메인 오케스트레이션
- **Adapter**: 외부 시스템과의 연결 (Web, DB, Slack API)

## 6. 주요 API 엔드포인트

### 6.1. 인증 관련
```
GET  /api/v1/slack/auth          - Slack OAuth 시작
GET  /api/v1/slack/callback      - OAuth 콜백 처리
POST /api/v1/slack/disconnect    - 워크스페이스 연결 해제
```

### 6.2. 이모지 팩 관리
```
GET  /api/v1/packs               - 팩 목록 조회
GET  /api/v1/packs/{id}          - 팩 상세 조회
POST /api/v1/packs               - 팩 생성 (관리자)
PUT  /api/v1/packs/{id}          - 팩 수정 (관리자)
DELETE /api/v1/packs/{id}        - 팩 삭제 (관리자)
```

### 6.3. 설치 관련
```
POST /api/v1/install/{packId}    - 팩 설치 시작
GET  /api/v1/install/status/{id} - 설치 진행 상태
GET  /api/v1/install/history     - 설치 이력 조회
```

## 7. Slack API 연동 사양

### 7.1. 필요한 OAuth Scopes
```
emoji:read        - 기존 이모지 읽기
emoji:write       - 새 이모지 추가
team:read        - 워크스페이스 정보 읽기
```

### 7.2. Rate Limiting 대응
- Slack API는 Tier 2: 20 requests/minute
- 지수 백오프(Exponential Backoff) 구현
- Redis를 활용한 요청 큐 관리

### 7.3. 이모지 제한사항
- 최대 파일 크기: 128KB
- 지원 형식: PNG, JPG, GIF
- 이모지 이름: 영문 소문자, 숫자, 언더스코어만 허용

## 8. 데이터베이스 스키마

### 8.1. 주요 테이블
```sql
-- 이모지 팩
CREATE TABLE emoji_packs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    author VARCHAR(100),
    category VARCHAR(50),
    emoji_count INT,
    download_count INT DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 개별 이모지
CREATE TABLE emojis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pack_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    image_url VARCHAR(500),
    aliases JSON,
    FOREIGN KEY (pack_id) REFERENCES emoji_packs(id)
);

-- 설치 이력
CREATE TABLE installations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    workspace_id VARCHAR(100),
    pack_id BIGINT NOT NULL,
    status VARCHAR(20),
    installed_count INT,
    failed_count INT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (pack_id) REFERENCES emoji_packs(id)
);

-- Slack 워크스페이스 연동
CREATE TABLE slack_workspaces (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    team_id VARCHAR(100) UNIQUE,
    team_name VARCHAR(200),
    access_token TEXT,
    bot_user_id VARCHAR(100),
    connected_at TIMESTAMP,
    last_used_at TIMESTAMP
);
```

## 9. Docker 설정

### 9.1. Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9.2. docker-compose 통합
```yaml
slack-emoji-service:
  container_name: asyncsite-slack-emoji-service
  build: ./slack-emoji-service
  ports:
    - "8084:8084"
  environment:
    SPRING_PROFILES_ACTIVE: docker
    EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka/
    SPRING_DATASOURCE_URL: jdbc:mysql://asyncsite-mysql:3306/slackemojidb
  depends_on:
    - eureka-server
    - mysql
    - redis
  networks:
    - asyncsite-network
```

## 10. 환경 변수

```bash
# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/slackemojidb
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=asyncsite_root_2024!

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Slack OAuth
SLACK_CLIENT_ID=your-slack-client-id
SLACK_CLIENT_SECRET=your-slack-client-secret
SLACK_REDIRECT_URI=http://localhost:8084/api/v1/slack/callback

# JWT (User Service와 공유)
JWT_SECRET=your-jwt-secret
```

## 11. 테스트 전략

### 11.1. 단위 테스트
- 도메인 로직 100% 커버리지
- Mock을 활용한 격리된 테스트

### 11.2. 통합 테스트
- @SpringBootTest로 전체 컨텍스트 테스트
- TestContainers로 실제 MySQL, Redis 테스트

### 11.3. Slack API 모킹
- WireMock으로 Slack API 응답 시뮬레이션
- 다양한 에러 시나리오 테스트

## 12. 빌드 및 실행

```bash
# 로컬 실행
./gradlew bootRun

# 테스트
./gradlew test

# Docker 빌드 및 실행
./gradlew dockerRebuildAndRunSlackEmojiOnly

# 로그 확인
docker logs -f asyncsite-slack-emoji-service
```

## 13. AI 어시스턴트 중요 참고사항

1. **기존 코드 패턴 확인**: Study Service, User Service의 패턴과 일관성 유지
2. **클린 아키텍처 준수**: 헥사고날 아키텍처 원칙 엄격히 준수
3. **전문적인 익명 커밋**: 커밋 메시지에 AI 관련 단어 절대 포함 금지
4. **독립 실행성 보장**: 다른 서비스 없이도 기본 기능 동작
5. **의존성 관리**: core-platform common 모듈 활용
6. **Docker 빌드**: 반드시 Gradle 태스크 사용
7. **Slack API 제한**: Rate Limiting과 파일 크기 제한 항상 고려

## 14. 개발 로드맵

### Phase 1: MVP (현재)
- [x] 프로젝트 구조 설정
- [ ] Slack OAuth2 연동
- [ ] 기본 팩 CRUD API
- [ ] 단일 이모지 설치 기능

### Phase 2: 핵심 기능
- [ ] 대량 설치 큐 시스템
- [ ] 설치 진행 상태 실시간 업데이트
- [ ] 인기 팩 추천 알고리즘
- [ ] 설치 실패 복구 메커니즘

### Phase 3: 고급 기능
- [ ] 팩 버전 관리
- [ ] 사용자 커스텀 팩 생성
- [ ] 팩 공유 및 마켓플레이스
- [ ] 분석 및 통계 대시보드

## 15. Known Issues & Solutions

### 15.1. Slack API 인증 만료
- **문제**: Access Token 만료
- **해결**: Refresh Token 구현 또는 재인증 유도

### 15.2. 대량 설치 시 타임아웃
- **문제**: 100개 이상 이모지 설치 시 타임아웃
- **해결**: 비동기 처리 + 배치 작업

### 15.3. 이미지 호스팅
- **문제**: GitHub Raw URL 직접 사용 불가
- **해결**: 프록시 서버 또는 CDN 활용

## 16. 🚨 CRITICAL: 필수 개발 규칙

### 필수 규칙
- 변경 전 관련 파일 전체 읽기
- 작은 단위로 커밋
- 가정 사항 문서화
- 시크릿 절대 커밋 금지
- 의미 있는 네이밍 사용

### 사고방식
- 시니어 엔지니어처럼 생각하기
- 추측하지 말고 확인하기
- 여러 옵션 비교 후 결정

### 테스트 규칙
- 새 코드는 새 테스트 필수
- 버그 수정은 회귀 테스트 포함
- 테스트는 독립적이고 결정적이어야 함

### 보안 규칙
- 시크릿을 코드/로그에 남기지 않기
- 입력 검증 및 정규화
- 최소 권한 원칙 적용

---

**"Think hard and think very very deeply"** - 항상 이 원칙을 기억하세요.