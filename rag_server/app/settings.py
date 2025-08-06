from pydantic import BaseSettings
from dotenv import load_dotenv
import os

load_dotenv()

class Settings(BaseSettings):
    openai_api_key: str = os.getenv("OPENAI_API_KEY")
    elasticsearch_url: str = os.getenv("ELASTICSEARCH_URL")
    elasticsearch_index: str = os.getenv("ELASTICSEARCH_INDEX")

    class Config:
        env_file = ".env"


settings = Settings()
