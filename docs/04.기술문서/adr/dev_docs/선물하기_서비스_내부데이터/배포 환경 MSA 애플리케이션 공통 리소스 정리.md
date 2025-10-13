## 배포 환경 MSA 애플리케이션 공통 리소스 정의 - ConfigMap

- **ConfigMap 이름**: `gift-config`
  - `API_GATEWAY_URI`: `http://api-gateway-svc:8080`
  - `SERVICE_USER_URI`: `http://user-service-svc:8084`
  - `SERVICE_PRODUCT_URI`: `http://product-service-svc:8081`
  - `SERVICE_WISH_URI`: `http://wish-service-svc:8082`
  - `SERVICE_ORDER_URI`: `http://order-service-svc:8083`
  - `KAKAO_REDIRECT_URI`: `http://localhost:8080/members/login/oauth2/code/kakao`
  - `FRONT_DOMAIN`: `http://localhost:8080`