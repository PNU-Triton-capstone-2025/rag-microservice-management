from langchain.prompts import PromptTemplate

yaml_generation_prompt = PromptTemplate(
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 애플리케이션 배포에 능숙한 Kubernetes 전문가입니다.
        아래의 문맥과 사용자 요구사항을 기반으로 Kubernetes 클러스터에 배포 가능한 YAML 명세와 그 출처를 JSON 형식으로 생성하세요.

        문맥:
        {context}

        요구사항:
        {question}

        작성 규칙:
        - 반드시 하나의 JSON 객체만을 출력해야 합니다.
        - JSON 객체는 'yaml'과 'attributions' 두 개의 키를 가져야 합니다.
        - 'yaml' 키의 값은 전체 Kubernetes YAML 명세를 담은 단일 문자열입니다.
        
        - **중요: 'yaml' 문자열의 내용은 반드시 마크다운 YAML 코드 블럭(```yaml)으로 감싸야 합니다.**
        
        - 'attributions' 키의 값은 각 소스(e.g., 'source_1')가 YAML 생성에 어떻게 기여했는지를 설명하는 객체입니다.
        - 각 필드의 역할은 YAML 내에 간단한 주석으로 설명하세요.

        JSON 출력 예시:
        {{
            "yaml": "```yaml\\napiVersion: apps/v1\\nkind: Deployment\\nmetadata:\\n  name: my-app\\nspec:\\n  replicas: 3 # From source_2\\n...\\n```",
            "attributions": {{
                "source_1": "Deployment의 기본 구조와 metadata 부분을 생성하는 데 사용되었습니다.",
                "source_2": "replicas 수를 3으로 설정하는 요구사항을 반영하는 데 사용되었습니다."
            }}
        }}
    """
)

yaml_edit_prompt = PromptTemplate(
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 애플리케이션 배포에 능숙한 Kubernetes 전문가입니다.
        아래 문맥은 기존에 존재하는 Kubernetes 리소스의 YAML 정의입니다. 사용자의 요청에 따라 해당 YAML을 **정확하고 유효한 형식으로 수정**하세요.

        기존 YAML:
        {context}

        수정 요청:
        {question}

        작성 규칙:
        - 전체 결과를 **YAML 형식**으로 출력하세요.
        - 각 필드의 역할을 **간단한 주석** 으로 설명하세요.
        - 기본적인 리소스 설정을 포함하세요:
        - `replicas`, `containerPort`, `resources.limits/requests` 등
        - 필요한 경우 다음과 같은 리소스도 함께 생성하세요:
        - `Service`, `Deployment`, `ConfigMap`, `Ingress`, `PersistentVolumeClaim`
        - 생성된 YAML은 실제 배포 가능한 형식이어야 합니다.
        - 하나의 YAML 안에 여러 리소스가 필요한 경우 `---` 로 구분해서 함께 정의하세요.
    """
)

msa_k8s_prompt = PromptTemplate(
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 네이티브 아키텍처 전문가이며, 특히 마이크로서비스 아키텍처(MSA)와 Kubernetes에 정통합니다.
        아래 문맥은 관련된 기술 문서, 매뉴얼 또는 과거 질의응답에서 발췌한 정보입니다. 사용자의 질문에 대해 전문적인 기술 지식을 바탕으로 **정확하고 간결하게** 설명하세요. 필요 시 예시도 포함하세요.

        문맥:
        {context}

        질문:
        {question}

        답변:
    """
)

log_analyze_prompt = PromptTemplate(
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 네이티브 아키텍처 전문가이며, 특히 마이크로서비스 아키텍처(MSA)와 Kubernetes에 정통합니다.
        아래 문맥은 MSA에서 발생한 에러에 관한 로그입니다. 사용자의 질문에 대해 전문적인 기술 지식을 바탕으로 **정확하고 간결하게** 에러의 해결 방안을 설명하세요. 필요 시 예시도 포함하세요.

        문맥:
        {context}

        질문:
        {question}

        답변:
    """
)

resource_setting_prompt = PromptTemplate(
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 네이티브 아키텍처 전문가이며, 특히 마이크로서비스 아키텍처(MSA)와 Kubernetes에 정통합니다.
        아래 문맥은 MSA의 리소스 사용량입니다. 사용자의 질문에 대해 전문적인 기술 지식을 바탕으로 **정확하고 간결하게** 권장 리소스 사용량을 설명해세요.

        문맥:
        {context}

        질문:
        {question}

        답변:
    """
)

prompts = {
    "yaml_generation": yaml_generation_prompt,
    "yaml_edit": yaml_edit_prompt,
    "msa_k8s": msa_k8s_prompt,
    "log_analyze": log_analyze_prompt,
    "resource_setting": resource_setting_prompt,
}