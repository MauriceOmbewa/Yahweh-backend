
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


    public JsonNode getPassage(String bibleId, String passageId) {
        return restClient.get()
                .uri("/bibles/{bibleId}/passages/{passageId}?content-type=text",
                        bibleId, passageId)
                .retrieve()
                .body(JsonNode.class);
    }


    public JsonNode searchBible(String bibleId, String query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bibles/{bibleId}/search")
                        .queryParam("query", query)
                        .build(bibleId))
                .retrieve()
                .body(JsonNode.class);
    }


}
