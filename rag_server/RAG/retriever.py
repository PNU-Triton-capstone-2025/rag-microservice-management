import os
from dotenv import load_dotenv
from langchain.text_splitter import CharacterTextSplitter
from langchain.chat_models import ChatOpenAI
from langchain.schema import Document
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores.elasticsearch import ElasticsearchStore
from langchain.chains import RetrievalQA
from elasticsearch import Elasticsearch

load_dotenv()
es_url = os.getenv("ELASTICSEARCH_URL")
es_client = Elasticsearch(es_url)
embedding_model = OpenAIEmbeddings()

vectorstore = ElasticsearchStore(
    es_url=es_url,
    index_name="langchain-rag-index",
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