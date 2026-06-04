# msa-payment

> Spring Boot와 Spring Cloud 기반으로 구성한 **MSA 학습용 프레임워크 프로젝트**입니다.  
> 서비스 디스커버리, API Gateway, Config Server, 서비스 간 Feign 통신, Redis/Redisson 기반 동시성 제어, Kafka 기반 이벤트 처리, 결제 흐름의 Saga/Outbox 패턴을 실험하기 위한 구조를 갖고 있습니다.

---

## 1. 프로젝트 개요

이 프로젝트는 단일 모놀리식 애플리케이션이 아니라 여러 개의 독립 서비스를 조합한 MSA 구조입니다.

주요 목적은 다음과 같습니다.

- Spring Cloud 기반 MSA 기본 구성 학습
- Gateway → Service 라우팅 구조 구성
- Eureka 기반 서비스 등록/탐색
- Config Server 기반 중앙 설정 관리
- OpenFeign 기반 내부 서비스 통신
- Redis/Redisson 기반 재고 동시성 제어
- Kafka 이벤트 기반 비동기 처리
- 주문/결제/재고 정합성을 위한 Saga Orchestration 흐름 실험
- 결제 실패/보류/타임아웃 상황에서 보상 트랜잭션 흐름 설계

---

## 2. 전체 아키텍처

```mermaid
flowchart LR
    Client[Client / Postman / Frontend] --> Gateway[Gateway Server - Spring Cloud Gateway]

    Gateway --> Eureka[Eureka Server - Service Discovery]
    Gateway --> Order[Order Service]
    Gateway --> Item[Item Service]
    Gateway --> Payment[Payment Service]
    Gateway --> User[User Service]

    Config[Config Server<br/>Centralized Config] --> Gateway
    Config --> Order
    Config --> Item
    Config --> Payment
    Config --> User
    Config --> Eureka

    Order -->|OpenFeign| Item
    Order -->|OpenFeign| Payment

    Order --> OrderDB[(MySQL - order_service)]
    Item --> ItemDB[(MySQL - item_service)]
    Payment --> PaymentDB[(MySQL - payment_service)]

    Item --> Redis[(Redis / Redisson - Distributed Lock)]
    Order --> Kafka[(Kafka - Domain Events)]
    Item --> Kafka
    Payment --> Kafka

    Kafka --> Consumers[Event Consumers - Compensation / Payment Check]
```

---

## 3. 서비스 구성

| 서비스 | 역할 | 핵심 포인트 |
|---|---|---|
| `config-server` | 중앙 설정 서버 | Spring Cloud Config Server 기반 설정 관리 |
| `eureka-server` | 서비스 디스커버리 | 각 서비스가 Eureka에 등록되고 Gateway/Feign이 탐색 |
| `gateway-server` | API Gateway | 외부 요청 진입점, WebFlux 기반 Gateway, LoadBalancer/Eureka 연동 |
| `user-service` | 사용자 서비스 | 사용자 도메인 확장용 서비스 |
| `item-service` | 상품/재고 서비스 | 상품 CRUD, 재고 증감, Redisson Lock 기반 동시성 제어 |
| `order-service` | 주문 서비스 | 주문 생성/조회, 결제 확인 흐름의 Saga Orchestrator 역할 |
| `payment-service` | 결제 서비스 | 결제 생성/조회/승인 결과 저장, 외부 PG 연동 확장 지점 |

---

## 4. 기술 스택

### Backend

- Java 17
- Spring Boot 3.5.x
- Spring Cloud 2025.0.1
- Spring Web / WebFlux Gateway
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka
- Spring Cloud Config
- Spring Kafka
- Spring Validation
- Spring Actuator

### Database / Infra

- MySQL
- Redis
- Redisson
- Kafka
- Eureka Server
- Config Server

### Logging / Observability

- Logback
- Logstash Logback Encoder
- Trace ID 기반 로그 추적 확장 가능 구조

---

## 5. 디렉터리 구조

```text
MSA-Frame
├── config-server
├── eureka-server
├── gateway-server
├── item-service
├── order-service
├── payment-service
├── user-service
└── README.md
```

각 서비스는 독립적인 Gradle 프로젝트로 구성되어 있으며, 서비스별로 `src/main/java`, `src/main/resources`, `build.gradle.kts`를 갖습니다.

