import json, time
from datetime import datetime, date
from flask import Response, stream_with_context
from langchain.chains import RetrievalQA
from langchain_core.runnables import RunnablePassthrough
from langchain_core.runnables import RunnableParallel
from langchain_core.output_parsers import StrOutputParser
from langchain_core.output_parsers import JsonOutputParser
import chain_components

def format_docs(docs):
    return "\n\n".join(
        f"[source_{i+1}]: {d.page_content}" for i, d in enumerate(docs)
    )

def _json_safe(val):
    """JSON으로 직렬화 가능한 값으로 변환"""
    if val is None or isinstance(val, (str, int, float, bool)):
        return val
    if isinstance(val, (datetime, date)):
        return val.isoformat()
    if isinstance(val, (list, tuple, set)):
        return [_json_safe(v) for v in val]
    if isinstance(val, dict):
        return {str(k): _json_safe(v) for k, v in val.items()}
    # 나머지 객체는 문자열로
    return str(val)

def _jline(obj: dict) -> bytes:
    # 혹시 남아 있는 비직렬 타입이 있어도 막아주기
    return (json.dumps(_json_safe(obj), ensure_ascii=False) + "\n").encode("utf-8")

def create_rag_chain_with_attribution(index_name: str, query_type: str, provider: str, llm: str, api_key: str):
    
    # 1. 컴포넌트 가져오기 (기존과 동일)
    embedding_model = chain_components.get_embedding_model()
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever(search_kwargs={"k": 20})
    chat_llm = chain_components.get_chat_llm(provider, llm, api_key)
    prompt_tmpl = chain_components.get_prompt_tmpl(query_type)
    
    # 2. 메인 생성 체인 정의
    # 이 체인은 'source_documents'와 'question'을 입력받아 JSON을 출력합니다.
    generation_chain = (
        {
            "context": lambda x: format_docs(x["source_documents"]),
            "question": lambda x: x["question"]
        }
        | prompt_tmpl
        | chat_llm
        | JsonOutputParser()
    )

    # 3. 최종 체인 정의 (RunnableParallel 사용)
    # retriever를 두 번 사용하여 한 번은 생성 체인으로, 한 번은 최종 출력으로 전달합니다.
    final_chain = RunnableParallel(
        llm_output= {"source_documents": retriever, "question": RunnablePassthrough()} | generation_chain,
        source_documents=retriever,
    )
    
    return final_chain

# def query_rag(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> dict:
#     rag_chain = create_rag_chain(index_name, query_type, provider, llm, api_key)
    
#     result = rag_chain.invoke(
#         {"query": query},
#     )
#     answer = result["result"]
    
#     #query: 사용자 질문, answer: RAG 기반 답변, sources: 참조한 문서
#     return {
#         "question": query,
#         "answer": answer,
#         "sources": [doc.page_content for doc in result["source_documents"]],
#         "log": f"response was created by {llm} of {provider} using {query_type} template"
#     }


def query_rag(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> dict:
    if query_type == "yaml_generation":
        rag_chain = create_rag_chain_with_attribution(index_name, query_type, provider, llm, api_key)
        
        result = rag_chain.invoke(query)

        # 중첩된 llm_output 딕셔너리를 먼저 가져옵니다.
        # .get("llm_output", {})는 llm_output이 없을 경우를 대비한 안전장치입니다.
        llm_output = result.get("llm_output", {})
        
        # llm_output 딕셔너리 안에서 'yaml'과 'attributions'를 찾습니다.
        return {
            "question": query,
            "answer": llm_output.get("yaml"), 
            "attributions": llm_output.get("attributions"),
            "sources": [doc.page_content for doc in result.get("source_documents", [])],
            "log": f"response was created by {llm} of {provider} using {query_type} template with attribution"
        }
    else:
        # 'yaml_generation'이 아닐 때의 기존 로직은 문제가 없으므로 그대로 둡니다.
        rag_chain = create_rag_chain(index_name, query_type, provider, llm, api_key)
        result = rag_chain.invoke({"query": query})
        
        return {
            "question": query,
            "answer": result.get("result"),
            "sources": [doc.page_content for doc in result.get("source_documents", [])],
            "log": f"response was created by {llm} of {provider} using {query_type} template"
        }

#해당 index를 참조하는 chain 생성
def create_rag_chain(index_name: str, query_type: str, provider: str, llm: str, api_key: str):
    
    # 1. 임베딩(OpenAI 고정)
    embedding_model = chain_components.get_embedding_model()
    
    # 2. 벡터스토어
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever(search_kwargs={"k": 20})
    
    # 3. LLM 선택
    chat_llm = chain_components.get_chat_llm(provider, llm, api_key)
    
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