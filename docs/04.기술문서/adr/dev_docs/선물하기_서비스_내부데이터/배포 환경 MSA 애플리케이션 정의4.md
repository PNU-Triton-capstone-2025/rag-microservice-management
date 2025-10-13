## 배포 환경 MSA 애플리케이션 정의 - 4

-   **내부 서비스 포트 (Service Port)**
    -   `api-gateway-svc`: **8080**
    -   `user-service-svc`: **8084**
    -   `product-service-svc`: **8081**
    -   `wish-service-svc`: **8082**
    -   `order-service-svc`: **8083**
    -   `gift-app-svc`: **8080** (외부 포트) -> **8085** (내부 컨테이너 포트)

-   **외부 노출 방식**
    -   **`gift-app-svc`**: `LoadBalancer` 타입을 사용하여 외부 트래픽을 서비스의 8080 포트로 전달합니다. 이 트래픽은 `gift-app` 컨테이너의 8085 포트로 전달됩니다.
    -   **기타 모든 서비스**: `ClusterIP` 타입을 사용하여 클러스터 내부에서만 통신합니다. 외부 요청은 모두 `api-gateway`를 통해서만 내부 서비스로 전달됩니다.
