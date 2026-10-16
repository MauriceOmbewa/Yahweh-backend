
package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.service.BibleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Bible", description = "Fetch Bible translations, passages and search")
@RestController
@Validated
public class BibleController {

    private final BibleService bibleService;

    public BibleController(BibleService bibleService) {
        this.bibleService = bibleService;
    }

    @Operation(
            summary = "Get available Bible translations",
            description = "Returns the Bible translations available through API.Bible."
    )
    @GetMapping("/api/bibles")
    public JsonNode getAvailableBibles() {
        return bibleService.getAvailableBibles();
    }


    @Operation(
            summary = "Get a Bible passage",
            description = "Retrieves a passage from a specified Bible translation."
    )
    @GetMapping("/api/bibles/{bibleId}/passages/{passageId}")
    public JsonNode getPassage(
            @Parameter(description = "The ID of the Bible translation, e.g. KJV")
            @PathVariable String bibleId,

            @Parameter(description = "The passage reference, e.g. JHN.3.16")
            @PathVariable String passageId) {

        return bibleService.getPassage(bibleId, passageId);
    }



    @Operation(
            summary = "Search a Bible translation",
            description = "Searches for words or phrases in a specified Bible translation."
    )
    @GetMapping("/api/bibles/{bibleId}/search")
    public JsonNode searchBible(
            @Parameter(description = "The ID of the Bible translation")
            @PathVariable String bibleId,

            @Parameter(description = "The word or phrase to search for")
            @RequestParam
            @NotBlank(message = "Search query must not be empty")
            @Size(max = 100, message = "Search query must not exceed 100 characters")
            String query) {

        return bibleService.searchBible(bibleId, query);
    }



}
