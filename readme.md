## API 명세

### View 컨트롤러

| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 설명 |
| :--- | :--- | :--- | :--- |
| 메인 페이지 | `GET` | `/` | `index.html` 뷰를 반환합니다. |
| 로그인 페이지 | `GET` | `/login` | `login.html` 뷰를 반환합니다. |
| 회원가입 페이지 | `GET` | `/register` | `register.html` 뷰를 반환합니다. |
| 회원가입 처리 | `POST` | `/register` | 폼 데이터를 받아 회원가입을 처리하고 성공 시 로그인 페이지로 리다이렉트합니다. |

-----

### User API

| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| 회원가입 | `POST` | `/api/users/register` | Body: `UserRegistrationDto` | **201 Created**\<br\>Body: `UserResponseDto`\<br\>\<br\>**400 Bad Request**\<br\>Body: `Map<String, String>` (유효성 검증 실패 시 에러 메시지) |
| 회원 탈퇴 | `DELETE` | `/api/users/me` | Body: `UserDeleteRequestDto` (선택 사항) | **204 No Content** |
| 비밀번호 변경 | `PATCH` | `/api/users/me/password` | Body: `ChangePasswordRequestDto` | **204 No Content** |
| LLM API 키 변경 | `PATCH` | `/api/users/me/api-key` | Body: `ChangeApiKeyRequest` | **204 No Content** |

-----

### Project API

| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| 프로젝트 목록 조회 | `GET` | `/api/projects` | Header: `Authorization` (인증 토큰) | **200 OK**\<br\>Body: `List<ProjectResponseDto>` |
| 프로젝트 생성 | `POST` | `/api/projects` | Header: `Authorization` (인증 토큰)\<br\>Body: `ProjectCreateRequestDto` | **201 Created** |

-----

### Private Data API

| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| 데이터 목록 조회 | `GET` | `/api/projects/{projectId}/private-data` | Path: `projectId` | **200 OK**\<br\>Body: `List<PrivateDataResponseDto>` |
| 데이터 파일 업로드 | `POST` | `/api/projects/{projectId}/private-data/upload` | Path: `projectId`\<br\>Form-Data: `file` (MultipartFile) | **200 OK**\<br\>Body: `UploadResultDto` |
| 데이터 삭제 | `DELETE` | `/api/projects/{projectId}/private-data/{id}` | Path: `projectId`, `id` | **204 No Content** |

-----

### RAG (채팅) API

| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| 채팅 페이지 정보 조회 | `GET` | `/api/projects/{projectId}/rag` | Path: `projectId` | **200 OK**\<br\>Body: `ChatPageResponseDto` (프로젝트 정보 및 전체 채팅 내역 포함) |
| 채팅 응답 스트리밍 | `GET` | `/api/projects/{projectId}/rag/stream` | Path: `projectId`\<br\>Query: `query` (사용자 질문) | **200 OK**\<br\>Content-Type: `text/event-stream`\<br\>Body: `Flux<String>` (실시간 스트리밍 텍스트) |


### RAG History API


| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| 채팅 기록 목록 조회 | `GET` | `/api/projects/{projectId}/rag/history` | Path: `projectId` | **200 OK**\<br\>Body: `List<RagHistoryResponseDto>` |
| 채팅 기록 상세 조회 | `GET` | `/api/projects/{projectId}/rag/history/{historyId}` | Path: `projectId`, `historyId` | **200 OK**\<br\>Body: `RagHistoryResponseDto` |
| 채팅 기록 삭제 | `DELETE` | `/api/projects/{projectId}/rag/history/{historyId}` | Path: `projectId`, `historyId` | **204 No Content** |


### SSH API


| 기능 | HTTP Method | 엔드포인트 (Endpoint) | 요청 (Request) | 응답 (Response) |
| :--- | :--- | :--- | :--- | :--- |
| SSH 세션 연결 | `POST` | `/api/ssh/connect/{projectId}` | Path: `projectId` | **200 OK**\<br\>Body: `Map<String, String>` (예: `{"sessionId": "some-session-id"}`) |


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
