# RAG 체인 핵심 요소 생성 함수 모음

> 핵심 요소인 임베팅 모델, LLM 모델, 벡터 저장소 등을 생성하는 함수를 모아놓은 곳.

```python

from settings import settings, Provider
from prompt_template import prompts
from langchain_community.embeddings import OpenAIEmbeddings
from langchain_community.chat_models import ChatOpenAI, ChatAnthropic
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_community.vectorstores import ElasticsearchStore

# 임베딩 모델 생성 함수
def get_embedding_model():
    return OpenAIEmbeddings()

# LLM 제공자와 모델 이름에 맞는 LLM 객체 생성
def get_chat_llm(provider, llm, api_key):
    if provider == Provider.openai.value:
        return ChatOpenAI(model_name=llm, openai_api_key=api_key)
    if provider == Provider.anthropic.value:
        return ChatAnthropic(model=llm, anthropic_api_key=api_key)
    if provider == Provider.gemini.value:
        return ChatGoogleGenerativeAI(model=llm, google_api_key=api_key)

def get_vectorstore(index_name, embedding_model):
    return ElasticsearchStore(
        es_url=settings.elasticsearch_url,
        index_name=index_name,
        embedding=embedding_model
    )

# 쿼리 타입에 맞는 프롬프트 템플릿 가져오기
def get_prompt_tmpl(query_type):
    return prompts.get(query_type)
```

- chain_query.py 파일의 코드를 간결하게 유지하기 위해 RAG 구성 요소 동적으로 생성해서 제공한다.