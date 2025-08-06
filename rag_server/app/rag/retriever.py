from langchain.text_splitter import CharacterTextSplitter
from langchain.schema import Document
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores.elasticsearch import ElasticsearchStore
from langchain.chains import RetrievalQA
from elasticsearch import Elasticsearch
from app.settings import settings

es_url = settings.elasticsearch_url
es_client = Elasticsearch(es_url)
embedding_model = OpenAIEmbeddings()

vectorstore = ElasticsearchStore(
    es_url=es_url,
    index_name=settings.elasticsearch_index,
    embedding=embedding_model,
    es_client=es_client
)

text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
    separator="\n",
    chunk_size = 600,
    chunk_overlap = 100,
)

def embed_and_store(text):   
    chunks = text_splitter.split_text(text)
    documents = [Document(page_content=chunk) for chunk in chunks]
    vectorstore.add_documents(documents)
    return len(documents)