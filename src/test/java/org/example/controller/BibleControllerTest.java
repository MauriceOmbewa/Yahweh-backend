
package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.service.BibleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.example.exception.GlobalExceptionHandler;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BibleController.class)
@Import(GlobalExceptionHandler.class)
class BibleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BibleService bibleService;

    @Test
    void shouldReturnAvailableBibles() throws Exception {
        String json = """
                {
                    "data": [
                        {
                            "id": "de4e12af7f28f599-01",
                            "name": "King James Version"
                        }
                    ]
                }
                """;

        when(bibleService.getAvailableBibles())
                .thenReturn(objectMapper.readTree(json));

        mockMvc.perform(get("/api/bibles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(json));
    }
}
