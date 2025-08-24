# RAG Microservice Management

## API 명세

### **사용자(User) API**

**베이스 경로**: `/api/users`

| 기능               | HTTP Method | 엔드포인트                         | 요청                                                                                                                | 응답                                                                   |
| :--------------- | :---------: | :---------------------------- | :---------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------- |
| 일반 회원가입          |    `POST`   | `/register`                   | Body: `{ "username": "newUser", "password": "password123", "api_keys": { "OPENAI": "sk-...", "GEMINI": "..." } }` | **201 Created** Body: `{ "id": 1, "username": "newUser" }`           |
| 로그인              |    `POST`   | `/login`                      | Body: `{ "username": "testuser", "password": "password123" }`                                                     | **200 OK** Body: `{ "access_token": "...", "refresh_token": "..." }` |
| 토큰 재발급           |    `POST`   | `/refresh`                    | Body: `{ "refresh_token": "eyJhbGciOiJI..." }`                                                                    | **200 OK** Body: `{ "access_token": "...", "refresh_token": "..." }` |
| LLM API 키 유효성 검사 |    `POST`   | `/validate-api-key`           | Body: `{ "provider": "OPENAI", "api_key": "sk-..." }`                                                             | **200 OK** Body: `"valid"`                                           |
| LLM API 키 조회  |    `GET`    | `/me/api-key?provider=OPENAI` | Query: `provider` (예: OPENAI, GEMINI, CLAUDE)                                                                     | **200 OK** Body: `{ "openai": "sk-..." }`                            |
| 회원 탈퇴            |   `DELETE`  | `/me`                         | Body: `{ "password": "password123" }`                                                                             | **204 No Content**                                                   |
| 비밀번호 변경          |   `PATCH`   | `/me/password`                | Body: `{ "curr_password": "current_password", "new_password": "new_strong_password" }`                            | **204 No Content**                                                   |
| LLM API 키 변경     |   `PATCH`   | `/me/api-key`                 | Body: `{ "provider": "OPENAI", "new_api_key": "sk-newkey..." }`                                                   | **204 No Content**                                                   |
---

### **프로젝트(Project) API**
**베이스 경로**: `/api/projects`

| 기능 | HTTP Method | 엔드포인트 | 요청 JSON 예시 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 내 프로젝트 목록 조회 | `GET` | `/` | (없음) | **200 OK** Body: `json [  {    "id": 1,    "name": "Project A",    "ssh_ip_address": "192.168.1.100",    "created_at": "2025-08-17T10:30:00"  }]` |
| 프로젝트 생성 | `POST` | `/` | ```json {  "name": "My New Project", "ssh_info": {    "ssh_ip_address": "192.168.1.10",    "username": "ubuntu",    "pem_file": null  }}``` | **201 Created** |

---

### **프라이빗 데이터(Private Data) API**
**베이스 경로**: `/api/projects/{projectId}/private-data`

| 기능 | HTTP Method | 엔드포인트 | 요청 파라미터 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 프라이빗 데이터 목록 조회 | `GET` | `/` | **Path**: `projectId` | **200 OK** Body: `[{ "id": 1, "project_id": 1, "filename": "doc.txt", "content_type": "text/plain", "created_at": "..." }]` |
| ZIP 업로드 & 저장 | `POST` | `/upload` | **Path**: `projectId` **Form-Data**: `file` | **200 OK** Body: `{ "message": "...", "saved_filenames": [{ "filename": "a.txt", "reason": "OK" }], "skipped_filenames": [] }` |
| 프라이빗 데이터 삭제 | `DELETE` | `/{id}` | **Path**: `projectId`, `id` | **204 No Content** |

---

### **RAG API**
**베이스 경로**: `/api/projects/{projectId}/rag`

| 기능 | HTTP Method | 엔드포인트 | 요청 파라미터 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 채팅 페이지 데이터 조회 | `GET` | `/` | **Path**: `projectId` | **200 OK** Body: `{ "project": {    "id": 1,    "name": "Project A",    "ssh_ip_address": "192.168.1.100",    "created_at": "2025-08-17T10:30:00"  }, "history": [{ "id": 1, "user_query": "...", "llm_response": "...", "created_at": "..." }] }` |
| 채팅 스트림(SSE) | `GET` | `/stream` | **Path**: `projectId` **Query**: `query` | **200 OK** `text/event-stream` Body: `{ "user_query": "...", "response": "..." }` 스트림 |

---

### **RAG 히스토리 API**
**베이스 경로**: `/api/projects/{projectId}/rag/history`

