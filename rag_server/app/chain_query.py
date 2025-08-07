from langchain_community.vectorstores import ElasticsearchStore
from langchain_community.embeddings import OpenAIEmbeddings
from langchain_community.chat_models import ChatOpenAI
from langchain.chains import RetrievalQA
from settings import settings
from prompt_template import prompts

def query_rag(question: str, index_name: str, query_type: str) -> dict:
    rag_chain = create_rag_chain(index_name, query_type)
    result = rag_chain.run(question)
    
    #question: 사용자 질문, answer: RAG 기반 답변, sources: 참조한 문서
    return {
        "question": question,
        "answer": result["result"],
        "sources": [doc.page_content for doc in result["source_documents"]]
    }

#해당 index를 참조하는 chain 생성
def create_rag_chain(index_name: str, query_type: str):
    embedding_model = OpenAIEmbeddings()
    vectorstore = ElasticsearchStore(
        es_url=settings.elasticsearch_url,
        index_name=index_name,
        embedding=embedding_model
    )
    retriever = vectorstore.as_retriever()
    llm = ChatOpenAI(temperature=0)

    rag_chain = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=retriever,
        return_source_documents=True,
        chain_type="stuff",
        chain_type_kwargs={"prompt": prompts.get(query_type)}
    )
    return rag_chain