# 문서 저장 및 삭제

> 텍스트를 받아서 벡터로 변환하고 Elasticsearch에 저장하거나, 저장된 문서를 삭제하는 기능을 수행한다.

```python
from langchain.text_splitter import CharacterTextSplitter
from langchain.schema import Document
from langchain_openai import OpenAIEmbeddings
from langchain_community.vectorstores import ElasticsearchStore
from elasticsearch import Elasticsearch
from settings import settings

# 설정 파일에서 Elasticsearch 주소 가져오기

es_url = settings.elasticsearch_url
es_client = Elasticsearch(es_url)

# 텍스트를 벡터로 변환할 OpenAI 임베딩 모델
embedding_model = OpenAIEmbeddings()

# 긴 텍스트를 적절한 크기로 자를 Text Splitter 준비

# chunk_size=600을 넘지 않도록 하나의 청크 생성
text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
    separator="\n", # 줄바꿈 문자 기준으로 자르기
    chunk_size=600, # 최대 600 토큰 크기로 자름
    chunk_overlap=100, # 조각 사이에 100 토큰 겹치게 해서 문맥 유지
)

# 텍스트 임베딩해서 Elasticsearch에 저장하는 함수
def embed_and_store(text, es_index, metadata):
    chunks = text_splitter.split_text(text)

    # Langchain의 Document 객체로 변환
    documents = [Document(page_content=chunk, metadata=metadata) for chunk in chunks]

    # Elasticsearch와 연결해서 벡터 저장소로 사용 설정
    vectorstore = ElasticsearchStore(
        es_url=es_url,
        index_name=es_index, # 저장할 인덱스(테이블과 유사) 이름
        embedding=embedding_model # 사용할 임베딩 모델
    )

    # Document들을 벡터로 변환해서 Elasticsearch에 저장
    vectorstore.add_documents(documents)
    # Elasticsearch에 데이터가 즉시 검색 가능하도록 새로고침
    es_client.indices.refresh(index=es_index)

    # 저장된 조각 개수 반환
    return len(documents)

def delete_by_file_name(es_index, file_name: str) -> int:
    body = {"query": {"term": {"metadata.file_name.keyword": file_name}}}
    res = es_client.delete_by_query(
        index=es_index,
        body=body,
        refresh=True # 즉시 반영
    )

    # 삭제된 문서 개수 반환
    return res.get("deleted", 0)
```

- embed_and_store: 텍스트를 받아 chunk로 나누고 각 chunk를 벡터로 변환해서 메타데이터(파일명 등)와 함께 Elasticsearch에 저장
- delete_by_file_name: 특정 파일명에 해당하는 모든 문서 조각을 Elasticsearch에서 삭제한다.