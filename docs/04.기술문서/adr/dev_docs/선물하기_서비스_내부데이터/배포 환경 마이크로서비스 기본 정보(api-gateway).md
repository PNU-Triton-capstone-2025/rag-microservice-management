## 배포 환경 마이크로서비스 기본 정보 (api-gateway)

-   **컨테이너 이미지 및 버전 정보**: `sk124590/api-gateway:2.1`
-   **마이크로서비스 포트**: `8080`
-   **환경 변수**:
    -   **ConfigMap (gift-config)**
        -   `SERVICE_USER_URI`
        -   `SERVICE_PRODUCT_URI`
        -   `SERVICE_WISH_URI`
        -   `SERVICE_ORDER_URI`
    -   **Secret (gift-secret)**
        -   `JWT_SECRET`
-   **리소스 설정**:
    -   requests: `cpu: "100m", memory: "256Mi"`
    -   limits: `cpu: "500m", memory: "512Mi"`
-   **복제 수 (Replicas)**: 1
