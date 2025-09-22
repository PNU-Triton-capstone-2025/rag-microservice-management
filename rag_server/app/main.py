from flask import Flask, request, jsonify
from datetime import datetime, timezone

from embedding import embed_and_store
from embedding import delete_by_file_name
from chain_query import query_rag
from settings import settings

app = Flask(__name__)

# health check용 API
@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "message": "Service is running"}), 200

@app.route("/", methods=["GET"])
def status():
    return jsonify({
        "service": "RAG Flask Server",
        "status": "running",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }), 200

# ElasticSearch의 index에 해당하는 문서를 활용하여 RAG 기반 답변 생성하는 API
@app.route("/api/get-rag-response", methods=["POST"])
def get_rag_response():
    data = request.json
    query = data.get("query")
    es_index = data.get("es_index")
    query_type = data.get("query_type")
    provider = data.get("provider")
    model = data.get("model")
    api_key = data.get("api_key")

    if not query:
        return jsonify({"error": "query is empty."}), 400

    try:
        response_data = query_rag(query, es_index, query_type, provider, model, api_key)
        return jsonify(response_data), 200
    except Exception as e:
        print(f"An error occurred: {e}")
        return jsonify({"error": str(e)}), 500
    
# 문서 embedding 후 ElasticSearch에 저장하는 API
@app.route("/api/embedding", methods=["POST"])
def embedding():
    data = request.json
    text = data.get("text")
    file_name = data.get("file_name")
    content_type = data.get("content_type")
    es_index = data.get("es_index")
    
    metadata = {
        "file_name": file_name,
        "content_type": content_type
    }
    
    if not text:
        return jsonify({"error": "document is empty."}), 400
    try:
        chunk_count = embed_and_store(text=text, es_index=es_index, metadata=metadata)
        return jsonify({
            "message": f"{chunk_count} chunks stored successfully in '{es_index}'"
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ES에서 문서 삭제 API
@app.route("/api/embedding/delete", methods=["POST"])
def delete_embedding():
    data = request.get_json(force=True) or {}
    es_index = data.get("es_index")
    file_name = data.get("file_name")

    if not es_index or not file_name:
        return jsonify({"error": "es_index and file_name are required"}), 400

    try:
        deleted = delete_by_file_name(es_index, file_name)
        return jsonify({"deleted": deleted}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/log-analyze", methods=["POST"])
def log_analyze():
    data = request.json
    err_log = data.get("err_log")
    es_index = data.get("es_index")
    provider = data.get("provider")
    model = data.get("model")
    api_key = data.get("api_key")
    yamls = data.get("yamls")

    query = f"다음은 MSA 애플리케이션에 발생한 에러 로그입니다. \n\n{err_log}\n\n적용된 YAML:\n{yamls}"
    query_type = "log_analyze"

    if not err_log:
        return jsonify({"error": "error log is empty."}), 400

    try:
        response_data = query_rag(query, es_index, query_type, provider, model, api_key)
        return jsonify(response_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/resource-setting", methods=["POST"])
def resource_setting():
    data = request.json
    resource_usage = data.get("resource_usage")
    es_index = data.get("es_index")
    provider = data.get("provider")
    model = data.get("model")
    api_key = data.get("api_key")
    yamls = data.get("yamls")

    query = f"다음은 현재 리소스 사용량 정보입니다. \n\n{resource_usage}\n\n적용된 YAML:\n{yamls}"
    query_type = "resource_setting"

    if not resource_usage:
        return jsonify({"error": " is empty."}), 400

    try:
        response_data = query_rag(query, es_index, query_type, provider, model, api_key)
        return jsonify(response_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)