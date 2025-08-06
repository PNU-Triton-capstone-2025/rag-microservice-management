from langchain.vectorstores.elasticsearch import ElasticsearchStore
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.chat_models import ChatOpenAI
from langchain.chains import RetrievalQA
from settings import settings

def create_rag_chain(index_name: str):
    embedding_model = OpenAIEmbeddings()
    vectorstore = ElasticsearchStore(
        es_url=settings.elasticsearch_url,
        index_name=index_name,
        embedding=embedding_model
    )
    retriever = vectorstore.as_retriever()
    llm = ChatOpenAI()

    rag_chain = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=retriever,
        return_source_documents=True,
        chain_type="stuff"
        # 또는 refine, map_reduce 등
    )
    return rag_chain

def query_rag(question: str, index_name: str) -> dict:
    rag_chain = create_rag_chain(index_name)
    result = rag_chain(question)
    return {
        "question": question,
        "answer": result["result"],
        "sources": [doc.page_content for doc in result["source_documents"]]
    }
