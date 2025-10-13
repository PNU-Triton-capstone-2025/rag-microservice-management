# 0. Namespace: 모든 리소스를 담을 'gift-service'라는 격리된 작업 공간을 만듭니다.
apiVersion: v1
kind: Namespace
metadata:
  name: gift-service
---
# 1. ConfigMap: 모든 서비스가 공유하는 '공개 설정'을 저장합니다.
apiVersion: v1
kind: ConfigMap
metadata:
  name: gift-config
  namespace: gift-service
data:
  # 서비스 간 통신 주소 (Kubernetes 내부 DNS 사용)
  API_GATEWAY_URI: "http://api-gateway-svc:8080"
  SERVICE_USER_URI: "http://user-service-svc:8084"
  SERVICE_PRODUCT_URI: "http://product-service-svc:8081"
  SERVICE_WISH_URI: "http://wish-service-svc:8082"
  SERVICE_ORDER_URI: "http://order-service-svc:8083"
  # 로컬 테스트를 위한 외부 주소 설정
  KAKAO_REDIRECT_URI: "http://localhost:8080/members/login/oauth2/code/kakao"
  FRONT_DOMAIN: "http://localhost:8080"
---
# 2. Secret: 암호화가 필요한 '비밀 설정'을 저장합니다.
apiVersion: v1
kind: Secret
metadata:
  name: gift-secret
  namespace: gift-service
type: Opaque
data:
  JWT_SECRET: WW4ya2ppYmRkRkFXdG5QSjJBRmxMOFdYbW9oSk1DdmlnUWdnYUV5cGE1RT0=
  KAKAO_CLIENT_ID: YWMyMGQ4NGY5N2I2NzlmMGJiZDlhODA2NWJhODIzZjM=
  KAKAO_CLIENT_SECRET: RnczSmlMNVBrcGV2WFZaOU9pVk04YmZiZ0hVUEQ1RUE=
---
# 3. Deployments: 각 서비스의 컨테이너를 어떻게 실행할지 정의합니다.

# --- API Gateway Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: sul1074/api-gateway:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# --- User Service Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector: { matchLabels: { app: user-service } }
  template:
    metadata: { labels: { app: user-service } }
    spec:
      containers:
      - name: user-service
        image: sul1074/user-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8084
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# --- Gift App (BFF) Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gift-app-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector: { matchLabels: { app: gift-app } }
  template:
    metadata: { labels: { app: gift-app } }
    spec:
      containers:
      - name: gift-app
        image: sul1074/gift-app:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8085
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# --- Product Service Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector: { matchLabels: { app: product-service } }
  template:
    metadata: { labels: { app: product-service } }
    spec:
      containers:
      - name: product-service
        image: sul1074/product-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# --- Wish Service Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wish-service-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector: { matchLabels: { app: wish-service } }
  template:
    metadata: { labels: { app: wish-service } }
    spec:
      containers:
      - name: wish-service
        image: sul1074/wish-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8082
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# --- Order Service Deployment ---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service-deployment
  namespace: gift-service
spec:
  replicas: 1
  selector: { matchLabels: { app: order-service } }
  template:
    metadata: { labels: { app: order-app } }
    spec:
      containers:
      - name: order-service
        image: sul1074/order-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8083
        envFrom:
        - configMapRef: { name: gift-config }
        - secretRef: { name: gift-secret }
---
# 4. Services: 서비스들끼리 서로를 찾을 수 있도록 '내부 전화번호부'를 만듭니다.
apiVersion: v1
kind: Service
metadata:
  name: api-gateway-svc
  namespace: gift-service
spec:
  selector: { app: api-gateway }
  ports:
  - { protocol: TCP, port: 8080, targetPort: 8080 }
---
apiVersion: v1
kind: Service
metadata:
  name: user-service-svc
  namespace: gift-service
spec:
  selector: { app: user-service }
  ports:
  - { protocol: TCP, port: 8084, targetPort: 8084 }
---
apiVersion: v1
kind: Service
metadata:
  name: gift-app-svc
  namespace: gift-service
spec:
  type: LoadBalancer 
  selector: { app: gift-app }
  ports:
  - protocol: TCP
    port: 8080          
    targetPort: 8085   
---
apiVersion: v1
kind: Service
metadata:
  name: product-service-svc
  namespace: gift-service
spec:
  selector: { app: product-service }
  ports:
  - { protocol: TCP, port: 8081, targetPort: 8081 }
---
apiVersion: v1
kind: Service
metadata:
  name: wish-service-svc
  namespace: gift-service
spec:
  selector: { app: wish-service }
  ports:
  - { protocol: TCP, port: 8082, targetPort: 8082 }
---
apiVersion: v1
kind: Service
metadata:
  name: order-service-svc
  namespace: gift-service
spec:
  selector: { app: order-service }
  ports:
  - { protocol: TCP, port: 8083, targetPort: 8083 }