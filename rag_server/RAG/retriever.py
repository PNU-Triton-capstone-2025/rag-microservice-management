from langchain.document_loaders import UnstructuredFileLoader
from langchain.text_splitter import CharacterTextSplitter
from langchain.vectorstores import Chroma
from langchain.embeddings import OpenAIEmbeddings


def embed_text_document(text):
    splitter = CharacterTextSplitter.from_tiktoken_encoder(
        separator="\n",
        chunk_size = 600,
        chunk_overlap = 100,
    )
    docs = splitter.create_documents([text])
    
    embeddings = OpenAIEmbeddings()
    
    #elasticSerach에 저장