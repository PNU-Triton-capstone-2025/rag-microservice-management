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
text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
    separator="\n",
    chunk_size = 512,
    chunk_overlap = 100,
)

# 텍스트 임베딩해서 Elasticsearch에 저장하는 함수
def embed_and_store(text, es_index, metadata):
    chunks = text_splitter.split_text(text)

    # Langchain의 Document 객체로 변환
    documents = [Document(page_content=chunk, metadata=metadata) for chunk in chunks]
    
    # Elasticsearch와 연결해서 벡터 저장소로 사용 설정
    vectorstore = ElasticsearchStore(
        es_url = es_url,
        index_name = es_index,
        embedding = embedding_model
    )
    
    # Document들을 벡터로 변환해서 Elasticsearch에 저장
    vectorstore.add_documents(documents)
    es_client.indices.refresh(index=es_index)
    
    return len(documents)

# 특정 파일명에 해당하는 모든 문서 조각 Elasticsearch에서 삭제
def delete_by_file_name(es_index, file_name: str) -> int:
    body = {"query": {"term": {"metadata.file_name.keyword": file_name}}}
    res = es_client.delete_by_query(
        index=es_index,
        body=body,
        conflicts="proceed",
        refresh=True
    )
    return res.get("deleted", 0)