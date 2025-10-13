# RAG 체인 조립 및 실행

RAG의 핵심 로직을 담당한다. chain_components.py에서 부품들을 가져와 RAG체인을 조립하고, 답변을 생성한다. 일반 답변과 스트리밍 답변 처리 기능 둘 다 존재.

```python
# import ...

# 검색된 Document 리스트를 LLM 프롬프트에 넣을 문자열로 변환
def format_docs(docs):
    return "\n\n".join(f"- {d.page_content}" for d in docs)

# RAG 체인을 생성하는 함수 (일반용)

def create_rag_chain(index_name: str, query_type: str, provider: str, llm: str, api_key: str):
    # chain_components.py 함수 호출
    embedding_model = chain_components.get_embedding_model()

    vectorstore = chain_components.get_vectorstore(index_name, embedding_model)

    retriever = vectorstore.as_retriever(search_kwargs={"k": 20}) # 관련 문서 20개 검색
    chat_llm = chain_components.get_chat_llm(provider, llm, api_key)

    prompt_tmpl = chain_components.get_prompt_tmpl(query_type)

    # LangChain의 RetrievalQA를 이용해서 체인 조립
    rag_chain = RetrievalQA.from_chain_type(
        llm=chat_llm,
        retriever=retriever,
        return_source_documents=True, # 참조 문서도 결과에 포함
        chain_type="stuff", #검색된 문서를 모두 프롬프트에 넣는 방식
        chain_type_kwargs={"prompt": prompt_tmpl},
    )

    return rag_chain




# 일반적인 RAG 답변 생성 함수. YML 파일 생성이 아님.
def query_rag(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> dict:
    rag_chain = create_rag_chain(index_name, query_type, provider, llm, api_key)

    result = rag_chain.invoke({"query": query})
    answer = result["result"]

    return {
        "question": query,
        "answer": answer,
        "sources": [doc.page_content for doc in result["source_documents"]],

    }

def create_rag_chain_stream(index_name: str, query_type: str, provider: str, llm: str, api_key: str):

    #...
    # LCEL(LangChain Expression Language)을 사용해서 파이프라인 형태로 체인 정의
    rag_chain = (
        {"context": retriever | format_docs, "question": RunnablePassthrough()}
        | prompt_tmpl # 프롬프트 템플릿에 데이터 삽입
        | chat_llm # 완성된 프롬프트 LLM에 전달
        | StrOutputParser() # LLM의 출력에서 텍스트만 추출
    )

    return rag_chain


def query_rag_stream(query: str, index_name: str, query_type: str, provider: str, llm: str, api_key: str) -> Response:
    rag_chain = create_rag_chain_stream(index_name, query_type, provider, llm, api_key)

    def generate():
        for chunk in rag_chain.stream(query): # 체인을 스트리밍 모드로 실행
            if chunk:
                yield _jline({"event": "token", "data": chunk})

        yield _jline({"event": "done", "data": {"answer": ...}})

    headers = {"Content-Type": "application/jsonl; charset=utf-8", ...}
    return Response(stream_with_context(generate()), headers=headers, status=200)

```