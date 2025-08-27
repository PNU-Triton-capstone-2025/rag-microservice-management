import os
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_anthropic import ChatAnthropic
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_elasticsearch import ElasticsearchStore
from langchain.prompts import PromptTemplate
from prompt_template import DEFAULT_PROMPT_TEMPLATE, RESOURCE_PROMPT_TEMPLATE, LOG_PROMPT_TEMPLATE

# --- Embedding ---
def get_embedding_model():
    return OpenAIEmbeddings()

# --- LLMs ---
def get_chat_llm(provider: str, llm: str):
    if provider == "openai":
        return ChatOpenAI(model_name=llm, streaming=True, temperature=0)
    elif provider == "anthropic":
        return ChatAnthropic(model_name=llm, streaming=True, temperature=0)
    elif provider == "gemini":
        return ChatGoogleGenerativeAI(model=llm, streaming=True, temperature=0)
    else:
        raise ValueError(f"Unsupported LLM provider: {provider}")

# --- Vector Store ---
def get_vectorstore(index_name: str, embedding_model):
    return ElasticsearchStore(
        es_url=os.environ.get("ELASTICSEARCH_URL"),
        index_name=index_name,
        embedding=embedding_model
    )

# --- Prompt Templates ---
def get_prompt_tmpl(query_type: str):
    if query_type == "resource":
        template = RESOURCE_PROMPT_TEMPLATE
    elif query_type == "log":
        template = LOG_PROMPT_TEMPLATE
    else: # default
        template = DEFAULT_PROMPT_TEMPLATE
    return PromptTemplate.from_template(template)