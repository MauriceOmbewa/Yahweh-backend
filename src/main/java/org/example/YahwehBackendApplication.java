
package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class YahwehBackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("API_BIBLE_KEY");

        if (apiKey != null && !apiKey.isBlank()) {
            System.setProperty("API_BIBLE_KEY", apiKey);
        }

        SpringApplication.run(YahwehBackendApplication.class, args);
    }
}
