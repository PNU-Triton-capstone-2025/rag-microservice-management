## 배포 환경 MSA 애플리케이션 정의 - 1

-   **서비스 설명**
    -   **서비스 이름**: 선물하기 서비스 (Gift Service)
    -   **구성 마이크로서비스 목록**:
        -   `api-gateway`: 모든 MSA 요청에 대한 인증 및 라우팅을 담당하는 관문 서비스입니다.
        -   `user-service`: 사용자 회원가입, 로그인, 정보 관리를 담당합니다.
        -   `product-service`: 상품 정보, 재고, 옵션을 관리합니다.
        -   `wish-service`: 사용자의 위시리스트를 관리합니다.
        -   `order-service`: 상품 주문 및 카카오톡 메시지 발송을 처리합니다.
        -   `gift-app`: 사용자를 위한 웹 UI를 제공하는 BFF(Backend for Frontend) 서비스입니다.