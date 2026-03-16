# 7. Use OpenAPI 3 and Swagger for API Documentation

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

API가 성장함에 따라 명확하고 일관되며 최신 상태의 문서를 유지하는 것이 중요합니다. 이는 API를 사용하는 프론트엔드, 모바일 및 다른 백엔드 개발자들에게 필수적입니다. API 문서를 수동으로 작성하고 유지하는 것은 오류가 발생하기 쉽고 시간이 많이 소요됩니다. API 문서를 자동으로 생성하고 공유할 수 있는 방법이 필요합니다.

As our API grows, it is crucial to maintain clear, consistent, and up-to-date documentation. This is essential for frontend, mobile, and other backend developers who consume the API. Manually writing and maintaining API documentation is error-prone and time-consuming. We need a way to automatically generate and share interactive API documentation.

## 결정 Decision

우리는 REST API를 정의하기 위해 OpenAPI 3 명세를 사용하기로 결정합니다. Spring Boot 프로젝트에 `springdoc-openapi`와 같은 라이브러리를 통합하여 Java 컨트롤러 코드와 어노테이션으로부터 OpenAPI 명세를 자동으로 생성할 것입니다. 이 명세는 특정 URL을 통해 노출되며, 우리는 Swagger UI를 사용하여 API 엔드포인트를 탐색하고 테스트할 수 있는 대화형 웹 인터페이스를 제공할 것입니다.

We have decided to use the OpenAPI 3 specification to define our REST APIs. We will integrate a library such as `springdoc-openapi` into our Spring Boot project to automatically generate the OpenAPI specification from our Java controller code and annotations. This specification will be exposed via a URL, and we will use Swagger UI to provide an interactive web interface for exploring and testing the API endpoints.

## 결과 Consequences

- **긍정적:**
- API 문서가 실제 코드와 항상 동기화되어, 문서가 뒤쳐지는 문제를 줄입니다.
- Swagger UI는 API 사용자가 브라우저에서 직접 엔드포인트를 테스트할 수 있는 대화형 환경을 제공하여 개발자 경험을 향상시킵니다.
- OpenAPI 명세는 클라이언트 SDK를 생성하는 데 사용될 수 있어 프론트엔드 및 모바일 팀의 개발을 더욱 가속화할 수 있습니다.
- API에 대한 명확한 계약을 제공합니다.

- **고려사항:**
- 개발자는 컨트롤러 메서드에 필요한 어노테이션을 배우고 일관되게 적용해야 합니다.
- 생성된 문서의 품질은 개발자가 제공하는 어노테이션과 설명에 따라 달라집니다. 의미 있는 설명을 작성하기 위한 규율이 필요합니다.
- 프로젝트에 새로운 의존성이 추가됩니다.

- **Positive:**
- API documentation is always in sync with the actual code, reducing documentation drift.
- Swagger UI provides an interactive environment where API consumers can test endpoints directly in the browser, improving the developer experience.
- The OpenAPI specification can be used to generate client SDKs, further accelerating development for frontend and mobile teams.
- Provides a clear contract for the API.

- **Considerations:**
- Requires developers to learn and consistently apply the necessary annotations to their controller methods.
- The quality of the generated documentation depends on the annotations and descriptions provided by the developers. Discipline is required to write meaningful descriptions.
- Adds a new dependency to the project.
