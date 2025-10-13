## user-service

| 에러 로그  | 원인    | 해결 방안   |
| ------------------- | ------------------------------ | ------------------------------------- |
| `"카카오 Token 요청 API 호출 실패. 상태 코드: {}"` | 카카오 API 통신 설정 오류 (Secret 또는 ConfigMap 값 오류 가능성) | 1. `gift-secret` Secret의 `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET` 값이 올바른지 확인 <br>2. `gift-config` ConfigMap의 `KAKAO_REDIRECT_URI` 값 확인 <br>3. user-service Deployment에서 `gift-secret`, `gift-config`가 올바르게 마운트되어 있는지 확인 |