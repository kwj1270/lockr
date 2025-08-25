# 6. Log HTTP Requests and Responses to Database

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

사용자 활동을 추적하고, 에러를 디버깅하며, 시스템을 모니터링하기 위해 상세한 로그가 필요합니다. 파일 기반 로깅은 특정 사용자의 활동을 추적하거나 에러로 이어진 이벤트 순서를 분석할 때, 검색하고 분석하기가 어렵습니다. 더 구조화되고 쉽게 쿼리할 수 있는 로깅 솔루션이 필요합니다.

Detailed logs are necessary for tracking user activity, debugging errors, and monitoring the system. File-based logging can be difficult to search and analyze, especially when tracing a specific user's actions or the sequence of events leading to an error. A more structured and easily queryable logging solution is required.

## 결정 Decision

모든 수신 HTTP 요청과 해당 응답을 별도의 데이터베이스 테이블(예: `http_log`)에 기록하기로 결정합니다. 로그 항목에는 요청 URL, 메서드, 헤더, 본문, 응답 상태 코드, 응답 본문, 사용자 식별자(인증된 경우), 타임스탬프, 처리 시간 등의 주요 정보가 포함됩니다. 비밀번호나 인증 토큰과 같은 민감한 정보는 기록 전에 반드시 마스킹 처리해야 합니다.

We have decided to log all incoming HTTP requests and their corresponding responses to a dedicated database table (e.g., `http_log`). The log entry will include key information such as the request URL, method, headers, body, response status code, response body, user identifier (if authenticated), timestamp, and duration. Sensitive information like passwords or authentication tokens must be masked before logging.

## 결과 Consequences

- **긍정적:**
  - 모든 API 상호작용에 대한 구조화되고 쿼리 가능한 감사 추적을 제공합니다.
  - 개발자가 문제의 원인이 된 정확한 요청/응답 내용을 검토할 수 있어 디버깅이 단순화됩니다.
  - API 사용 패턴 분석 및 성능 모니터링이 가능해집니다.

- **고려사항:**
  - 데이터베이스 저장 비용이 증가합니다. 테이블 증가를 관리하기 위해 데이터 보존 및 아카이빙 정책이 필요합니다.
  - 추가적인 데이터베이스 쓰기 작업으로 인해 모든 API 호출에 성능 오버헤드가 발생할 수 있습니다. 응답 시간에 미치는 영향을 최소화하기 위해 이 로깅은 비동기적으로 처리해야 합니다.
  - 민감한 데이터가 제대로 마스킹되지 않을 경우 보안 위험이 있습니다. 마스킹 구현은 철저해야 하며 정기적으로 검토해야 합니다.

- **Positive:**
  - Provides a structured, queryable audit trail of all API interactions.
  - Simplifies debugging by allowing developers to inspect the exact request/response payloads that caused an issue.
  - Enables analysis of API usage patterns and performance monitoring.

- **Considerations:**
  - Increased database storage costs. A data retention and archiving policy will be necessary to manage table growth.
  - Potential performance overhead on every API call due to the additional database write operation. This logging should be handled asynchronously to minimize the impact on response times.
  - Security risk if sensitive data is not properly masked. The masking implementation must be thorough and regularly reviewed.
