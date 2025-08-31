from langchain_core.documents import Document
from typing import List, Dict, Any

def map_yaml_to_sources(generated_yaml: str, source_documents: List[Document]) -> Dict[str, Any]:
    """
    생성된 YAML의 각 라인이 어떤 소스 문서를 참조했는지 매핑합니다.

    Args:
        generated_yaml (str): LLM이 생성한 YAML 텍스트.
        source_documents (List[Document]): RAG Retriever가 가져온 소스 문서 리스트.

    Returns:
        Dict[str, Any]: 매핑 정보. 
                         예: {"line_1": {"content": "apiVersion: v1", "source": "None"}, ...}
    """
    
    lines = generated_yaml.split('\n')
    attribution_map = {}

    for i, line in enumerate(lines):
        if not line.strip() or line.strip().startswith('#'):
            # 비어 있거나 주석 라인은 건너뜀
            continue

        line_number_key = f"line_{i+1}"
        best_match_source = "No specific source found" # 기본값

        # 가장 관련 있는 소스 찾기 (간단한 포함 관계 확인)
        # 키워드나 핵심 값 부분을 추출하여 비교하면 더 정확해짐
        cleaned_line = line.strip().split('#')[0].strip() # 주석 제외
        if not cleaned_line:
            continue

        for doc_index, doc in enumerate(source_documents):
            # 문서 내용에 현재 라인의 텍스트가 포함되어 있는지 확인
            if cleaned_line in doc.page_content:
                file_name = doc.metadata.get("file_name", f"Unknown_doc_{doc_index}")
                best_match_source = file_name
                break # 첫 번째 일치하는 문서를 출처로 간주

        attribution_map[line_number_key] = {
            "content": line,
            "source_document": best_match_source
        }

    return attribution_map