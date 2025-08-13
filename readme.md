## API 명세


### 사용자(User) API (`/api/users`)

| 기능           | HTTP Method | 엔드포인트                    | 요청 파라미터(타입/위치)                                                                                                                                                                 | 응답                                                                        |
| :----------- | :---------: | :----------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------ |
| 일반 회원가입      |    `POST`   | `/api/users/register`    | **Body(JSON)** (DTO: `UserRegistrationDto`)<br>• `username`: string (필수)<br>• `password`: string (필수)<br>• `apiKeys`: object\<string(enum LlmProvider) → string(API Key)> (선택) | **201 Created**<br>Body: `UserResponseDto { id: long, username: string }` |
| 회원 탈퇴        |   `DELETE`  | `/api/users/me`          | **Body(JSON)** (DTO: `UserDeleteRequestDto`)<br>• `password`: string (필수)                                                                                                      | **204 No Content**                                                        |
| 비밀번호 변경      |   `PATCH`   | `/api/users/me/password` | **Body(JSON)** (DTO: `ChangePasswordRequestDto`)<br>• `currPassword`: string (필수)<br>• `newPassword`: string (필수)                                                              | **204 No Content**                                                        |
| LLM API 키 변경 |   `PATCH`   | `/api/users/me/api-key`  | **Body(JSON)** (DTO: `ChangeApiKeyRequest`)<br>• `provider`: string(enum LlmProvider) (필수)<br>• `newApiKey`: string (필수)                                                       | **204 No Content**                                                        |

---

### 프로젝트(Project) API (`/api/projects`)

| 기능           | HTTP Method | 엔드포인트           | 요청 파라미터(타입/위치)                                                                                                                                                                                                                                             | 응답                                                                    |
| :----------- | :---------: | :-------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------- |
| 내 프로젝트 목록 조회 |    `GET`    | `/api/projects` | (없음)                                                                                                                                                                                                                                                       | **200 OK**<br>Body: `ProjectResponseDto[] { id: long, name: string }` |
| 프로젝트 생성      |    `POST`   | `/api/projects` | **Body(JSON)** (DTO: `ProjectCreateRequestDto`)<br>• `name`: string (필수)<br>• `sshInfoCreateRequestDto`: object (선택, DTO: `SshInfoCreateRequestDto`)<br>  • `sshIpAddress`: string (선택)<br>  • `username`: string (선택)<br>  • `pemFile`: null (※ JSON만 수신) | **201 Created**                                                       |

---

### 프라이빗 데이터(Private Data) API

베이스 경로: `/api/projects/{projectId}/private-data`

| 기능             | HTTP Method | 엔드포인트                                           | 요청 파라미터(타입/위치)                                                         | 응답                                                                                                                                      |
| :------------- | :---------: | :---------------------------------------------- | :--------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------- |
| 프라이빗 데이터 목록 조회 |    `GET`    | `/api/projects/{projectId}/private-data`        | **Path** `projectId`: long                                             | **200 OK**<br>Body: `PrivateDataResponseDto[] { id: long, projectId: long, filename: string, contentType: string, createdAt: instant }` |
| ZIP 업로드 & 저장   |    `POST`   | `/api/projects/{projectId}/private-data/upload` | **Path** `projectId`: long<br>**Form-Data** `file`: MultipartFile(ZIP) | **200 OK**<br>Body: `UploadResultDto { message: string, savedFilenames: string[], skippedFilenames: string[] }`                         |
| 프라이빗 데이터 삭제    |   `DELETE`  | `/api/projects/{projectId}/private-data/{id}`   | **Path** `projectId`: long, `id`: long                                 | **204 No Content**                                                                                                                      |

---

### RAG API

베이스 경로: `/api/projects/{projectId}/rag`

| 기능            | HTTP Method | 엔드포인트                                  | 요청 파라미터(타입/위치)                                          | 응답                                                                                                          |
| :------------ | :---------: | :------------------------------------- | :------------------------------------------------------ | :---------------------------------------------------------------------------------------------------------- |
| 채팅 페이지 데이터 조회 |    `GET`    | `/api/projects/{projectId}/rag`        | **Path** `projectId`: long                              | **200 OK**<br>Body: `ChatPageResponseDto { project: ProjectResponseDto, history: RagHistoryResponseDto[] }` |
| 채팅 스트림(SSE)   |    `GET`    | `/api/projects/{projectId}/rag/stream` | **Path** `projectId`: long<br>**Query** `query`: string | **200 OK**<br>`text/event-stream` (Body: `RagResponseDto` 스트림)                                              |

---

### RAG 히스토리 API

베이스 경로: `/api/projects/{projectId}/rag/history`

| 기능         | HTTP Method | 엔드포인트                                               | 요청 파라미터(타입/위치)                                | 응답                                                                                                                                     |
| :--------- | :---------: | :-------------------------------------------------- | :-------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| 히스토리 목록 조회 |    `GET`    | `/api/projects/{projectId}/rag/history`             | **Path** `projectId`: long                    | **200 OK**<br>Body: `RagHistoryResponseDto[] { id: long, title: string, userQuery: string, llmResponse: string, createdAt: datetime }` |
| 히스토리 단건 조회 |    `GET`    | `/api/projects/{projectId}/rag/history/{historyId}` | **Path** `projectId`: long, `historyId`: long | **200 OK**<br>Body: `RagHistoryResponseDto`                                                                                            |
| 히스토리 삭제    |   `DELETE`  | `/api/projects/{projectId}/rag/history/{historyId}` | **Path** `projectId`: long, `historyId`: long | **204 No Content**                                                                                                                     |

---

### SSH 연결 API (`/api/ssh`)

| 기능             | HTTP Method | 엔드포인트                          | 요청 파라미터(타입/위치)             | 응답                                          |
| :------------- | :---------: | :----------------------------- | :------------------------- | :------------------------------------------ |
| 프로젝트 SSH 세션 생성 |    `POST`   | `/api/ssh/connect/{projectId}` | **Path** `projectId`: long | **200 OK**<br>Body: `{ sessionId: string }` |

---


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
