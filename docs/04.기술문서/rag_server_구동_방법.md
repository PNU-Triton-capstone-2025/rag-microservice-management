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
