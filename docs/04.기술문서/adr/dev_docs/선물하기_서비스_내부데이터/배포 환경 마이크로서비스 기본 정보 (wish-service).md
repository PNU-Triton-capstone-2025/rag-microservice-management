## 배포 환경 마이크로서비스 기본 정보 (wish-service)

-   **컨테이너 이미지 및 버전 정보**: `sk124590/wish-service:2.1`
-   **마이크로서비스 포트**: `8082`
-   **환경 변수**:
    -   **ConfigMap (gift-config)**
        -   `SERVICE_PRODUCT_URI`
-   **리소스 설정**:
    -   requests: `cpu: "100m", memory: "512Mi"`
    -   limits: `cpu: "700m", memory: "1Gi"`
-   **복제 수 (Replicas)**: 1
