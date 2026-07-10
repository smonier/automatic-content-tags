package org.jahia.se.modules.contenttags.provider;

import org.jahia.se.modules.contenttags.service.spi.LlmProvider;
import org.jahia.se.modules.contenttags.service.spi.LlmSettings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.Map;

/**
 * {@link LlmProvider} implementation for the Anthropic Messages API
 * ({@code POST {baseUrl}/v1/messages}).
 *
 * <p>Stateless and thread-safe; credentials come from {@link LlmSettings} per call.</p>
 */
@Component(service = LlmProvider.class, immediate = true)
public class AnthropicProvider implements LlmProvider {

    static final String NAME = "anthropic";
    private static final String API_VERSION = "2023-06-01";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String complete(String prompt, LlmSettings settings) throws IOException, InterruptedException {
        JSONObject body = new JSONObject()
                .put("model", settings.model())
                .put("max_tokens", settings.maxTokens())
                .put("messages", new JSONArray().put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt)));
        if (settings.temperature() != null) {
            body.put("temperature", settings.temperature());
        }

        JSONObject response = HttpJson.post(settings.baseUrl() + "/v1/messages",
                Map.of("x-api-key", settings.apiKey(), "anthropic-version", API_VERSION), body);

        StringBuilder text = new StringBuilder();
        JSONArray content = response.optJSONArray("content");
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject block = content.optJSONObject(i);
                if (block != null && "text".equals(block.optString("type"))) {
                    text.append(block.optString("text"));
                }
            }
        }
        return text.toString();
    }
}
