from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    openai_api_key: str
    elasticsearch_url: str
    langsmith_api_key: str
    langsmith_project: str

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

# 싱글톤 인스턴스
settings = Settings()
