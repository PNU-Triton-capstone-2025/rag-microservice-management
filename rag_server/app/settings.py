from pydantic import BaseSettings
from dotenv import load_dotenv
import os

load_dotenv()

class Settings(BaseSettings):
    openai_api_key: str = os.getenv("OPENAI_API_KEY")
    elasticsearch_url: str = os.getenv("ELASTICSEARCH_URL")

    class Config:
        env_file = ".env"
        
#싱글톤 패턴으로 환경변수 사용
settings = Settings()
