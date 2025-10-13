## 주요 에러 원인 및 해결방안 (wish-service)

| 에러 로그         | 원인   | 해결 방안     |
| ------------------------- | ------------------- | ------------------------------------------ |
| `"product-service에서 상품 정보 조회 실패 - productId: {}"` | product-service 주소를 찾지 못하거나 통신 실패 | 1. `gift-config` ConfigMap의 `SERVICE_PRODUCT_URI`가 `product-service-svc` 내부 DNS 주소인지 확인 <br>2. wish-service Deployment에서 `gift-config` 참조 및 환경변수 주입 여부 확인 |