## 배포 환경 MSA 애플리케이션 정의 - 2

-   **의존 마이크로서비스**
    -   **`gift-app`**: `api-gateway`에 의존하여 모든 백엔드 API를 호출합니다.
    -   **`api-gateway`**: `user-service`, `product-service`, `wish-service`, `order-service`로 요청을 라우팅합니다.
    -   **`order-service`**: `product-service` (재고 차감), `wish-service` (주문 후 위시리스트 삭제), `user-service` (카카오 토큰 조회)에 의존합니다.
    -   **`wish-service`**: `product-service` (상품 정보 조회)에 의존합니다.
    -   **`user-service`**, **`product-service`**: 다른 서비스에 대한 직접적인 의존성 없이 독립적으로 실행됩니다.