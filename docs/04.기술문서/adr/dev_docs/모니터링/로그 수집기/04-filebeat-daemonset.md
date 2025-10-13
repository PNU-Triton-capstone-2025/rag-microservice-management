# 04-filebeat-daemonset.yml 이해하기

> Filebeat를 쿠버네티스 클러스터에서 실제로 실행시키는 핵심적인 역할을 합니다.

- DaemonSet : 클러스터의 모든 노드(Node)에 지정된 파드(Pod)를 하나씩 실행시킵니다.

```YML
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: filebeat
  namespace: logging
  labels:
    k8s-app: filebeat
```
- k8s-app: filebeat라는 label을 붙입니다.

```YML
spec:
  selector:
    matchLabels:
      k8s-app: filebeat
```
- selector: DaemonSet이 관리할 파드를 찾는 방법을 정의합니다.
- k8s-app: filebeat라는 라벨이 붙은 파드들을 자신의 관리 대상으로 인식합니다.

```YML
  template:
    metadata:
      labels:
        k8s-app: filebeat
```
- template: DaemonSet이 생성할 파드의 설계도 입니다.
- metadata.labels : 이 설계도로 만들어지는 모든 파드에는 k8s-app: filebeat라는 꼬리표가 붙습니다.

```YML
    spec:
      serviceAccountName: filebeat
      terminationGracePeriodSeconds: 30
      hostNetwork: true
      dnsPolicy: ClusterFirstWithHostNet
```
- filebeat ServiceAccount를 사용하도록 지정합니다.
- template.spec: 파드의 세부 사양 정의
- terminationGracePeriodSeconds
    - 파드가 종료 명령을 받았을 때, 30초 동안 정리할 시간을 줍니다.
- hostNetwork: true
    - 파드가 노드의 네트워크를 직접 사용하도록 설정해서 Filebeat가 노드의 정보를 쉽게 파악할 수 있게 도와줍니다.

```YML
      containers:
        - name: filebeat
          image: docker.elastic.co/beats/filebeat:7.17.9
          args: [
            "-c", "/etc/filebeat.yml",
            "-e"
          ]
          env: # NODE_NAME 환경 변수를 추가합니다.
          - name: NODE_NAME
            valueFrom:
              fieldRef:
                fieldPath: spec.nodeName
          securityContext:
            runAsUser: 0
```
- containers: 파드 안에서 실행될 컨테이너를 정의합니다.
- image: 사용할 도커 이미지를 지정합니다.
- args: 컨테이너가 시작될 때 실행할 명령어
    - /etc/filebeat.yml 설정 파일을 사용합니다. (filebeat -c /etc/filebeat.yml -e)
        - 사용할 설정 파일 경로를 지정하고, 로그를 표준 출력(콘솔)으로 내보내서 Pod 로그상에서 조회 가능하도록 합니다.
- securityContext.runAsUser: 0
    - 컨테이너를 root 사용자로 실행합니다. 
        - 호스트 노드의 로그 파일을 읽으려면 루트 권한이 필요합니다.
```YML
          volumeMounts:
            - name: config
              mountPath: /etc/filebeat.yml
              readOnly: true
              subPath: filebeat.yml
            - name: data
              mountPath: /usr/share/filebeat/data
            - name: varlibdockercontainers
              mountPath: /var/lib/docker/containers
              readOnly: true
            - name: varlog
              mountPath: /var/log
              readOnly: true
```
- volumeMounts: 아래 volumes에 정의된 저장 공간을 컨테이너 내부의 특정 경로에 마운트합니다.
    - config 볼륨을 컨테이너의 /etc/filebeat.yml 파일로 연결합니다.
    - data 볼륨을 Filebeat가 상태 정보를 저장하는 경로에 연결합니다.
    - varlibdockercontainers:
        - 호스트 노드의 컨테이너 로그 경로를 컨테이너 내부로 연결해서 다른 파드들의 로그를 읽을 수 있게 합니다.
```YML
      volumes:
        - name: config
          configMap:
            name: filebeat-config
            defaultMode: 0640
        - name: varlibdockercontainers
          hostPath:
            path: /var/lib/docker/containers
        - name: varlog
          hostPath:
            path: /var/log
        - name: data
          hostPath:
            path: /var/lib/filebeat-data
            type: DirectoryOrCreate
```
- volumes: 파드가 사용할 저장 공간의 실체를 정의합니다.
    - config 
        - 이전에 정의한 설정 파일 내용인 filebeat-config 라는 이름의 ConfigMap을 소스로 사용합니다.
    - varlibdockercontainers, varlog, data
        - hostPath 타입을 사용합니다.
        - 호스트 노드의 실제 경로를 볼륨으로 사용해서 컨테이너의 격리된 환경을 넘어서 호스트의 파일 시스템에 접근하여 로그를 수집할 수 있습니다.