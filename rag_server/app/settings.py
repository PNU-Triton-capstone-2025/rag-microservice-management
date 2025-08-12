from enum import Enum
from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict
import os
from dotenv import load_dotenv

# .env 파일 경로 설정 및 로드
env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env")
load_dotenv(dotenv_path=env_path)

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
    
    # os.environ에 LangChain 및 LangSmith용 환경변수 주입
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

# 싱글톤 인스턴스
settings = Settings()
settings.apply_to_environ()