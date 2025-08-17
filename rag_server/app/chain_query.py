import chain_components
from langchain.chains import RetrievalQA
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler

def query_rag(question: str, index_name: str, query_type: str, provider: str, llm: str) -> dict:
    rag_chain = create_rag_chain(index_name, query_type, provider, llm)
    callbacks = [StreamingStdOutCallbackHandler()]
    
    result = rag_chain.invoke(
        {"query": question},
        config={"callbacks": callbacks}
    )
    
    #question: 사용자 질문, answer: RAG 기반 답변, sources: 참조한 문서
    return {
        "question": question,
        "answer": result["result"],
        "sources": [doc.page_content for doc in result["source_documents"]],
        "log": f"response was created by {llm} of {provider}"
    }

#해당 index를 참조하는 chain 생성
def create_rag_chain(index_name: str, query_type: str, provider: str, llm: str):
    
    # 1. 임베딩(OpenAI 고정)
    embedding_model = chain_components.get_embedding_model()
    
    # 2. 벡터스토어
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever()
    
    # 3. LLM 선택
    chat_llm = chain_components.get_chat_llm(provider, llm)
    
    # 4. 프롬프트
    prompt_tmpl = chain_components.get_prompt_tmpl(query_type)
    
    # 5. 체인
    rag_chain = RetrievalQA.from_chain_type(
        llm=chat_llm,
        retriever=retriever,
        return_source_documents=True,
        chain_type="stuff",
        chain_type_kwargs={"prompt": prompt_tmpl},
    )
    return rag_chain