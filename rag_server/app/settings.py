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

class Settings(BaseSettings):
    # API Keys
    openai_api_key: Optional[str] = None
    anthropic_api_key: Optional[str] = None
    google_api_key: Optional[str] = None        # Gemini
    
    # 기타 필수 설정
    elasticsearch_url: str
    langsmith_api_key: str
    langsmith_project: str
    
    # .env에서 값 읽어옴
    model_config = SettingsConfigDict(
        env_file=env_path, 
        extra="ignore"
    )
    
    # LangChain 라이브러리에서 API 키 인식하도록 환경 변수 등록
    def apply_to_environ(self):
        # OpenAI
        if self.openai_api_key:
            os.environ["OPENAI_API_KEY"] = self.openai_api_key

        # Anthropic
        if self.anthropic_api_key:
            os.environ["ANTHROPIC_API_KEY"] = self.anthropic_api_key

        # Google Gemini
        if self.google_api_key:
            os.environ["GOOGLE_API_KEY"] = self.google_api_key
            
        # ElasticSearch & LangSmith
        os.environ["ELASTICSEARCH_URL"] = self.elasticsearch_url
        os.environ["LANGCHAIN_API_KEY"] = self.langsmith_api_key
        os.environ["LANGCHAIN_PROJECT"] = self.langsmith_project
        os.environ["LANGCHAIN_TRACING_V2"] = "true"

# 생성된 인스턴스의 설정 값 실제 환경 변수에 적용
settings = Settings()
settings.apply_to_environ()