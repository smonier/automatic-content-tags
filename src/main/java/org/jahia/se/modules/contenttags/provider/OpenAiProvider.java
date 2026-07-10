package org.jahia.se.modules.contenttags.provider;

import org.jahia.se.modules.contenttags.service.spi.LlmProvider;
import org.osgi.service.component.annotations.Component;

/**
 * {@link LlmProvider} implementation for the OpenAI Chat Completions API.
 */
@Component(service = LlmProvider.class, immediate = true)
public class OpenAiProvider extends OpenAiCompatibleProvider {

    static final String NAME = "openai";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected String maxTokensField() {
        // OpenAI deprecated max_tokens in favor of max_completion_tokens (required by gpt-5 family)
        return "max_completion_tokens";
    }
}
