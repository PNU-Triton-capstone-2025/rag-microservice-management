from flask import Flask, request, jsonify
from RAG.retriever import embed_text_document

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
    
    if not text:
        return jsonify({"error": "문서가 비어 있습니다."}), 400
    
    #vector embedding
    embed_text_document(text)
    
    return jsonify({
        "message": "문서 임베딩 성공"
    }), 200

if __name__ == "__main__":
    app.run(debug=True)