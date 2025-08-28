import json, time
from datetime import datetime, date
from flask import Response, stream_with_context
from langchain.chains import RetrievalQA
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
import chain_components

def format_docs(docs):
    return "\n\n".join(f"- {d.page_content}" for d in docs)

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

def query_rag(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> dict:
    rag_chain = create_rag_chain(index_name, query_type, provider, llm, api_key)
    
    result = rag_chain.invoke(
        {"query": query},
    )
    answer = result["result"]
    
    #query: 사용자 질문, answer: RAG 기반 답변, sources: 참조한 문서
    return {
        "question": query,
        "answer": answer,
        "sources": [doc.page_content for doc in result["source_documents"]],
        "log": f"response was created by {llm} of {provider} using {query_type} template"
    }

def query_rag_stream(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> Response:
    rag_chain = create_rag_chain_stream(index_name, query_type, provider, llm, api_key)
    started_at = time.time()
    
    embedding_model = chain_components.get_embedding_model()
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever()
    
    def generate():
        # 1) 소스 먼저
        # docs = retriever.get_relevant_documents(query)
        # sources = [
        #     {
        #         "snippet": d.page_content[:200],
        #         "metadata": getattr(d, "metadata", {}),
        #     }
        #     for d in docs
        # ]
        # yield _jline({"event": "sources", "data": sources})

        # 2) 토큰 스트리밍
        answer_acc = []
        last_beat = time.time()
        for chunk in rag_chain.stream(query):
            if chunk:
                answer_acc.append(chunk)
                print(chunk)
                yield _jline({"event": "token", "data": chunk})
            if time.time() - last_beat > 15:
                yield _jline({"event": "heartbeat"})
                last_beat = time.time()

        final_answer = "".join(answer_acc)
        latency_ms = int((time.time() - started_at) * 1000)

        # 3) 완료 신호
        yield _jline({"event": "done", "data": {"answer": "".join(answer_acc)}})
        print(f"response was created by {llm} of {provider} using {query_type} template")

    headers = {
            "Content-Type": "application/jsonl; charset=utf-8",
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
            "Connection": "keep-alive",
        }
    return Response(stream_with_context(generate()), headers=headers, status=200)

#해당 index를 참조하는 chain 생성
def create_rag_chain(index_name: str, query_type: str, provider: str, llm: str, api_key: str):
    
    # 1. 임베딩(OpenAI 고정)
    embedding_model = chain_components.get_embedding_model()
    
    # 2. 벡터스토어
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever()
    
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

# 스트리밍용
def create_rag_chain_stream(index_name: str, query_type: str, provider: str, llm: str, api_key: str):
    
    # 1. 임베딩(OpenAI 고정)
    embedding_model = chain_components.get_embedding_model()
    
    # 2. 벡터스토어
    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)
    retriever = vectorstore.as_retriever()
    
    # 3. LLM 선택
    chat_llm = chain_components.get_chat_llm(provider, llm, api_key)
    
    # 4. 프롬프트
    prompt_tmpl = chain_components.get_prompt_tmpl(query_type)
    
    # 5. 체인
    rag_chain = (
        {"context": retriever | format_docs, "question": RunnablePassthrough()}
        | prompt_tmpl
        | chat_llm
        | StrOutputParser()
    )
    return rag_chain