---

## 6. 서비스별 내부 구조

### 6.1 Gateway Server

```text
gateway-server
└── src/main/java/app/backend/gatewayserver
    ├── GatewayServerApplication.java
    └── global
        ├── constant
        └── filter
```

Gateway는 외부 요청의 단일 진입점입니다.  
Eureka와 연동하면 `lb://SERVICE-NAME` 방식으로 서비스 라우팅을 구성할 수 있습니다.

---

### 6.2 Item Service

```text
item-service
└── src/main/java/app/backend/itemservice
    ├── ItemServiceApplication.java
    ├── global
    │   ├── aop/lock
    │   ├── constant
    │   ├── error
    │   ├── filter
    │   ├── manager
    │   ├── response
    │   └── util
    ├── infrastructure
    │   ├── kafka
    │   └── redis
    └── item
        ├── controller
        │   ├── external
        │   └── internal
        ├── dto
        ├── entity
        ├── exception
        ├── repository
        └── service
```

Item Service는 상품 정보와 재고를 관리합니다.

핵심 기능은 다음과 같습니다.

- 상품 생성
- 상품 조회
- 상품 수정
- 상품 삭제
- 재고 증가/차감
- Redisson 분산 락 기반 재고 변경 동시성 제어

재고 변경 메서드에는 다음과 같은 형태의 커스텀 락이 적용되어 있습니다.

```java
@CustomLock(
    key = "'Item:' + #itemId",
    waitTime = 3000,
    leaseTime = 10000
)
```

이를 통해 같은 상품의 재고를 동시에 변경하는 요청이 들어와도 Redis 기반 락으로 임계 구역을 보호할 수 있습니다.

---

### 6.3 Order Service

```text
order-service
└── src/main/java/app/backend/orderservice
    ├── OrderServiceApplication.java
    ├── global
    ├── infrastructure
    │   ├── client
    │   │   ├── config
    │   │   ├── constants
    │   │   ├── dto
    │   │   ├── error
    │   │   ├── item
    │   │   └── payment
    │   ├── kafka
    │   │   ├── config
    │   │   ├── constants
    │   │   ├── event
    │   │   ├── message
    │   │   ├── outbox
    │   │   └── util
    │   └── redis
    └── order
        ├── controller
        ├── dto
        ├── entity
        ├── event
        ├── exception
        ├── orchestrator
        ├── repository
        └── service
```

Order Service는 이 프로젝트에서 가장 중요한 서비스입니다.  
주문 도메인을 관리하면서 결제/재고 정합성을 맞추기 위한 **Saga Orchestrator** 역할을 수행합니다.

핵심 기능은 다음과 같습니다.

- 주문 생성
- 주문 단건 조회
- 결제 확인 요청 처리
- Item Service 재고 차감 요청
- Payment Service 결제 승인 요청
- 결제 성공 시 주문 확정
- 결제 실패 시 주문 거절 및 재고 복구 이벤트 발행
- 결제 상태 불명확 시 주문 보류 및 결제 상태 확인 이벤트 발행

---

### 6.4 Payment Service

```text
payment-service
└── src/main/java/app/backend/paymentservice
    ├── PaymentServiceApplication.java
    ├── global
    ├── infrastructure
    │   ├── client/payment
    │   └── kafka
    └── payment
        ├── controller
        │   ├── external
        │   └── internal
        ├── dto/response
        ├── entity
        ├── exception
        ├── repository
        └── service
```

Payment Service는 결제 정보를 생성하고 결제 승인 결과를 저장합니다.

주요 엔티티인 `Payment`는 다음과 같은 정보를 관리합니다.

- 주문 ID
- 주문 번호
- 결제 키
- 결제 수단
- 카드 번호
- 승인 번호
- 영수증 URL
- 결제 금액
- 승인 시간
- 취소 사유
- 결제 상태
- 취소 시간

---

## 7. 핵심 비즈니스 흐름

### 7.1 주문 생성 흐름

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Gateway as Gateway Server
    participant Order as Order Service
    participant Item as Item Service
    participant OrderDB as Order DB

    User->>Gateway: POST /api/v1/orders
    Gateway->>Order: 주문 생성 요청 전달
    Order->>Item: 상품 정보 조회
    Item-->>Order: 상품 정보 응답
    Order->>Order: 재고 부족 여부 검증
    Order->>OrderDB: 주문 저장(status = CREATED)
    Order-->>Gateway: 주문 생성 결과
    Gateway-->>User: 주문 생성 응답
