from settings import settings, Provider
from prompt_template import prompts

from langchain_community.embeddings import OpenAIEmbeddings
from langchain_community.chat_models import ChatOpenAI
from langchain_community.chat_models import ChatAnthropic
from langchain_google_genai import ChatGoogleGenerativeAI

from langchain_community.vectorstores import ElasticsearchStore

def get_embedding_model():
    return OpenAIEmbeddings()
    
def get_chat_llm(provider, llm):
    if provider == Provider.openai.value:
        return ChatOpenAI(model_name = llm)

    if provider == Provider.anthropic.value:
        return ChatAnthropic(model = llm)

    if provider == Provider.gemini.value:
        return ChatGoogleGenerativeAI(model = llm)
    
def get_vectorstore(index_name, embedding_model):
    vectorstore = ElasticsearchStore(
        es_url=settings.elasticsearch_url,
        index_name=index_name,
        embedding=embedding_model
    )
    return vectorstore

def get_prompt_tmpl(query_type):
    prompt_tmpl = prompts.get(query_type)
    return prompt_tmpl