# Blocking vs Non-Blocking, Sync vs Async

## Blocking
- 자신의 작업을 진행하다가 다른 주체의 작업이 시작되면 다른 작업이 끝날 때까지 기다렸다가 자신의 작업을 시작하는 것.

## Non-Blocking
- 다른 주체의 작업에 관련없이 자신의 작업을 수행하는 것.

> Blocking/Non-Blocking의 관점은 제어권에 있다.

## Synchronous : 동기
- 작업을 동시에 수행하거나, 동시에 끝나거나, **끝나는 동시에 시작함을 의미**

## Asynchronous : 비동기
- 시작, 종료가 일치하지 않으며, **끝나는 동시에 시작을 하지 않음을 의미**

> Synchronous, Asynchronous의 관점은 결과 처리에 관점이 있다.

> 살펴봐야 할 4가지 케이스

|   |Blocking|Non-Blocking|
|---|:---:|:---:|
|Sync|Blocking/Sync|Non-Blocking/Sync|
|Async|Blocking/Async|Non-Blocking/Async|

## Blocking/Sync

- 일반적인 동기 처리.
- 다른 함수를 호출할 경우 제어권이 다른 함수에 넘어가고, 결과 처리도 다른 함수가 완료되었을 때 수행하게 된다.

## Non-Blocking/Sync

- 다른 함수를 호출하더라도 제어권은 가진다.
- 단, 결과 처리가 완료되면 이에 대한 결과 처리를 바로 수행한다.
- e.g. 게임에서 정보 가져올 때 프로그레시브 바

## Blocking/Async
- 다른 함수를 호출할 때 제어권을 넘겨버리지만, 결과 처리를 즉각적으로 수행하지 않는다.
- 사실 장점이 딱히 없어 보인다.
- 개발자의 실수로 해당 형태를 쓰는 경우가 있다.

## Non-Blocking/Async
- 제어권도 넘기지 않고 결과를 바로 처리하지도 않는다.
- 자신의 일이 끝나면 필요할 때 처리.
- e.g. 자바스크립트 콜백