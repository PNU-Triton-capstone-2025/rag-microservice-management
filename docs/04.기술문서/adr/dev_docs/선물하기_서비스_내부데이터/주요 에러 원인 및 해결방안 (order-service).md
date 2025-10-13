## 주요 에러 원인 및 해결방안 (order-service)

| 에러 로그     | 원인    | 해결 방안   |
| --------------------- | --------------- | ------------------ |
| `"product-service에서 옵션 정보 조회 실패 - optionId: {}"`         | product-service와 직접 통신 실패 (서비스 디스커버리 문제, 주소 오류 가능성) | 1. `gift-config` ConfigMap의 `SERVICE_PRODUCT_URI`가 `product-service-svc` 내부 DNS 주소인지 확인 <br>2. order-service Deployment에서 `gift-config`를 `envFrom`으로 참조하고 있는지 확인 |
| `"주문 완료 후 위시리스트 삭제 요청 실패 - memberId: {}, productId: {}"` | wish-service 통신 실패 (주소 오류 가능성)                      | 1. `gift-config` ConfigMap의 `SERVICE_WISH_URI`가 `wish-service-svc` 내부 DNS 주소인지 확인 <br>2. order-service Deployment에서 `gift-config` 참조 여부 확인     |