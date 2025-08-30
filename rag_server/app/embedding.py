from langchain.text_splitter import CharacterTextSplitter
from langchain.schema import Document
from langchain_openai import OpenAIEmbeddings
from langchain_community.vectorstores import ElasticsearchStore
from elasticsearch import Elasticsearch
from settings import settings

es_url = settings.elasticsearch_url
es_client = Elasticsearch(es_url)
embedding_model = OpenAIEmbeddings()
text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
    separator="\n",
    chunk_size = 512,
    chunk_overlap = 100,
)

def embed_and_store(text, es_index, metadata):
    chunks = text_splitter.split_text(text)
    documents = [Document(page_content=chunk, metadata=metadata) for chunk in chunks]
    
    vectorstore = ElasticsearchStore(
        es_url = es_url,
        index_name = es_index,
        embedding = embedding_model
    )
    
    vectorstore.add_documents(documents)
    es_client.indices.refresh(index=es_index)
    
    return len(documents)

def delete_by_file_name(es_index, file_name: str) -> int:
    body = {"query": {"term": {"metadata.file_name.keyword": file_name}}}
    res = es_client.delete_by_query(
        index=es_index,
        body=body,
        conflicts="proceed",
        refresh=True
    )
    return res.get("deleted", 0)