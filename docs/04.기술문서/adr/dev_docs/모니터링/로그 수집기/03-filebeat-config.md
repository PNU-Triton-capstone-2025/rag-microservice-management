# 03-filebeat-config.yml 이해하기

```YML
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: logging
  labels:
    k8s-app: filebeat
data: # 실제 설정 내용이 들어가는 부분
  # filebeat.yml이라는 키에 | 아래의 문자열을 저장합니다. 
  # 이 내용은 추후 Filebeat 파드 안의 실제 /etc/filebeat.yml파일로 마운트됩니다.
  filebeat.yml: | 
    filebeat.autodiscover:
      providers:
        - type: kubernetes
          hints.enabled: true
    processors:
      - add_kubernetes_metadata:
          in_cluster: true
  
    output.logstash:
      hosts: ["logstash.logging.svc.cluster.local:5044"]
```

> filebeat.yml 내용 알아보기

1. filebeat.autodiscover (자동 탐지 설정)
- providers의 타입을 kubernetes로 지정해서, Filebeat가 쿠버네티스 클러스터의 변경사항(파드 생성/삭제 등)을 자동으로 감지하도록 합니다.
- hints.enabled: true
    - 힌트 기반 자동 탐지 활성화
    - 추후 애플리케이션 파드의 annotation에 로그 수집에 관한 힌트를 남길 수 있습니다. Filebeat는 그 힌트를 보고 자동으로 로그 수집 설정을 적용합니다.
- processors (로그 가공 설정)
    - add_kubernetes_metadata
        - 수집된 모든 로그에 쿠버네티스 메타데이터를 자동으로 추가하는 프로세서
        - Kibana에서 파드, 네임스페이스에 대한 필터링 가능

- output.logstash (출력 설정)

> ConfigMap은 업무 지시서로 생각하면 이해하기 편합니다.

> ConfigMap은 컨테이너 이미지와 설정을 분리해서, 나중에 로그 수집 방식을 변경하고 싶을 때 이미지 재빌드 없이 YML파일만 수정하면 되도록 유연성을 제공합니다.

- 쿠버네티스를 항상 지켜보다가 새로운 파드가 생기면 자동으로 로그 수집 과정을 시작합니다.
- 파드에 특별한 힌트가 있는 경우 그대로 따라 합니다.
- 수집한 모든 로그에 어느 파드, 어느 네임스페이스에서 왔는지 메타데이터를 추가합니다.
- 작업이 끝난 로그는 logstash로 전송합니다.