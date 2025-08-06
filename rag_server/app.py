from flask import Flask, request, jsonify
from rag import embed_and_store

app = Flask(__name__)

@app.route("/api/get-rag-response", methods=["POST"])
def get_rag_response():
    data = request.json
    query = data.get("query")

    if not query:
        return jsonify({"error": "질문이 비어 있습니다."}), 400

    #generate response
    response_text = "응답"

    return jsonify({
        "response": response_text
    }), 200
    
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
        return jsonify({"message": f"{chunk_count} chunks stored successfully in '{es_index}'",}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)