```YML
# =============================================================================
# 1. Metricbeat가 사용할 권한 설정 (ServiceAccount, ClusterRole, ClusterRoleBinding)
# =============================================================================
apiVersion: v1
kind: ServiceAccount
metadata:
  name: metricbeat
  namespace: ${NAMESPACE}
  labels:
    k8s-app: metricbeat
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: metricbeat
  labels:
    k8s-app: metricbeat
rules:
- apiGroups: [""]
  resources:
  - nodes
  - namespaces
  - events
  - pods
  - services
  verbs: ["get", "list", "watch"]
- apiGroups: ["extensions", "apps"]
  resources:
  - replicasets
  - deployments
  - daemonsets
  - statefulsets
  verbs: ["get", "list", "watch"]
- apiGroups: ["coordination.k8s.io"]
  resources:
    - leases
  verbs: ["get", "create", "update"]
- apiGroups: [""]
  resources:
  - nodes/stats
  - nodes/metrics
  verbs: ["get"]
- nonResourceURLs:
  - "/metrics"
  verbs: ["get"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: metricbeat
subjects:
- kind: ServiceAccount
  name: metricbeat
  namespace: ${NAMESPACE}
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: metricbeat

---
# =============================================================================
# 2. Metricbeat 설정 파일 (ConfigMap)
# =============================================================================
apiVersion: v1
kind: ConfigMap
metadata:
  name: metricbeat-config
  namespace: ${NAMESPACE}
  labels:
    k8s-app: metricbeat
data:
  metricbeat.yml: |
    metricbeat.config.modules:
      # 모듈 설정을 외부 파일에서 동적으로 로드합니다.
      path: ${path.config}/modules.d/*.yml
      reload.enabled: false

    # 쿠버네티스 환경에서 Pod, Node 등 객체 정보를 자동으로 추가해줍니다.
    processors:
      - add_kubernetes_metadata:

    # Logstash를 거치지 않고 Elasticsearch로 직접 데이터를 전송합니다.
    # ---------------------------- Elasticsearch Output ----------------------------
    output.elasticsearch:
      # Elasticsearch 서비스의 주소를 입력해야 합니다.
      # 보통 'http://<서비스명>.<네임스페이스>.svc.cluster.local:9200' 형태입니다.
      hosts: ['http://elasticsearch-master.default.svc.cluster.local:9200']
      index: "project-${PROJECT_ID}-metrics-%{+YYYY.MM.dd}"

    # ---------------------------- Kibana ----------------------------
    # Kibana 대시보드를 자동으로 생성하기 위한 설정입니다. (필요시 주석 해제)
    # setup.kibana:
    #   host: "http://kibana.default.svc.cluster.local:5601"

---
# =============================================================================
# 3. Metricbeat 모듈 설정 (ConfigMap) - 시스템, 쿠버네티스 메트릭 활성화
# =============================================================================
apiVersion: v1
kind: ConfigMap
metadata:
  name: metricbeat-modules
  namespace: ${NAMESPACE}
  labels:
    k8s-app: metricbeat
data:
  system.yml: |
    - module: system
      period: 10s
      metricsets:
        - cpu
        - load
        - memory
        - network
        - process
        - process_summary
        - uptime
        - socket_summary
      process.include_top_n:
        by_cpu: 5
        by_memory: 5
  kubernetes.yml: |
    - module: kubernetes
      period: 10s
      metricsets:
        - node
        - system
        - pod
        - container
        - volume
      host: ${NODE_NAME}
      # kube-state-metrics가 클러스터에 배포되어 있다면 주석을 해제하여 더 풍부한 메트릭을 수집할 수 있습니다.
      # add_metadata: true
      # kube_state_metrics.hosts: ["kube-state-metrics.kube-system.svc.cluster.local:8080"]

---
# =============================================================================
# 4. Metricbeat 실행 (DaemonSet)
# =============================================================================
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: metricbeat
  namespace: ${NAMESPACE}
  labels:
    k8s-app: metricbeat
spec:
  selector:
    matchLabels:
      k8s-app: metricbeat
  template:
    metadata:
      labels:
        k8s-app: metricbeat
    spec:
      serviceAccountName: metricbeat
      terminationGracePeriodSeconds: 30
      hostNetwork: true
      dnsPolicy: ClusterFirstWithHostNet
      containers:
      - name: metricbeat
        image: docker.elastic.co/beats/metricbeat:7.17.9
        args: [
          "-c", "/etc/metricbeat.yml",
          "-e",
        ]
        env:
        - name: NODE_NAME
          valueFrom:
            fieldRef:
              fieldPath: spec.nodeName
        securityContext:
          runAsUser: 0
        volumeMounts:
        - name: config
          mountPath: /etc/metricbeat.yml
          readOnly: true
          subPath: metricbeat.yml
        - name: modules
          mountPath: /usr/share/metricbeat/modules.d
          readOnly: true
        - name: data
          mountPath: /usr/share/metricbeat/data
        - name: proc
          mountPath: /hostfs/proc
          readOnly: true
        - name: cgroup
          mountPath: /hostfs/sys/fs/cgroup
          readOnly: true
        - name: dockersock
          mountPath: /var/run/docker.sock
          readOnly: true
      volumes:
      - name: config
        configMap:
          defaultMode: 0640
          name: metricbeat-config
      - name: modules
        configMap:
          defaultMode: 0640
          name: metricbeat-modules
      - name: data
        hostPath:
          path: /var/lib/metricbeat-data
          type: DirectoryOrCreate
      - name: proc
        hostPath:
          path: /proc
      - name: cgroup
        hostPath:
          path: /sys/fs/cgroup
      - name: dockersock
        hostPath:
          path: /var/run/docker.sock

```