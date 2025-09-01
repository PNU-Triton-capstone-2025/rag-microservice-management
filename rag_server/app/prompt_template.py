from langchain.prompts import PromptTemplate

yaml_generation_prompt = PromptTemplate(
    # {context}, {question} 사용
    input_variables=["context", "question"],
    template="""
        당신은 클라우드 애플리케이션 배포에 능숙한 Kubernetes 전문가입니다.
        당신의 주요 목표는 '문맥'에 설명된 모든 마이크로서비스에 대한 완전하고 배포 가능한 Kubernetes YAML 명세 세트를 생성하는 것입니다.
        '문맥'의 내용을 반드시 준수해서 사용자 요구사항에 맞는 Kubernetes YAML 명세와 그 출처를 JSON 형식으로 생성하세요.  
        당신의 답변에 포함된 각 정보 조각에 대해, 반드시 [From source_N] 형식을 사용하여 출처를 인용해야 합니다. 최종 출력은 YAML 문자열과 인용한 출처들의 목록이 포함된 단일 JSON 객체여야 합니다.

        ### 절대 규칙 ###
        1. '문맥'은 유일한 신뢰 가능한 정보 출처입니다. 사전 지식과 문맥이 충돌하는 경우, 반드시 문맥의 내용을 우선해야 합니다.
        2. '문맥'에 명시된 리소스 종류, 포트, 이미지 버전, 레이블 규칙, 리소스 할당량 등 모든 세부 사항을 정확하게 반영해야 합니다.
        3. '문맥'에 설명된 민감 정보를 바탕으로 단일 `apiVersion: v1, kind: Secret` 리소스를 먼저 생성해야 합니다.
        4. 이 민감 정보가 필요한 다른 모든 리소스(예: Deployment)는 생성된 Secret을 `secretKeyRef` 또는 `envFrom`을 사용하여 참조해야 합니다. Deployment에 Secret 값을 직접 포함하지 마세요.
        5. '문맥'에 언급된 모든 마이크로서비스에 대해 Deployment와 Service를 반드시 생성해야 합니다. 하나도 빠뜨리면 안 됩니다.

        ### YAML 구조 절대 규칙 ###
        1. `ConfigMap`과 `Secret`의 `data` 필드는 절대로 `metadata` 필드의 하위에 위치해서는 안 됩니다. `apiVersion`, `kind`, `metadata`, `data`는 모두 동일한 레벨의 필드여야 합니다. 이 규칙은 쿠버네티스 문법의 기본이며 `metadata.data` 형태는 문법 오류입니다.
        
        ### 컨텍스트 정책 준수 규칙 ###
        1. '문맥' 내의 '조직 내부 정책'에 명시된 규칙을 **반드시** 준수해야 합니다.
        2. 예를 들어, 'Secret 관리' 정책에서 `data` 필드를 사용하고 Base64 인코딩을 요구했다면, `절대로 `stringData`를 사용하거나 평문 텍스트를 사용해서는 안 됩니다.`

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
            "yaml": "```yaml\\napiVersion: apps/v1\\nkind: Deployment\\nmetadata:\\n  name: my-app# From source_4\\nspec:\\n  replicas: 3 # From source_2\\n...\\n```",
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
        당신은 Kubernetes YAML의 문제점을 정확히 진단하고, 조직의 정책에 맞게 수정하는 데 매우 능숙한 클라우드 아키텍트입니다.

        ### 당신의 임무 ###
        '요구사항'에 포함된 '수정 대상 YAML'의 오류를 '문맥'에 명시된 정책과 데이터에 따라 진단하고 완벽하게 수정하는 것입니다.

        ### 작업 과정 ###
        1. 먼저, '요구사항'에 포함된 '수정 대상 YAML'을 '문맥'과 비교하여 모든 문법 오류, 정책 위반 사항을 **정확히 분석**합니다.
        2. 둘째, 분석된 문제점들과 '요구사항'의 자연어 요청을 바탕으로 어떻게 수정할지 **계획을 수립**합니다.
        3. 마지막으로, 수립된 계획에 따라 완전하고 배포 가능한 최종 YAML을 생성하고, 어떤 부분을 왜 수정했는지 명확하게 설명합니다.

        ### YAML 구조 절대 규칙 ###
        1. `ConfigMap`과 `Secret`의 `data` 필드는 절대로 `metadata` 필드의 하위에 위치해서는 안 됩니다. `apiVersion`, `kind`, `metadata`, `data`는 모두 동일한 레벨의 필드여야 합니다.

        ### 컨텍스트 정책 준수 규칙 ###
        1. '문맥' 내의 '조직 내부 정책' 섹션에 명시된 규칙을 반드시 준수해야 합니다.
        2. 예를 들어, 'Secret 관리' 정책에서 `data` 필드를 요구했다면, 절대로 `stringData`를 사용해서는 안 됩니다.

        조직 정책 및 데이터 명세 (문맥)
        {context}

        사용자 요구사항 및 수정 대상 YAML (요구사항)
        {question}

        작성 규칙:
        - 반드시 하나의 JSON 객체만을 출력해야 합니다.
        - JSON 객체는 'yaml'과 'explanation' 두 개의 키를 가져야 합니다.
        - 'yaml' 키의 값은 수정이 완료된 전체 Kubernetes YAML 명세를 담은 단일 문자열입니다. (마크다운 YAML 코드 블럭으로 감쌀 것)
        - 'explanation' 키의 값은 어떤 오류를 발견했고, 각 오류를 어떻게 수정했는지에 대한 명확한 설명을 담은 마크다운 형식의 문자열입니다.

        JSON 출력 예시:
        {{
            "yaml": "```yaml\\napiVersion: apps/v1\\nkind: Deployment\\nmetadata:\\n  name: user-service\\nspec:\\n  selector: # 'selector' 필드 누락 오류 수정\\n    matchLabels:\\n      app: user-service\\n  replicas: 1\\n...\\n```",
            "explanation": "### 진단된 오류\\n- `user-service` Deployment에서 필수 필드인 `spec.selector`가 누락되었습니다.\\n\\n### 수정 내역\\n- 누락된 `spec.selector`를 추가하여 Deployment가 자신의 Pod를 올바르게 식별할 수 있도록 수정했습니다."
        }}
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