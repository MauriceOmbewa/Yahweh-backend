
package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BibleService {

    private final RestClient restClient;

    public BibleService(
            @Value("${api.bible.base-url}") String baseUrl,
            @Value("${api.bible.key}") String apiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("api-key", apiKey)
                .build();
    }

    public JsonNode getAvailableBibles() {
        return restClient.get()
                .uri("/bibles")
                .retrieve()
                .body(JsonNode.class);
    }
}
