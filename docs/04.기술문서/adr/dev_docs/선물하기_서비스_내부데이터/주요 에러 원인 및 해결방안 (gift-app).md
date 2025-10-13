## 주요 에러 원인 및 해결방안 (gift-app)

| 에러 로그   | 원인   | 해결 방안  |
| --------------------------- | ----------------- | --------------------------------- |
| `"user-service 호출 실패: 로그인/회원가입 요청 처리 중 오류가 발생했습니다."` | user-service 호출 실패 (게이트웨이 주소 오류, 라우팅 문제 가능성) | 1. `gift-config` ConfigMap에서 `API_GATEWAY_URI`가 `api-gateway-svc` 주소와 일치하는지 확인 |
| `"product-service 호출 실패: 상품 정보 조회 중 오류가 발생했습니다."`    | product-service 호출 실패                        | 1. `gift-config` ConfigMap에서 `API_GATEWAY_URI` 확인                              |
