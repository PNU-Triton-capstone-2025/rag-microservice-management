# Kubernetes 기본 명령어 모음

## Kubernetes 구성 요소

###  클러스터 (Cluster) 
- Kubernetes가 설치된 서버들의 전체 집합입니다.

1. 컨트롤 플레인
클러스터 전체를 관리하고 지시하는 마스터 노드입니다.

2. 워커 노드
실제 애플리케이션(파드)를 실행하는 일꾼입니다.

> Docker Desktop, Minikube 등의 환경에서는 한 대의 컴퓨터가 컨트롤 플레인과 워커 노드의 역할을 모두 수행합니다. 그러나 실제 운영 환경에서는 여러 대의 컴퓨터를 사용합니다.

### 노드 (Node)
- 클러스터에 속한 개별 서버(물리적 또는 가상 머신)입니다.

### 파드 (Pod) 
- 클러스터에서 실행되는 가장 작은 배포 단위입니다. 하나 이상의 컨테이너를 가집니다.

### 서비스 (Service)
- 여러 파드를 대표하는 고유한 접속 주소(IP)를 제공합니다. 파드가 죽고 새로 생겨도 서비스 주소는 바뀌지 않습니다.

### 디플로이먼트 (Deployment)
- 지정된 수의 파드가 항상 실행되도록 관리하고, 앱의 배포와 업데이트를 자동화합니다.

### 레플리카셋 (ReplicaSet)
- 디플로이먼트의 일부로, 지정된 수의 동일한 파드가 항상 실행되도록 보장합니다.

## kubectl 사용해서 Kubernetes 클러스터 관리하기

### 클러스터 정보 및 상태 확인

```bash
# 현재 연결된 클러스터의 마스터 노드와 서비스 주소
kubectl cluster-info
```

```bash
# 클라이언트(kubectl)와 서버(Kubernetes API)의 버전 조회
kubectl version
```

```bash
# 클러스터에 포함된 모든 노드(서버)의 목록과 상태 조회
kubectl get nodes
```

```bash
# 마스터 노드의 핵심 컴포넌트 상태 조회
kubectl get componentstatuses
```

### 리소스 조회 및 확인
> 클러스터에 배포된 리소스(파드, 서비스 등)의 상태를 확인할 때 사용합니다.

```bash
# 특정 타입의 모든 리소스 조회 (e.g. kubectl get pods, kubectl get services)
kubectl get <리소스_타입>
```

```bash
# 현재 네임스페이스의 주요 리소스(Pod, Service, Deployment 등)을 모두 보여준다.
kubectl get all 
```

```bash
# 특정 네임스페이스 파드 조회
kubectl get pods -n <네임스페이스>
```

```bash
# 모든 네임스페이스 파드 조회
kubectl get pods --all-namespaces
```

```bash
# 파드 목록 상세 정보(IP, 노드 위치 등) 조회
kubectl get pods -o wide
```

```bash
# 특정 리소스의 모든 상세 정보와 최근 이벤트 조회. 문제 해결 시 주로 사용
# e.g. kubectl describe pod my-pod
kubectl describe <리소스_타입> <이름>
```

### 리소스 생성, 수정, 삭제
> YAML 파일을 이용해서 리소스를 관리하는 명령어입니다.

```bash
# YAML 파일에 정의된 리소스를 생성하거나 변경
kubectl apply -f <파일_또는_폴더>
```

```bash
# YAML 파일에 정의된 리소스 삭제
kubectl delete -f <파일_또는_폴더>
```

```bash
# 특정 리소스 지정 삭제
kubectl delete <리소스_타입> <이름>
```

```bash
# 리소스 단순 생성
kubectl create -f <파일_이름>
```

```bash
# 실행 중인 리소스의 설정을 직접 편집기(vi)로 수정한다.
kubectl edit <리소스_타입> <이름>
```

### 파드(Pod)문제 해결 및 디버깅
> 파드가 비정상적으로 동작할 때 원인을 파악하기 위해 사용합니다.

```bash
# 특정 파드 컨테이너의 실시간 로그 출력
kubectl logs <파드_이름>
```

```bash
# 로그 스트리밍 실시간 출력 (-f : follow)
kubectl logs -f <파드_이름>
```

```bash
# CrashLoopBackOff 상태인 파드의 직전 실행 로그 확인
kubectl logs <파드_이름> previous
```

```bash
# 실행 중인 파드 컨테이너에 접속하여 명령어 실행 
# e.g. kubectl exec -it my-pod -- /bin/sh
kubectl exec -it <파드_이름> -- <명령어>
```

### 설정 및 컨텍스트 관리
> 여러 개의 k8s 클러스터를 관리할 때 사용합니다.

```bash
# 현재 설정된 모든 클러스터의 연결 정보(컨텍스트) 조회
kubectl config get-contexts
```

```bash
# 현재 사용 중인 컨텍스트 조회
kubectl config current-context
```

```bash
# 사용할 클러스터 전환
kubectl config use-context <컨텍스트_이름>
```