# 프로젝트의 모든 설정 값 한 곳에서 관리하는 역할

```python
from enum import Enum
from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
import os
from dotenv import load_dotenv

# .env 경로 설정 & 로드
env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env")
load_dotenv(dotenv_path=env_path)

# LLM 제공자 사전 정의
class Provider(str, Enum):
    openai = "openai"
    anthropic = "anthropic"
    gemini = "gemini"

class Settings(BaseSetting):
    openapi_key: Optional[str] = None
    anthropic_api_key: Optional[str] = None
    google_api_key: Optional[str] = None

    elasticsearch_url: str
    langsmith_api_key: str
    langsmith_project: str

    model_config = SettingsConfigDict(
        env_file=env_path,
        extra="ignore"
    )

    # LangChain 라이브러리에서 API 키 인식하도록 환경 변수 등록
    def apply_to_environ(self):
        if self.openai_api_key:
            os.environ["OPENAI_API_KEY"] = self.openai_api_key

        os.environ["ELASTICSEARCH_URL"] = self.elasticsearch_url
        os.environ["LANGCHAIN_API_KEY"] = self.langsmith_api_key

# 생성된 인스턴스의 설정 값 실제 환경 변수에 적용
settings = Settings()
settings.apply_to_environ()
```

