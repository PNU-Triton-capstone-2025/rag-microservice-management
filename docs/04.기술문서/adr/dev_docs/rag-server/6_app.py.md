# API 서버

```python

from flask import Flask, request, jsonify
# ... (내부 모듈 import)

app = Flask(__name__)

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "message": "Service is running"}), 200


@app.route("/api/get-rag-response", method=["POST"])
def get_rag_response():
    data = request.json
    query = data.get("query")

    try:
        response_data = query_rag(query, es_index, query_type, provider, model, api_key)
        return jsonify(response_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/get-rag-response-stream", methods=["POST"])
def get_rag_response_stream():
    # ...

    try:
        return query_rag_stream(query, es_index, query_type, provider, model)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# 문서 임베딩 및 저장 API
@app.route("/api/embedding", methods=["POST"])
def embedding():
    #...

    try:
        chunk_count = embed_and_store(text=text, es_index=es_index, metadata=metadata)
        return jsonify({"message": f"{chunk_count} chunks stored successfully"}),
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# 문서 삭제 API
@app.route("/api/embedding/delete", method=["POST"])
def delete_embedding():
    # ...
    
    try:
        deleted = delete_by_file_name(es_index, file_name)
        return jsonify({"deleted": deleted}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# (로그 분석, 리소스 설정 등 API들)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)

```

request.json -> 클라이언트가 보낸 JSON 읽기
jsonify(...) -> 파이썬 딕셔너리 JSON 응답으로 변환해서 클라이언트에 보내기