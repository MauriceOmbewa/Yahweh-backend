
package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health", description = "Health check")
@RestController
public class HelloController {

    @Operation(summary = "Health check", description = "Returns 200 when the server is up.")
    @GetMapping("/api/hello")
    public Map<String, String> sayHello() {
        return Map.of(
                "message", "Yahweh Backend is running!"
        );
    }
}
