# 06-logstash-deployment.yml 이해하기

```YML
apiVersion: apps/v1
kind: Deployment
metadata:
  name: logstash
  namespace: logging
spec:
  replicas: 1
  selector:
    matchLabels:
      app: logstash
```
- 리소스 종류, replicas 개수를 지정합니다.
    - replicas: 1 
        - Logstash 파드 1개 실행 상태로 유지합니다.
- selector:
    - 이 Deployment가 어떤 파드를 관리할지 선택합니다.
        - app: logstash label이 붙은 파드를 찾아서 관리합니다.
```YML
  template:
    metadata:
      labels:
        app: logstash
```
- Deployment가 생성할 파드의 설계도입니다.
    - app: logstash label 지정합니다.
```YML
    spec:
      containers:
        - name: logstash
          image: docker.elastic.co/logstash/logstash:7.17.9
          ports:
            - containerPort: 5044
```
- 컨테이너 포트 지정(5044 : Filebeat로부터 데이터를 받기 위해 열어두는 포트입니다.)

```YML
          volumeMounts:
            - name: config-volume
              mountPath: /usr/share/logstash/pipeline/
            - name: config-yml
              mountPath: /usr/share/logstash/config/logstash.yml
              subPath: logstash.yml
      volumes:
        - name: config-volume
          configMap:
            name: logstash-config
        - name: config-yml
          configMap:
            name: logstash-yml-config
```
- 앞서 정의한 ConfigMap을 Logstash 컨테이너에 전달(마운트)합니다.
    - Logstash 컨테이너 시작 시 /usr/share/logstash/pipeline/ 내에 logstash-conf가 생성됩니다. logstash.yml도 마찬가지로 mountPath에 생성됩니다.


```YML
apiVersion: v1
kind: Service
metadata:
  name: logstash
  namespace: logging
spec:
  selector:
    app: logstash
  ports:
    - port: 5044
      targetPort: 5044
      protocol: TCP
```
- Service는 여러 파드에 대한 안정적인 단일 네트워크 진입점(고정 주소)를 제공합니다.
    - Deployment의 공식 주소로 이해하면 됩니다.
- metadata.name
    - 이 이름이 쿠버네티스 내부 DNS 주소의 일부가 됩니다.
        - e.g. logstash.logging.svc.cluster.local
- Deployment가 파드를 새로 만들면 파드의 IP주소는 계속 바뀌지만, Service의 주소는 항상 동일하게 유지됩니다.

- ports
    - 이 Service 자체가 5044번 포트를 외부에 노출한다. Filebeat는 이 포트를 사용합니다.
    - targetPort: 5044
        - Service의 5044번 포트로 들어온 요청을, selector로 찾은 파드의 5044번 포트로 포워딩 합니다.