```

주문 생성 단계에서는 실제 재고를 바로 차감하지 않고, 상품 조회와 재고 가능 여부를 확인한 뒤 주문을 `CREATED` 상태로 저장합니다.

---

### 7.2 결제 확인 Saga 흐름

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Gateway as Gateway Server
    participant Order as Order Service<br/>Saga Orchestrator
    participant Item as Item Service
    participant Payment as Payment Service
    participant Kafka as Kafka / Outbox
    participant OrderDB as Order DB

    User->>Gateway: POST /api/v1/orders/{orderId}/payment/confirmation
    Gateway->>Order: 결제 확인 요청
    Order->>OrderDB: 주문 조회
    Order->>Item: 재고 차감 요청
    Item-->>Order: 재고 차감 성공

    Order->>Payment: 결제 승인 요청
    Payment-->>Order: 결제 상태 응답

    alt 결제 성공 DONE
        Order->>OrderDB: 주문 확정(status = CONFIRMED)
        Order-->>Gateway: 결제 성공 응답
    else 결제 실패 ABORTED
        Order->>OrderDB: 주문 거절(status = REJECTED)
        Order->>Kafka: 재고 복구 이벤트 발행
        Order-->>Gateway: 결제 실패 응답
    else 결제 보류/상태 불명확
        Order->>OrderDB: 주문 보류(status = PENDING)
        Order->>Kafka: 결제 상태 확인 이벤트 발행
        Order-->>Gateway: 결제 보류 응답
    end
```

Order Service는 결제 흐름에서 중앙 조정자 역할을 합니다.  
Item Service와 Payment Service에 직접 요청을 보내고, 실패/보류 상황에 따라 이벤트를 발행합니다.

---

## 8. 주문 상태 모델

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> CONFIRMED: 결제 성공
    CREATED --> REJECTED: 결제 실패 / 재고 차감 실패
    CREATED --> PENDING: 결제 상태 불명확
    PENDING --> CONFIRMED: 결제 상태 재확인 성공
    PENDING --> REJECTED: 결제 상태 재확인 실패
    REJECTED --> CANCELLED: 취소 처리 확장
    CONFIRMED --> CANCELLED: 주문 취소 확장
```

현재 주문 상태는 다음과 같이 구성됩니다.

| 상태 | 의미 |
|---|---|
| `CREATED` | 주문 생성 완료 |
| `PENDING` | 결제 결과가 명확하지 않아 확인 대기 |
| `CONFIRMED` | 결제 성공 및 주문 확정 |
| `REJECTED` | 결제 실패 또는 재고 차감 실패 |
| `CANCELLED` | 주문 취소 |

---

## 9. 이벤트 기반 보상 처리

이 프로젝트는 결제 실패/보류 상황에서 직접 모든 처리를 동기적으로 끝내지 않고, 이벤트를 통해 후속 작업을 분리하는 구조를 갖습니다.

### 주요 이벤트

| 이벤트 | 발행 주체 | 목적 |
|---|---|---|
| `ORDER_ITEM_RELEASE_REQUESTED` | Order Service | 결제 실패 또는 주문 거절 시 Item Service에 재고 복구 요청 |
| `PAYMENT_CHECK_REQUIRED` | Order Service | 결제 상태가 불명확한 경우 Payment Service 또는 후속 워커가 결제 상태를 재조회하도록 요청 |

```mermaid
flowchart TD
    A[Payment Confirm 요청] --> B{Payment 결과}

    B -->|DONE| C[Order CONFIRMED]
    B -->|ABORTED| D[Order REJECTED]
    D --> E[ORDER_ITEM_RELEASE_REQUESTED 이벤트 발행]
    E --> F[Item 재고 복구]

    B -->|Timeout / Unknown / Pending| G[Order PENDING]
    G --> H[PAYMENT_CHECK_REQUIRED 이벤트 발행]
    H --> I[PG 상태 재조회]
    I --> J{최종 결제 상태}
    J -->|성공| K[Order CONFIRMED]
    J -->|실패| L[Order REJECTED]
    L --> M[재고 복구 이벤트 발행]
