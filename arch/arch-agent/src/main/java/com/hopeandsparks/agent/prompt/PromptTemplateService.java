package com.hopeandsparks.agent.prompt;

public interface PromptTemplateService {

    PromptTemplate load(String agentName);

    String render(String agentName, java.util.Map<String, Object> variables);
}
