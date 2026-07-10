package org.jahia.se.modules.contenttags.provider;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Minimal JSON-over-HTTP helper shared by the LLM providers.
 *
 * <p>Uses the JDK {@link HttpClient} so the bundle carries no HTTP client dependency.
 * A single client instance is shared; it is thread-safe.</p>
 */
final class HttpJson {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(90);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private HttpJson() {
    }

    /**
     * Posts a JSON body and returns the parsed JSON response.
     *
     * @param url     the absolute endpoint URL
     * @param headers request headers (auth, version...); Content-Type is set automatically
     * @param body    the JSON request body
     * @return the parsed response body
     * @throws IOException          on transport failure or non-2xx status; the message contains
     *                              the status code and a truncated response body, never the request
     * @throws InterruptedException if interrupted while waiting for the response
     */
    static JSONObject post(String url, Map<String, String> headers, JSONObject body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        headers.forEach(builder::header);

        HttpResponse<String> response = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String detail = response.body() == null ? "" : response.body();
            if (detail.length() > 500) {
                detail = detail.substring(0, 500) + "...";
            }
            throw new IOException("LLM API call to " + url + " failed with HTTP " + response.statusCode() + ": " + detail);
        }
        return new JSONObject(response.body());
    }
}