```

---

## 10. Outbox 패턴 의도

Order Service에는 Kafka 이벤트와 Outbox 관련 패키지가 존재합니다.

```text
order-service
└── infrastructure
    └── kafka
        ├── event
        ├── message
        ├── outbox
        └── util
```

Outbox 패턴의 목적은 다음과 같습니다.

1. DB 상태 변경과 이벤트 발행 의도를 같은 트랜잭션 경계 안에서 기록한다.
2. 메시지 브로커 장애로 인해 비즈니스 상태와 이벤트 발행 상태가 어긋나는 문제를 줄인다.
3. Consumer는 처리 이력을 기록하여 중복 메시지를 방어한다.
4. 결제/재고 같은 정합성이 중요한 흐름에서 장애 복구 지점을 명확히 만든다.

```mermaid
flowchart LR
    Service[Order Service] --> Tx[DB Transaction]
    Tx --> Domain[Order 상태 변경]
    Tx --> Outbox[Outbox Event 저장]

    Outbox --> Publisher[Outbox Publisher / CDC / Worker]
    Publisher --> Kafka[Kafka Topic]
    Kafka --> Consumer[Consumer Service]
    Consumer --> Processed[Processed Message 저장]
```

---

## 11. 내부 통신 구조

Order Service는 OpenFeign 기반 Adapter를 통해 Item Service와 Payment Service를 호출합니다.

```mermaid
flowchart LR
    OrderController[OrderController] --> Orchestrator[OrderOrchestrator]
    Orchestrator --> OrderService[OrderService]
    Orchestrator --> ItemAdapter[ItemAdapter]
    Orchestrator --> PaymentAdapter[PaymentAdapter]

    ItemAdapter -->|Feign| ItemService[Item Service]
    PaymentAdapter -->|Feign| PaymentService[Payment Service]
```

이 구조의 장점은 다음과 같습니다.

- Controller가 외부 서비스 통신 세부 구현을 알 필요가 없음
- Orchestrator가 비즈니스 흐름을 중앙에서 조정
- Feign 예외를 내부 도메인 예외로 변환하기 쉬움
- Item/Payment 호출 실패 시 보상 흐름을 명확히 작성 가능

---

## 12. API 예시

### Order Service

| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/v1/orders` | 주문 생성 |
| `GET` | `/api/v1/orders/{orderId}` | 주문 단건 조회 |
| `POST` | `/api/v1/orders/{orderId}/payment/confirmation` | 결제 확인 및 주문 확정/거절/보류 처리 |
| `GET` | `/api/v1/orders/test/{itemId}` | Item Service 연동 테스트용 API |

---

## 13. 실행 전 준비 사항

서비스 실행 전 다음 인프라가 필요합니다.

- MySQL
- Redis
- Kafka
- Config Server
- Eureka Server

Order Service 기준으로 확인되는 기본 설정은 다음과 같은 형태입니다.

```yaml
server:
  port: 0

spring:
  application:
    name: order-service

  config:
    import: optional:configserver:http://admin:1234@localhost:9000

eureka:
  client:
    service-url:
      defaultZone: http://admin:1234@localhost:8761/eureka
```

`server.port: 0`은 서비스 인스턴스가 랜덤 포트로 실행되도록 하며, Eureka에 등록되면 Gateway나 Feign Client가 서비스 이름 기반으로 접근할 수 있습니다.

---

## 14. 로컬 실행 순서 예시

> 실제 실행 전 각 서비스의 `application.yml` 또는 Config Server 설정을 자신의 로컬 환경에 맞게 수정해야 합니다.

```bash
# 1. Config Server 실행
cd config-server
./gradlew bootRun

# 2. Eureka Server 실행
cd ../eureka-server
./gradlew bootRun

# 3. Gateway Server 실행
cd ../gateway-server
./gradlew bootRun

# 4. 비즈니스 서비스 실행
cd ../item-service
./gradlew bootRun

cd ../payment-service
./gradlew bootRun

cd ../order-service
./gradlew bootRun

cd ../user-service
./gradlew bootRun
```

---

## 15. 프로젝트 요약

```mermaid
mindmap
  root((MSA-Frame))
    Spring Cloud
      Gateway
      Eureka
      Config Server
    Domain Services
      Order Service
      Item Service
      Payment Service
      User Service
    Consistency
      Saga Orchestration
      Compensation Event
      Outbox Pattern
    Infra
      MySQL
      Redis
      Kafka
```

