## 배포 환경 마이크로서비스 기본 정보 (gift-app)

-   **컨테이너 이미지 및 버전 정보**: `sk124590/gift-app:2.1`
-   **마이크로서비스 포트**: `8085`
-   **환경 변수**:
    -   **ConfigMap (gift-config)**
        -   `API_GATEWAY_URI`
        -   `KAKAO_REDIRECT_URI`
    -   **Secret (gift-secret)**
        -   `JWT_SECRET`
        -   `KAKAO_CLIENT_ID`
        -   `KAKAO_CLIENT_SECRET`
-   **리소스 설정**:
    -   requests: `cpu: "100m", memory: "512Mi"`
    -   limits: `cpu: "500m", memory: "1Gi"`
-   **복제 수 (Replicas)**: 1
