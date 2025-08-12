from settings import settings, Provider
from prompt_template import prompts

from langchain_community.embeddings import OpenAIEmbeddings
from langchain_community.chat_models import ChatOpenAI
from langchain_community.chat_models import ChatAnthropic
from langchain_google_genai import ChatGoogleGenerativeAI

from langchain_community.vectorstores import ElasticsearchStore

from langchain.chains import RetrievalQA

def query_rag(question: str, index_name: str, query_type: str, provider: str, llm: str) -> dict:
    rag_chain = create_rag_chain(index_name, query_type, provider, llm)
    result = rag_chain(question)
    
    #question: 사용자 질문, answer: RAG 기반 답변, sources: 참조한 문서
    return {
        "question": question,
        "answer": result["result"],
        "sources": [doc.page_content for doc in result["source_documents"]],
        "log": f"response was created by {llm} of {provider}"
    }
    
def get_chat_llm(provider, llm):
    if provider == Provider.openai.value:
        return ChatOpenAI(model_name = llm)

    if provider == Provider.anthropic.value:
        return ChatAnthropic(model = llm)

    if provider == Provider.gemini.value:
        return ChatGoogleGenerativeAI(model = llm)
    

#해당 index를 참조하는 chain 생성
def create_rag_chain(index_name: str, query_type: str, provider: str, llm: str):
    
    # 1. 임베딩(OpenAI 고정)
    embedding_model = OpenAIEmbeddings()
    
    # 2. 벡터스토어
    vectorstore = ElasticsearchStore(
        es_url=settings.elasticsearch_url,
        index_name=index_name,
        embedding=embedding_model
    )
    retriever = vectorstore.as_retriever()
    
    # 3. LLM 선택
    llm = get_chat_llm(provider, llm)
    
    # 4. 프롬프트
    prompt_tmpl = prompts.get(query_type)
    
    # 5. 체인
    rag_chain = RetrievalQA.from_chain_type(
        llm=llm,
        retriever=retriever,
        return_source_documents=True,
        chain_type="stuff",
        chain_type_kwargs={"prompt": prompt_tmpl}
    )
    return rag_chain