---

## 16. 설계 의도: 정합성 우선 결제 흐름

이 프로젝트의 핵심은 단순히 주문, 상품, 결제 서비스를 나누는 것이 아니라 **서비스가 분리된 환경에서 주문·재고·결제 상태가 서로 어긋나지 않도록 만드는 것**입니다.

모놀리식 구조에서는 하나의 트랜잭션 안에서 주문 생성, 재고 차감, 결제 상태 저장을 처리할 수 있지만, MSA 구조에서는 각 서비스가 독립된 DB와 트랜잭션을 갖기 때문에 하나의 로컬 트랜잭션으로 전체 정합성을 보장하기 어렵습니다.

따라서 이 프로젝트에서는 다음 기준으로 설계했습니다.

- 재고 차감은 Item Service가 책임진다.
- 주문 상태 변경은 Order Service가 책임진다.
- 결제 승인 및 결제 결과 저장은 Payment Service가 책임진다.
- 전체 흐름의 조정은 Order Service의 Orchestrator가 담당한다.
- 결제 실패 또는 결과 불명확 상황은 이벤트 기반 보상 처리로 수렴시킨다.
- 동기 호출 실패와 실제 비즈니스 실패를 구분하여 처리한다.

즉, 이 프로젝트는 **분산 트랜잭션을 직접 묶는 방식이 아니라 Saga Orchestration과 보상 이벤트를 통해 최종 정합성을 맞추는 구조**를 지향합니다.

---

## 17. 재고 동시성 처리 설계

같은 상품의 재고 변경은 정합성을 위해 반드시 순차적으로 처리되어야 합니다.

예를 들어 동일한 `itemId`에 대해 100개의 결제 요청이 동시에 들어오면, 모든 요청이 동시에 같은 재고 값을 읽고 차감할 경우 Lost Update 문제가 발생할 수 있습니다.

```text
초기 재고 = 1000

요청 A: 1000 조회 → 990 저장
요청 B: 1000 조회 → 990 저장

실제로는 20개가 차감되어야 하지만 최종 재고는 990으로 남을 수 있음
```

이를 방지하기 위해 Item Service의 재고 변경 구간에는 Redis/Redisson 기반 분산 락을 적용했습니다.

```java
@CustomLock(
    key = "'Item:' + #itemId",
    waitTime = 3000,
    leaseTime = 10000
)
```

이 방식은 같은 상품에 대한 재고 변경 요청을 하나씩 처리하도록 만들어 재고 정합성을 보호합니다.

다만 이 구조는 성능 관점에서 다음과 같은 특징을 갖습니다.

- 같은 `itemId`에 대한 요청은 사실상 순차 처리된다.
- 재고 변경 임계 구역이 길어질수록 전체 응답 시간이 증가한다.
- 내부 통신 지연, DB 지연, 로그 I/O 지연이 있으면 락 대기 시간이 길어진다.
- 따라서 재고 변경 로직은 최대한 짧고 단순하게 유지해야 한다.

이 프로젝트에서는 정합성을 우선하기 위해 Redis Lock을 적용했으며, 이후 개선 방향으로는 DB Atomic Update 또는 Redis Lua Script 방식도 고려할 수 있습니다.

---

## 18. 정합성 검증 기준

동시성 테스트 이후에는 단순히 요청이 성공했는지만 보는 것이 아니라, 최종 데이터가 정합성을 만족하는지 확인해야 합니다.

가장 기본적인 재고 검증 기준은 다음과 같습니다.

```text
초기 재고 = 현재 상품 재고 + CONFIRMED 주문 수량 합 + PENDING 주문 수량 합
```

`PENDING` 주문은 아직 최종 상태가 확정되지 않은 주문이므로, 최종적으로는 `CONFIRMED` 또는 `REJECTED`로 수렴해야 합니다.

테스트 후 확인해야 할 기준은 다음과 같습니다.

- `CONFIRMED` 주문 수량 합과 실제 차감된 재고가 일치하는가?
- `REJECTED` 주문이 재고를 점유하고 있지 않은가?
- `PENDING` 주문이 남아 있다면 후속 이벤트로 수렴 가능한가?
- 결제 `DONE` 상태와 주문 `CONFIRMED` 상태가 일치하는가?
- 결제 실패 또는 승인 거절 주문의 재고가 복구되었는가?

