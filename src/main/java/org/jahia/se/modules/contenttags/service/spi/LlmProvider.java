package org.jahia.se.modules.contenttags.service.spi;

import java.io.IOException;

/**
 * Service provider interface for a chat-completion LLM backend.
 *
 * <p>Implementations are stateless OSGi Declarative Services components registered under this
 * interface. The active provider is selected at call time by
 * {@code org.jahia.se.modules.contenttags.service.internal.ContentTagsServiceImpl} from the
 * {@code llm.provider} configuration key, matched against {@link #getName()}.</p>
 *
 * <p>Threading: implementations must be safe for concurrent use; all per-request state
 * (credentials, model, limits) is passed via {@link LlmSettings}.</p>
 */
public interface LlmProvider {

    /**
     * @return the provider key used in the {@code llm.provider} configuration value
     *         (lowercase, e.g. {@code anthropic}, {@code openai}, {@code deepseek})
     */
    String getName();

    /**
     * Sends a single-turn user prompt to the provider and returns the raw assistant text.
     *
     * @param prompt   the full user prompt
     * @param settings the resolved connection settings; {@link LlmSettings#hasApiKey()} is
     *                 guaranteed true by the caller
     * @return the concatenated assistant text of the response, never null
     * @throws IOException          on transport failure or a non-2xx HTTP status
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    String complete(String prompt, LlmSettings settings) throws IOException, InterruptedException;
}
