## 배포 환경 마이크로서비스 기본 정보 (user-service)

-   **컨테이너 이미지 및 버전 정보**: `sk124590/user-service:2.1`
-   **마이크로서비스 포트**: `8084`
-   **환경 변수**:
    -   **Secret (gift-secret)**
        -   `JWT_SECRET`
        -   `KAKAO_CLIENT_ID`
        -   `KAKAO_CLIENT_SECRET`
    -   **ConfigMap (gift-config)**
        -   `KAKAO_REDIRECT_URI`
-   **리소스 설정**:
    -   requests: `cpu: "150m", memory: "512Mi"`
    -   limits: `cpu: "1000m", memory: "1Gi"`
-   **복제 수 (Replicas)**: 1