예시 SQL은 다음과 같습니다.

```sql
select status, count(*), sum(stock)
from tbl_order
where item_id = ?
group by status;
```

```sql
select payment_status, count(*)
from payment
group by payment_status;
```

---

## 19. 동시성 테스트 결과

### 테스트 환경

- 로컬 노트북 RAM 16GB
- Spring Application
  - Config Server 3개
  - Order Service
  - Payment Service
  - Item Service
- Docker Compose
  - MySQL
  - Redis
  - Kafka
  - Debezium Connect
  - Elasticsearch
  - Kibana
  - Logstash
  - Filebeat

### 테스트 조건

- 동일한 `itemId`에 대해 100개 동시 결제 요청
- 주문 1건당 재고 10개 차감
- 내부 통신 실패 시 retry 적용
- 결제 실패/보류 상황에서 이벤트 기반 재고 복구 및 결제 상태 확인 처리

### 1차 결과: ELK 계열 포함 실행

ELasticsearch, Kibana, Logstash, Filebeat을 함께 실행한 상태에서는 100개 동시 요청 처리에 약 17초가 소요되었습니다.

이때 다수 요청이 내부 통신 retry를 모두 실패했고, 실패한 주문은 `REJECTED` 처리 후 재고 복구 이벤트를 통해 정합성을 맞췄습니다.

분석 결과, 애플리케이션 로직 자체의 정합성 문제라기보다는 로컬 환경에서 로그 수집/검색 스택까지 함께 실행하면서 CPU, 메모리, 디스크 I/O 병목이 발생한 것으로 판단했습니다.

### 2차 결과: ELK 계열 중지 후 실행

Elasticsearch, Kibana, Logstash, Filebeat을 중지한 뒤 다시 테스트한 결과, 100개 동시 요청은 약 7초 내에 처리되었습니다.

결과는 다음과 같습니다.

```json
{
  "itemId": 21,
  "totalOrderCount": 100,
  "successCount": 85,
  "successItemStock": 850,
  "rejectCount": 15,
  "rejectItemStock": 150,
  "pendingCount": 0,
  "pendingItemStock": 0,
  "itemInfo": {
    "stock": 150
  }
}
```

15건의 `REJECTED` 중 14건은 정상적인 결제 승인 거절이었고, 1건은 Payment Service 네트워크 문제로 인해 결제 상태 확인 이벤트가 발행된 뒤 최종 승인 거절을 확인하고 재고 복구가 처리되었습니다.

최종 재고 검증 결과는 다음과 같습니다.

```text
성공 주문 수량 850 + 남은 재고 150 = 초기 재고 1000
```

즉, 동시 요청과 일부 네트워크 실패 상황에서도 최종 재고 정합성은 유지되었습니다.

---

## 20. 성능 테스트 해석

이 테스트는 단순 조회 API의 처리량 테스트가 아니라 다음 작업을 포함한 End-to-End 흐름입니다.

- 주문 조회
- Item Service 재고 차감 요청
- Redis Lock 기반 동시성 제어
- Payment Service 결제 승인 요청
- 결제 결과에 따른 주문 상태 변경
- 결제 실패 시 Kafka 이벤트 발행
- Consumer의 이벤트 처리
- 재고 복구 이벤트 처리
- Trace ID 기반 로그 기록

따라서 같은 상품에 대한 100개 동시 요청에서 모든 요청이 완전히 병렬로 처리되지는 않습니다.  
같은 `itemId`의 재고 변경은 정합성을 위해 순차적으로 처리되어야 하기 때문입니다.

ELK 계열을 제거했을 때 처리 시간이 약 17초에서 약 7초로 줄어든 점을 통해, 로컬 환경에서는 애플리케이션 코드보다 인프라 리소스 경쟁이 주요 병목이었다고 볼 수 있습니다.

다만 운영 환경에서는 다음과 같은 추가 검증이 필요합니다.

- 서비스별 CPU / Memory 사용량
- HikariCP active / pending connection
- Redis Lock 대기 시간
- Feign timeout / retry 횟수
- Kafka consumer lag
- MySQL slow query
- JVM GC pause
- 로그 출력량과 디스크 I/O

---

