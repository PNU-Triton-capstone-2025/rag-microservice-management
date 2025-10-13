# LLM 프롬프트 정의

- LLM에게 어떤 역할 부여하고, 어떤 형식으로 답변 생성해야 하는지 지시하는 프롬프트 템플릿 정의.

```python
from langchain.prompts import PromptTemplate

yaml_generation_prompt = PromptTempate(
    input_variables=["context", "question"], # {context}, {question} 사용

    template="""
        당신은 클라우드 애플리케이션 배포에 능숙한 Kubernetes 전문가입니다.
        아래의 문맥과 사용자 요구사항을 기반으로 ... YAML 명세를 생성하세요.

        문맥:
        {context}  # 이곳에 Elasticsearch에서 찾아온 문서 내용이 들어갑니다.

        요구사항:
        {question} # 이곳에 사용자의 질문이 들어갑니다.
    """
)


prompts = {
    "yaml_generation": yaml_generation_prompt,
    "yaml_edit": yaml_edit_prompt,
    "msa_k8s": msa_k8s_prompt,
    "log_analyze": log_analyze_prompt,
    "resource_setting": resource_setting_prompt,
}

```