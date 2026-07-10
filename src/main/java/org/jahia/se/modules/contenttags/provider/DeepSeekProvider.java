package org.jahia.se.modules.contenttags.provider;

import org.jahia.se.modules.contenttags.service.spi.LlmProvider;
import org.osgi.service.component.annotations.Component;

/**
 * {@link LlmProvider} implementation for the DeepSeek API, which is
 * OpenAI-Chat-Completions compatible.
 */
@Component(service = LlmProvider.class, immediate = true)
public class DeepSeekProvider extends OpenAiCompatibleProvider {

    static final String NAME = "deepseek";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected String maxTokensField() {
        return "max_tokens";
    }
}
