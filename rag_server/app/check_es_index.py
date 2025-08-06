from elasticsearch import Elasticsearch

def ensure_index_exists(es: Elasticsearch, es_index: str, dims: int = 1536):
    if not es.indices.exists(index=es_index):
        print(f"Creating index: {es_index}")
        es.indices.create(
            index=es_index,
            body={
                "mappings": {
                    "properties": {
                        "text": {"type": "text"},
                        "vector": {
                            "type": "dense_vector",
                            "dims": dims
                        },
                        "metadata": {
                            "properties": {
                                "file_name": {"type": "keyword"},
                                "content_type": {"type": "keyword"}
                            }
                        }
                    }
                }
            }
        )
        print("index created.")
    print("index already existed.")
    
    