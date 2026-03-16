# 5. Use ULID for Entity Identifiers

Date: 2025-08-23

## 상태 Status

Accepted

## 배경 Context

데이터베이스의 엔티티마다 고유한 식별자(Primary Key)를 생성하는 전략이 필요합니다. 일반적으로 Auto-incrementing integers나 UUID가 사용됩니다. Auto-incrementing 방식은 구현이 간단하지만, 데이터베이스에 종속적이며 분산 환경에서 충돌 가능성이 있고, 외부로 노출될 경우 데이터의 규모를 추측할 수 있게 합니다. UUID는 분산 환경에서 고유성을 보장하지만, 시간순으로 정렬되지 않아 데이터베이스 인덱싱에 비효율적일 수 있습니다.

We need a strategy for generating unique identifiers (Primary Keys) for entities in our database. Common choices include auto-incrementing integers and UUIDs. The auto-incrementing approach is simple to implement but is database-dependent, can cause collisions in a distributed environment, and can reveal the scale of data if exposed externally. UUIDs guarantee uniqueness in distributed systems but are not time-sortable, which can be inefficient for database indexing.

## 결정 Decision

우리는 모든 엔티티의 기본 식별자로 ULID(Universally Unique Lexicographically Sortable Identifier)를 사용하기로 결정합니다. ULID는 UUID처럼 전역적으로 고유한 값을 생성하면서도, 시간순으로 정렬이 가능하도록 설계되었습니다. 처음 48비트는 타임스탬프, 나머지 80비트는 임의의 값으로 구성되어 고유성과 정렬 가능성을 모두 만족시킵니다.

- Java 진영에서 가장 유명한 ULID 라이브러리인 com.github.f4b6a3:ulid-creator:5.2.3 를 사용한다.

We have decided to use ULID (Universally Unique Lexicographically Sortable Identifier) as the primary identifier for all entities. ULIDs are designed to be globally unique, like UUIDs, but are also lexicographically sortable by their generation time. The first 48 bits are a timestamp, and the remaining 80 bits are a random value, satisfying both uniqueness and sortability.

## 결과 Consequences

- **긍정적:**
  - 분산된 환경에서도 중앙 관리 없이 안전하게 식별자를 생성할 수 있습니다.
  - 시간순으로 정렬이 가능하여, 최신 데이터를 조회하는 등 시간 기반 쿼리의 데이터베이스 인덱스 성능이 향상됩니다.
  - URL-safe하며, UUID보다 문자열 표현이 짧습니다.
  - 순차적인 ID가 아니므로 외부 노출 시 내부 데이터 규모를 파악하기 어렵습니다.

- **고려사항:**
  - UUID에 비해 상대적으로 덜 알려져 있어, Java에서 안정적이고 잘 관리되는 라이브러리를 선택해야 합니다.


- **Positive:**
  - Identifiers can be safely generated in a distributed environment without a central authority.
  - Being time-sortable improves database index performance for time-based queries, such as fetching the latest data.
  - They are URL-safe and have a shorter string representation than UUIDs.
  - As non-sequential IDs, they do not reveal internal data scale if exposed.

- **Considerations:**
  - It is less common than UUID, so we must select a stable and well-maintained library for Java.
