package org.jahia.se.modules.contenttags.provider;

import org.jahia.se.modules.contenttags.service.spi.LlmProvider;
import org.jahia.se.modules.contenttags.service.spi.LlmSettings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Base implementation for providers exposing an OpenAI-compatible Chat Completions API
 * ({@code POST {baseUrl}/v1/chat/completions} with a Bearer token).
 *
 * <p>Subclasses only differ in {@link #getName()} and in the name of the output-token
 * limit field ({@link #maxTokensField()}), which OpenAI renamed to
 * {@code max_completion_tokens} while DeepSeek kept {@code max_tokens}.</p>
 */
public abstract class OpenAiCompatibleProvider implements LlmProvider {

    /**
     * @return the JSON field name carrying the output token limit for this provider
     */
    protected abstract String maxTokensField();

    @Override
    public String complete(String prompt, LlmSettings settings) throws IOException, InterruptedException {
        JSONObject body = new JSONObject()
                .put("model", settings.model())
                .put(maxTokensField(), settings.maxTokens())
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt)));
        if (settings.temperature() != null) {
            body.put("temperature", settings.temperature());
        }

        JSONObject response = HttpJson.post(settings.baseUrl() + "/v1/chat/completions",
                Map.of("Authorization", "Bearer " + settings.apiKey()), body);

        JSONArray choices = response.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            return "";
        }
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        return message == null ? "" : message.optString("content", "");
    }
}