| 기능 | HTTP Method | 엔드포인트 | 요청 파라미터 / JSON                                                                                                                         | 응답                                                                                                                             |
| :--------- | :---------: | :------------- | :------------------------------------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------- |
| 히스토리 목록 조회 |    `GET`    | `/`            | **Path**: `projectId`                                                                                                                  | **200 OK** Body:<br>`[{ "id": 1, "title": "content_title", "user_query": "...", "llm_response": "...", "created_at": "..." }]` |
| 히스토리 단건 조회 |    `GET`    | `/{historyId}` | **Path**: `projectId`, `historyId`                                                                                                     | **200 OK** Body:<br>`{ "id": 1, "title": "content_title", "user_query": "...", "llm_response": "...", "created_at": "..." }`   |
| 히스토리 저장 |    `POST`   | `/`            | **Path**: `projectId`<br>**Body JSON**:<br>`json { "title": "content_title", "user_query": "사용자 질문", "llm_response": "프론트에서 생성된 답변" }` | **201 Created** Body:<br>`{ "id": 123 }`                                                                                       |
| 히스토리 삭제 |   `DELETE`  | `/{historyId}` | **Path**: `projectId`, `historyId`                                                                                                     | **204 No Content**                                                                                                             |


---

### **SSH 연결 API**
**베이스 경로**: `/api/ssh`

| 기능 | HTTP Method | 엔드포인트 | 요청 파라미터 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 프로젝트 SSH 세션 생성 | `POST` | `/connect/{projectId}` | **Path**: `projectId` | **200 OK** Body: `{ "sessionId": "..." }` |

---

### **배포(Deploy) API**
**베이스 경로**: `/api/projects/{projectId}/deploy`

| 기능 | HTTP Method | 엔드포인트 | 요청 JSON 예시 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 설정 파일 다운로드 | `POST` | `/download-config` | ```json{  "namespace": "logging",  "logstash_port": 5044}``` | **200 OK** Body: ZIP 파일 |

### **모니터링(Monitoring) API**

**베이스 경로**: `/api/projects/{projectId}/monitoring`

| 기능 | HTTP Method | 엔드포인트 | 요청 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 로그 분석 모델 조회 | `GET` | `/endpoint` | **Path**: `projectId` | **200 OK** Body: `{ "provider": "OPENAI", "model": "GPT_4" }` |
| 로그 분석 모델 수정 | `PUT` | `/endpoint` | **Path**: `projectId`\<br\>**Body**: `{ "provider": "GOOGLE", "model": "GEMINI_PRO" }` | **204 No Content** |
| 모니터링 이력 조회 | `GET` | `/` | **Path**: `projectId`\<br\>**Query**: `page`, `size`, `sort` | **200 OK** Body: (페이지네이션된 MonitoringHistoryResponseDto 목록) |
| 모니터링 이력 삭제 | `DELETE` | `/{monitoringHistoryId}` | **Path**: `projectId`, `monitoringHistoryId` | **204 No Content** |


### **RAG 서버 로그 분석 요청 API 형태**

**베이스 경로**: (RAG 서버 주소)

| 기능 | HTTP Method | 엔드포인트 | 요청 JSON 예시 | 응답 |
| :--- | :---: | :--- | :--- | :--- |
| 로그 분석 및 리포트 생성 | `POST` | `/api/get-rag-response` | **Body**:`json { "es_index": "project-1-logs-*", "provider": "OPENAI", "model": "GPT_4", "query": "분석할 로그 프롬프트..." }` | **200 OK** **Body**:`json { "title": "로그 분석 리포트 제목", "llm_response": "LLM이 생성한 분석 결과..." }` |

## RAG server 구동 방법

### 1. .env에 API_KEY 입력
`OPENAI_API_KEY = "YOUR API KEY"`

### 2. python 가상환경 생성
`python -m venv ./<venv>`

### 3. 가상환경 activate

<table>
  <tr>
    <td>Platform</td>
    <td>Shell</td>
    <td>Command to activate virtual environment</td>
  </tr>
  <tr>
    <th rowspan="4">POSIX</th>
    <td>bash/zsh</td>
    <td><code>$ source <venv>/bin/activate</code></td>
  </tr>
  <tr>
    <td>fish</td>
    <td><code>$ source <venv>/bin/activate.fish</code></td>
  </tr>
  <tr>
    <td>csh/tcsh</td>
    <td><code>$ source <venv>/bin/activate.csh</code></td>
  </tr>
  <tr>
    <td>pwsh</td>
    <td><code>$ <venv>/bin/Activate.ps1</code></td>
  </tr>
  <tr>
    <th rowspan="2">Windows</th>
    <td>cmd.exe</td>
    <td><code>C:\> <venv>\Scripts\activate.bat</code></td>
  </tr>
  <tr>
    <td>PwerShell</td>
    <td><code>PS C:\> <venv>\Scripts\Activate.ps1</code></td>
  </tr>
</table>

### 4. requirements 설치
`pip install -r requirements.txt`

### 5. Run Server
`python main.py`
