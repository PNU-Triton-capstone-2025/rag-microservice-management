from flask import Flask, request, jsonify
from embedding import embed_and_store
from chain_query import query_rag
from settings import settings

app = Flask(__name__)

@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok", "message": "Service is running"}), 200

#ElasticSearch의 index에 해당하는 문서를 활용하여 RAG 기반 답변 생성하는 api
@app.route("/api/get-rag-response", methods=["POST"])
def get_rag_response():
    data = request.json
    query = data.get("query")
    es_index = data.get("es_index")
    query_type = data.get("query_type")
    provider = data.get("provider")
    model = data.get("model")

    if not query:
        return jsonify({"error": "query is empty."}), 400

    try:
        response_data = query_rag(query, es_index, query_type, provider, model)
        return jsonify(response_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
#문서 embedding 후 ElasticSearch에 저장하는 api
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

if __name__ == "__main__":
    #init_indices()
    app.run(host="0.0.0.0", port=5000, debug=True)