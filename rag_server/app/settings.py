from pydantic_settings import BaseSettings, SettingsConfigDict
import os
from dotenv import load_dotenv

# .env 파일 경로 설정 및 로드
env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env")
load_dotenv(dotenv_path=env_path)


class Settings(BaseSettings):
    # .Settings를 .env 값으로 초기화 및 타입 검사
    openai_api_key: str
    elasticsearch_url: str
    langsmith_api_key: str
    langsmith_project: str
    
    model_config = SettingsConfigDict(env_file=env_path, extra="ignore")
    
    # os.environ에 LangChain 및 LangSmith용 환경변수 주입
    def apply_to_environ(self):
        os.environ["OPENAI_API_KEY"] = self.openai_api_key
        os.environ["ELASTICSEARCH_URL"] = self.elasticsearch_url
        os.environ["LANGCHAIN_API_KEY"] = self.langsmith_api_key
        os.environ["LANGCHAIN_PROJECT"] = self.langsmith_project
        os.environ["LANGCHAIN_TRACING_V2"] = "true"

# 싱글톤 인스턴스
settings = Settings()
settings.apply_to_environ()