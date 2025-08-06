from langchain.text_splitter import CharacterTextSplitter
from langchain.schema import Document
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores.elasticsearch import ElasticsearchStore
from elasticsearch import Elasticsearch
from settings import settings

#
es_url = settings.elasticsearch_url
es_client = Elasticsearch(es_url)
embedding_model = OpenAIEmbeddings()

text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
    separator="\n",
    chunk_size = 600,
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