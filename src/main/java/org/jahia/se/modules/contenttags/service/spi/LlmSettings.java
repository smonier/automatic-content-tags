package org.jahia.se.modules.contenttags.service.spi;

/**
 * Immutable connection settings for one LLM provider, resolved from the module's
 * OSGi configuration (PID {@code org.jahia.se.modules.contenttags}) at call time.
 *
 * @param apiKey      the provider API key; never logged
 * @param baseUrl     the provider API base URL without a trailing slash (e.g. {@code https://api.anthropic.com})
 * @param model       the model identifier to request
 * @param maxTokens   the maximum number of tokens the provider may generate
 * @param temperature the sampling temperature, or {@code null} to omit it from the request
 *                    (recent Anthropic and OpenAI models reject non-default temperatures)
 */
public record LlmSettings(String apiKey, String baseUrl, String model, int maxTokens, Double temperature) {

    /**
     * @return true when an API key is configured for this provider
     */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "LlmSettings[apiKey=" + (hasApiKey() ? "***" : "<empty>") + ", baseUrl=" + baseUrl
                + ", model=" + model + ", maxTokens=" + maxTokens + ", temperature=" + temperature + "]";
    }
}
